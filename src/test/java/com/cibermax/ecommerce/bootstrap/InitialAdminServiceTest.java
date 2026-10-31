package com.cibermax.ecommerce.bootstrap;

import com.cibermax.ecommerce.acceso.entity.Usuario;
import com.cibermax.ecommerce.acceso.enums.Rol;
import com.cibermax.ecommerce.acceso.repository.UsuarioRepository;
import com.cibermax.ecommerce.empleado.entity.Empleado;
import com.cibermax.ecommerce.empleado.repository.EmpleadoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitialAdminServiceTest {

    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    @Test
    void runnerDoesNothingWhenBootstrapIsDisabled() {
        InitialAdminProperties properties = properties(false);
        InitialAdminBootstrap runner = new InitialAdminBootstrap(
                properties,
                null
        );

        assertDoesNotThrow(() -> runner.run(
                new DefaultApplicationArguments()
        ));
    }

    @Test
    void createsAdminEmployeeAndBcryptPassword() {
        AdminBootstrapControl control = control(false);
        Usuario[] savedUser = new Usuario[1];
        Empleado[] savedEmployee = new Empleado[1];

        InitialAdminService service = service(
                properties(true),
                control,
                (method, args) -> {
                    if (method.getName().equals("saveAndFlush")) {
                        savedUser[0] = (Usuario) args[0];
                        return args[0];
                    }
                    if (method.getName().equals("existsByRole")
                            || method.getName().equals("existsByEmail")) {
                        return false;
                    }
                    return null;
                },
                (method, args) -> {
                    if (method.getName().equals("saveAndFlush")) {
                        savedEmployee[0] = (Empleado) args[0];
                        return args[0];
                    }
                    if (method.getName().equals("existsByDni")) {
                        return false;
                    }
                    return null;
                }
        );

        assertEquals(
                InitialAdminService.BootstrapResult.CREATED,
                service.initialize()
        );
        assertNotNull(savedUser[0]);
        assertNotNull(savedEmployee[0]);
        assertEquals("admin@example.com", savedUser[0].getEmail());
        assertTrue(savedUser[0].getRoles().contains(Rol.ADMINISTRADOR));
        assertTrue(savedUser[0].isActive());
        assertTrue(passwordEncoder.matches(
                "UnaFraseSegura2026", savedUser[0].getPassword()
        ));
        assertEquals(savedUser[0], savedEmployee[0].getUsuario());
        assertTrue(savedEmployee[0].isActive());
        assertTrue(control.isInitialized());
        assertNotNull(control.getInitializedAt());
    }

    @Test
    void doesNotCreateAgainAfterControlIsInitialized() {
        AdminBootstrapControl control = control(true);
        InitialAdminService service = service(
                properties(true),
                control,
                (method, args) -> {
                    throw new AssertionError("No debe consultar ni guardar usuarios");
                },
                (method, args) -> {
                    throw new AssertionError("No debe consultar ni guardar empleados");
                }
        );

        assertEquals(
                InitialAdminService.BootstrapResult.ALREADY_INITIALIZED,
                service.initialize()
        );
    }

    @Test
    void rejectsWeakPasswordBeforePersisting() {
        InitialAdminProperties properties = properties(true);
        properties.setPassword("admin123");
        InitialAdminService service = service(
                properties,
                control(false),
                (method, args) -> false,
                (method, args) -> false
        );

        assertThrows(IllegalStateException.class, service::initialize);
    }

    private InitialAdminProperties properties(boolean enabled) {
        InitialAdminProperties properties = new InitialAdminProperties();
        properties.setEnabled(enabled);
        properties.setEmail(" ADMIN@EXAMPLE.COM ");
        properties.setPassword("UnaFraseSegura2026");
        properties.setNombres("Admin");
        properties.setApellidos("Inicial");
        properties.setDni("12345678");
        properties.setFechaNacimiento("1990-01-01");
        return properties;
    }

    private AdminBootstrapControl control(boolean initialized) {
        AdminBootstrapControl control = new AdminBootstrapControl();
        control.setId((short) 1);
        control.setInitialized(initialized);
        return control;
    }

    private InitialAdminService service(
            InitialAdminProperties properties,
            AdminBootstrapControl control,
            RepositoryInvocation userInvocation,
            RepositoryInvocation employeeInvocation
    ) {
        AdminBootstrapControlRepository controls = repositoryProxy(
                AdminBootstrapControlRepository.class,
                (method, args) -> method.getName().equals("findByIdForUpdate")
                        ? Optional.of(control)
                        : args.length == 1 && method.getName().equals("save")
                        ? args[0]
                        : null
        );

        return new InitialAdminService(
                properties,
                controls,
                repositoryProxy(UsuarioRepository.class, userInvocation),
                repositoryProxy(EmpleadoRepository.class, employeeInvocation),
                passwordEncoder
        );
    }

    @SuppressWarnings("unchecked")
    private <T> T repositoryProxy(
            Class<T> type,
            RepositoryInvocation invocation
    ) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> invocation.invoke(
                        method,
                        args == null ? new Object[0] : args
                )
        );
    }

    @FunctionalInterface
    private interface RepositoryInvocation {
        Object invoke(Method method, Object[] args);
    }
}
