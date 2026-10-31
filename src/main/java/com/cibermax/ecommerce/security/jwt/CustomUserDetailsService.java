package com.cibermax.ecommerce.security.jwt;

import com.cibermax.ecommerce.acceso.repository.UsuarioRepository;
import com.cibermax.ecommerce.empleado.entity.Empleado;
import com.cibermax.ecommerce.empleado.repository.EmpleadoRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;

    public CustomUserDetailsService(
            UsuarioRepository usuarioRepository,
            EmpleadoRepository empleadoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String emailNormalizado = email == null
                ? ""
                : email.strip().toLowerCase(Locale.ROOT);

        var usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado"
                ));

        if (!usuario.isActive()) {
            throw new DisabledException("El usuario está inactivo");
        }

        Empleado empleado = empleadoRepository
                .findByUsuarioId(usuario.getId())
                .orElse(null);

        if (empleado != null && !empleado.isActive()) {
            throw new DisabledException("El empleado está inactivo");
        }

        return usuario;
    }
}
