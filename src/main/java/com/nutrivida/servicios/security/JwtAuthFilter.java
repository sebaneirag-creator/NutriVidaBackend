package com.nutrivida.servicios.security;

import com.nutrivida.servicios.model.Usuario;
import com.nutrivida.servicios.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee "Authorization: Bearer <token>", valida firma y vigencia y carga al usuario desde la BD
 * (así un usuario desactivado o con rol cambiado se refleja de inmediato).
 * Si el token no sirve, la solicitud sigue sin autenticar y Spring Security responde 401.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.validarYObtenerId(cabecera.substring(7))
                    .flatMap(usuarioRepository::findById)
                    .filter(Usuario::isActivo)
                    .ifPresent(usuario -> {
                        UsuarioAutenticado principal = new UsuarioAutenticado(
                                usuario.getId(), usuario.getCorreo(), usuario.getRol().getNombre());
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                principal, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(request, response);
    }
}
