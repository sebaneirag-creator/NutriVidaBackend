package com.nutrivida.servicios.service;

import com.nutrivida.servicios.dto.PerfilRequest;
import com.nutrivida.servicios.dto.UsuarioRequest;
import com.nutrivida.servicios.dto.UsuarioResponse;
import com.nutrivida.servicios.exception.ConflictoException;
import com.nutrivida.servicios.exception.RecursoNoEncontradoException;
import com.nutrivida.servicios.exception.ReglaNegocioException;
import com.nutrivida.servicios.model.Rol;
import com.nutrivida.servicios.model.Usuario;
import com.nutrivida.servicios.repository.PedidoRepository;
import com.nutrivida.servicios.repository.RefreshTokenRepository;
import com.nutrivida.servicios.repository.RolRepository;
import com.nutrivida.servicios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PedidoRepository pedidoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------- Administracion ----------

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll(Sort.by("id")).stream().map(UsuarioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest req) {
        String correo = normalizar(req.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("Ya existe un usuario con ese correo");
        }
        if (req.contrasena() == null || req.contrasena().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria al crear un usuario");
        }
        Usuario usuario = new Usuario();
        copiarDatos(usuario, req.nombre(), req.apellido(), correo, req.fechaNacimiento(),
                req.genero(), req.region(), req.direccion());
        usuario.setContrasena(passwordEncoder.encode(req.contrasena()));
        usuario.setRol(buscarRol(req.rol()));
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest req, Long idActual) {
        Usuario usuario = buscar(id);
        String correo = normalizar(req.correo());
        if (usuarioRepository.existsByCorreoAndIdNot(correo, id)) {
            throw new ConflictoException("Ya existe otro usuario con ese correo");
        }
        Rol rol = buscarRol(req.rol());
        if (id.equals(idActual) && !rol.getId().equals(usuario.getRol().getId())) {
            throw new ReglaNegocioException("No puedes cambiar tu propio rol");
        }
        copiarDatos(usuario, req.nombre(), req.apellido(), correo, req.fechaNacimiento(),
                req.genero(), req.region(), req.direccion());
        if (req.contrasena() != null && !req.contrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(req.contrasena()));
        }
        usuario.setRol(rol);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarActivo(Long id, boolean activo, Long idActual) {
        Usuario usuario = buscar(id);
        if (id.equals(idActual) && !activo) {
            throw new ReglaNegocioException("No puedes desactivar tu propia cuenta");
        }
        usuario.setActivo(activo);
        if (!activo) {
            refreshTokenRepository.revocarTodosDelUsuario(id); // cierra sus sesiones
        }
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse desbloquear(Long id) {
        Usuario usuario = buscar(id);
        usuario.setBloqueadoHasta(null);
        usuario.setIntentosFallidos(0);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id, Long idActual) {
        Usuario usuario = buscar(id);
        if (id.equals(idActual)) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta");
        }
        if (pedidoRepository.existsByUsuarioId(id)) {
            throw new ConflictoException(
                    "No se puede eliminar: el usuario tiene pedidos registrados. Puedes desactivarlo");
        }
        refreshTokenRepository.eliminarTodosDelUsuario(id);
        usuarioRepository.delete(usuario);
    }

    // ---------- Perfil propio ----------

    @Transactional(readOnly = true)
    public UsuarioResponse miPerfil(Long idActual) {
        return UsuarioResponse.desde(buscar(idActual));
    }

    @Transactional
    public UsuarioResponse actualizarMiPerfil(Long idActual, PerfilRequest req) {
        Usuario usuario = buscar(idActual);
        String correo = normalizar(req.correo());
        if (usuarioRepository.existsByCorreoAndIdNot(correo, idActual)) {
            throw new ConflictoException("Ya existe otro usuario con ese correo");
        }
        copiarDatos(usuario, req.nombre(), req.apellido(), correo, req.fechaNacimiento(),
                req.genero(), req.region(), req.direccion());
        if (req.contrasena() != null && !req.contrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(req.contrasena()));
        }
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    // ---------- Auxiliares ----------

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado (id " + id + ")"));
    }

    private Rol buscarRol(String nombre) {
        return rolRepository.findByNombre(nombre.trim().toUpperCase())
                .orElseThrow(() -> new ReglaNegocioException(
                        "Rol inválido. Usa ADMINISTRADOR, VENDEDOR o CLIENTE"));
    }

    private void copiarDatos(Usuario u, String nombre, String apellido, String correo,
                             java.time.LocalDate fechaNacimiento, String genero, String region, String direccion) {
        u.setNombre(nombre.trim());
        u.setApellido(apellido.trim());
        u.setCorreo(correo);
        u.setFechaNacimiento(fechaNacimiento);
        u.setGenero(genero);
        u.setRegion(region);
        u.setDireccion(direccion);
    }

    private String normalizar(String correo) {
        return correo.trim().toLowerCase();
    }
}
