package com.nutrivida.servicios.service;

import com.nutrivida.servicios.dto.AuthResponse;
import com.nutrivida.servicios.dto.LoginRequest;
import com.nutrivida.servicios.dto.RefreshRequest;
import com.nutrivida.servicios.dto.RegistroRequest;
import com.nutrivida.servicios.dto.UsuarioResponse;
import com.nutrivida.servicios.exception.ConflictoException;
import com.nutrivida.servicios.exception.CredencialesInvalidasException;
import com.nutrivida.servicios.exception.CuentaBloqueadaException;
import com.nutrivida.servicios.model.RefreshToken;
import com.nutrivida.servicios.model.Rol;
import com.nutrivida.servicios.model.Usuario;
import com.nutrivida.servicios.repository.RefreshTokenRepository;
import com.nutrivida.servicios.repository.RolRepository;
import com.nutrivida.servicios.repository.UsuarioRepository;
import com.nutrivida.servicios.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_INTENTOS = 3;
    private static final long MINUTOS_BLOQUEO = 5;
    private static final String MSG_CREDENCIALES = "Correo o contraseña incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.jwt.refresh-dias}")
    private long refreshDias;

    // noRollbackFor: los intentos fallidos y el bloqueo deben guardarse aunque se lance la excepcion
    @Transactional(noRollbackFor = {CredencialesInvalidasException.class, CuentaBloqueadaException.class})
    public AuthResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByCorreo(normalizar(req.correo()))
                .orElseThrow(() -> new CredencialesInvalidasException(MSG_CREDENCIALES));

        if (!usuario.isActivo()) {
            throw new CuentaBloqueadaException("La cuenta está desactivada. Contacta al administrador");
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (usuario.getBloqueadoHasta() != null) {
            if (usuario.getBloqueadoHasta().isAfter(ahora)) {
                long minutos = Duration.between(ahora, usuario.getBloqueadoHasta()).toMinutes() + 1;
                throw new CuentaBloqueadaException(
                        "Cuenta bloqueada por intentos fallidos. Intenta nuevamente en " + minutos + " minuto(s)");
            }
            usuario.setBloqueadoHasta(null);
            usuario.setIntentosFallidos(0);
        }

        if (!passwordEncoder.matches(req.contrasena(), usuario.getContrasena())) {
            int intentos = usuario.getIntentosFallidos() + 1;
            if (intentos >= MAX_INTENTOS) {
                usuario.setBloqueadoHasta(ahora.plusMinutes(MINUTOS_BLOQUEO));
                usuario.setIntentosFallidos(0);
                usuarioRepository.save(usuario);
                throw new CuentaBloqueadaException("Demasiados intentos fallidos. Cuenta bloqueada por "
                        + MINUTOS_BLOQUEO + " minutos");
            }
            usuario.setIntentosFallidos(intentos);
            usuarioRepository.save(usuario);
            throw new CredencialesInvalidasException(
                    MSG_CREDENCIALES + ". Intentos restantes: " + (MAX_INTENTOS - intentos));
        }

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);
        return emitirTokens(usuario);
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest req) {
        String correo = normalizar(req.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("Ya existe una cuenta con ese correo");
        }
        Rol cliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new IllegalStateException("Falta el rol CLIENTE en la base de datos"));

        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre().trim());
        usuario.setApellido(req.apellido().trim());
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(req.contrasena()));
        usuario.setFechaNacimiento(req.fechaNacimiento());
        usuario.setGenero(req.genero());
        usuario.setRegion(req.region());
        usuario.setDireccion(req.direccion());
        usuario.setRol(cliente); // el registro publico SIEMPRE crea clientes
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    /** Renueva la sesion: el refresh token se rota (el anterior queda revocado). */
    @Transactional
    public AuthResponse refrescar(RefreshRequest req) {
        RefreshToken guardado = refreshTokenRepository.findByTokenHash(hash(req.refreshToken()))
                .orElseThrow(() -> new CredencialesInvalidasException("Sesión inválida"));
        if (guardado.isRevocado() || guardado.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new CredencialesInvalidasException("La sesión expiró, inicia sesión nuevamente");
        }
        Usuario usuario = guardado.getUsuario();
        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException("La cuenta está desactivada");
        }
        guardado.setRevocado(true);
        refreshTokenRepository.save(guardado);
        return emitirTokens(usuario);
    }

    @Transactional
    public void cerrarSesion(RefreshRequest req) {
        refreshTokenRepository.findByTokenHash(hash(req.refreshToken())).ifPresent(token -> {
            token.setRevocado(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthResponse emitirTokens(Usuario usuario) {
        String accessToken = jwtService.generar(usuario);

        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken entidad = new RefreshToken();
        entidad.setUsuario(usuario);
        entidad.setTokenHash(hash(refreshToken)); // en la BD solo se guarda el hash
        entidad.setExpiraEn(LocalDateTime.now().plusDays(refreshDias));
        refreshTokenRepository.save(entidad);

        return new AuthResponse(accessToken, refreshToken, jwtService.getExpiracionSegundos(),
                UsuarioResponse.desde(usuario));
    }

    private String hash(String token) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String normalizar(String correo) {
        return correo.trim().toLowerCase();
    }
}
