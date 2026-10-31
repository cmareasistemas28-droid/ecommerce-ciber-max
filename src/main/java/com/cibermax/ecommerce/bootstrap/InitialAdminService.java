package com.cibermax.ecommerce.bootstrap;

import com.cibermax.ecommerce.acceso.entity.Usuario;
import com.cibermax.ecommerce.acceso.enums.AuthProvider;
import com.cibermax.ecommerce.acceso.enums.Rol;
import com.cibermax.ecommerce.acceso.repository.UsuarioRepository;
import com.cibermax.ecommerce.empleado.entity.Empleado;
import com.cibermax.ecommerce.empleado.repository.EmpleadoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class InitialAdminService {

    private static final short BOOTSTRAP_ID = 1;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{8}$");

    private final InitialAdminProperties properties;
    private final AdminBootstrapControlRepository controlRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialAdminService(
            InitialAdminProperties properties,
            AdminBootstrapControlRepository controlRepository,
            UsuarioRepository usuarioRepository,
            EmpleadoRepository empleadoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.controlRepository = controlRepository;
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public BootstrapResult initialize() {
        AdminBootstrapControl control = controlRepository
                .findByIdForUpdate(BOOTSTRAP_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el registro de control del bootstrap administrativo"
                ));

        if (control.isInitialized()) {
            return BootstrapResult.ALREADY_INITIALIZED;
        }

        if (usuarioRepository.existsByRole(Rol.ADMINISTRADOR)) {
            markAsInitialized(control);
            return BootstrapResult.ADMIN_ALREADY_EXISTS;
        }

        InitialAdminData data = validateAndReadProperties();

        if (usuarioRepository.existsByEmail(data.email())) {
            throw new IllegalStateException(
                    "El correo del administrador inicial ya está registrado"
            );
        }
        if (empleadoRepository.existsByDni(data.dni())) {
            throw new IllegalStateException(
                    "El DNI del administrador inicial ya está registrado"
            );
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(data.email());
        usuario.setPassword(passwordEncoder.encode(data.password()));
        usuario.setAuthProvider(AuthProvider.LOCAL);
        usuario.setActive(true);
        usuario.setRoles(new HashSet<>(Set.of(Rol.ADMINISTRADOR)));
        usuarioRepository.saveAndFlush(usuario);

        Empleado empleado = new Empleado();
        empleado.setUsuario(usuario);
        empleado.setNombres(data.nombres());
        empleado.setApellidos(data.apellidos());
        empleado.setDni(data.dni());
        empleado.setFechaNacimiento(data.fechaNacimiento());
        empleado.setTelefono(data.telefono());
        empleado.setDireccion(data.direccion());
        empleado.setReferencia(data.referencia());
        empleado.setActive(true);
        empleadoRepository.saveAndFlush(empleado);

        markAsInitialized(control);
        return BootstrapResult.CREATED;
    }

    private void markAsInitialized(AdminBootstrapControl control) {
        control.setInitialized(true);
        control.setInitializedAt(LocalDateTime.now());
        controlRepository.save(control);
    }

    private InitialAdminData validateAndReadProperties() {
        String email = normalize(properties.getEmail());
        String password = properties.getPassword();
        String nombres = normalize(properties.getNombres());
        String apellidos = normalize(properties.getApellidos());
        String dni = normalize(properties.getDni());
        String fechaNacimientoRaw = normalize(properties.getFechaNacimiento());

        requireText(email, "ADMIN_INITIAL_EMAIL");
        requireText(password, "ADMIN_INITIAL_PASSWORD");
        requireText(nombres, "ADMIN_INITIAL_NOMBRES");
        requireText(apellidos, "ADMIN_INITIAL_APELLIDOS");
        requireText(dni, "ADMIN_INITIAL_DNI");
        requireText(fechaNacimientoRaw, "ADMIN_INITIAL_FECHA_NACIMIENTO");

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalStateException("ADMIN_INITIAL_EMAIL no es válido");
        }
        if (!DNI_PATTERN.matcher(dni).matches()) {
            throw new IllegalStateException(
                    "ADMIN_INITIAL_DNI debe contener exactamente 8 dígitos"
            );
        }
        validatePassword(password);

        LocalDate fechaNacimiento;
        try {
            fechaNacimiento = LocalDate.parse(
                    fechaNacimientoRaw,
                    DateTimeFormatter.ISO_LOCAL_DATE
            );
        } catch (DateTimeParseException exception) {
            throw new IllegalStateException(
                    "ADMIN_INITIAL_FECHA_NACIMIENTO debe usar YYYY-MM-DD",
                    exception
            );
        }
        if (!fechaNacimiento.isBefore(LocalDate.now())) {
            throw new IllegalStateException(
                    "ADMIN_INITIAL_FECHA_NACIMIENTO debe ser anterior a hoy"
            );
        }

        return new InitialAdminData(
                email,
                password,
                nombres,
                apellidos,
                dni,
                fechaNacimiento,
                normalize(properties.getTelefono()),
                normalize(properties.getDireccion()),
                normalize(properties.getReferencia())
        );
    }

    private void validatePassword(String password) {
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (password.length() < 12 || !hasLetter || !hasDigit) {
            throw new IllegalStateException(
                    "ADMIN_INITIAL_PASSWORD debe tener al menos 12 caracteres, "
                            + "una letra y un número"
            );
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.strip();
    }

    private void requireText(String value, String variable) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(
                    "Falta la variable obligatoria " + variable
            );
        }
    }

    public enum BootstrapResult {
        CREATED,
        ADMIN_ALREADY_EXISTS,
        ALREADY_INITIALIZED
    }

    private record InitialAdminData(
            String email,
            String password,
            String nombres,
            String apellidos,
            String dni,
            LocalDate fechaNacimiento,
            String telefono,
            String direccion,
            String referencia
    ) {
        private InitialAdminData {
            email = email.toLowerCase(Locale.ROOT);
        }
    }
}
