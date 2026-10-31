package com.cibermax.ecommerce.acceso.Service;

import com.cibermax.ecommerce.acceso.entity.Usuario;
import com.cibermax.ecommerce.empleado.entity.Empleado;
import com.cibermax.ecommerce.empleado.repository.EmpleadoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {
    private final EmpleadoRepository empleadoRepository;

    public LoginService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }
    public String obtenerEmailAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }

    public Optional<Empleado> obtenerEmpleadoAutenticado() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Usuario usuario)) {
            return Optional.empty();
        }

        return empleadoRepository.findByUsuarioId(usuario.getId());
    }
}
