package com.cibermax.ecommerce.security;

import com.cibermax.ecommerce.acceso.entity.Usuario;
import com.cibermax.ecommerce.acceso.enums.AuthProvider;
import com.cibermax.ecommerce.acceso.enums.Rol;
import com.cibermax.ecommerce.acceso.repository.UsuarioRepository;
import com.cibermax.ecommerce.empleado.entity.Empleado;
import com.cibermax.ecommerce.empleado.repository.EmpleadoRepository;
import com.cibermax.ecommerce.security.jwt.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationServiceTest {

    @Test
    void normalizesEmailAndReturnsRoleAuthorities() {
        Usuario usuario = usuario(true);
        final String[] emailBuscado = new String[1];

        UsuarioRepository usuarios = repositoryProxy(
                UsuarioRepository.class,
                (method, arguments) -> {
                    if (method.getName().equals("findByEmail")) {
                        emailBuscado[0] = (String) arguments[0];
                        return Optional.of(usuario);
                    }
                    return null;
                }
        );

        CustomUserDetailsService service = new CustomUserDetailsService(
                usuarios,
                emptyEmployeeRepository()
        );

        UserDetails details = service.loadUserByUsername(" ADMIN@CIBERMAX.TEST ");

        assertEquals("admin@cibermax.test", emailBuscado[0]);
        assertEquals("admin@cibermax.test", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_ADMINISTRADOR")
        ));
    }

    @Test
    void rejectsInactiveUser() {
        Usuario usuario = usuario(false);
        CustomUserDetailsService service = new CustomUserDetailsService(
                userRepository(usuario),
                emptyEmployeeRepository()
        );

        assertThrows(
                DisabledException.class,
                () -> service.loadUserByUsername("admin@cibermax.test")
        );
    }

    @Test
    void rejectsInactiveEmployee() {
        Usuario usuario = usuario(true);
        Empleado empleado = new Empleado();
        empleado.setUsuario(usuario);
        empleado.setActive(false);

        CustomUserDetailsService service = new CustomUserDetailsService(
                userRepository(usuario),
                employeeRepository(empleado)
        );

        assertThrows(
                DisabledException.class,
                () -> service.loadUserByUsername("admin@cibermax.test")
        );
    }

    private static Usuario usuario(boolean active) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("admin@cibermax.test");
        usuario.setPassword("{bcrypt}hash");
        usuario.setAuthProvider(AuthProvider.LOCAL);
        usuario.setActive(active);
        usuario.setRoles(new HashSet<>(java.util.Set.of(Rol.ADMINISTRADOR)));
        return usuario;
    }

    private static UsuarioRepository userRepository(Usuario usuario) {
        return repositoryProxy(
                UsuarioRepository.class,
                (method, arguments) -> method.getName().equals("findByEmail")
                        ? Optional.of(usuario)
                        : null
        );
    }

    private static EmpleadoRepository emptyEmployeeRepository() {
        return employeeRepository(null);
    }

    private static EmpleadoRepository employeeRepository(Empleado empleado) {
        return repositoryProxy(
                EmpleadoRepository.class,
                (method, arguments) -> method.getName().equals("findByUsuarioId")
                        ? Optional.ofNullable(empleado)
                        : null
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T repositoryProxy(
            Class<T> repositoryType,
            RepositoryInvocation invocation
    ) {
        return (T) Proxy.newProxyInstance(
                repositoryType.getClassLoader(),
                new Class<?>[]{repositoryType},
                (proxy, method, arguments) -> invocation.invoke(
                        method,
                        arguments == null ? new Object[0] : arguments
                )
        );
    }

    @FunctionalInterface
    private interface RepositoryInvocation {
        Object invoke(java.lang.reflect.Method method, Object[] arguments);
    }
}
