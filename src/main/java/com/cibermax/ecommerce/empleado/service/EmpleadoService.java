package com.cibermax.ecommerce.empleado.service;

import com.cibermax.ecommerce.acceso.entity.Usuario;
import com.cibermax.ecommerce.acceso.enums.AuthProvider;
import com.cibermax.ecommerce.acceso.repository.UsuarioRepository;
import com.cibermax.ecommerce.empleado.dto.Response.EmpleadoResponse;
import com.cibermax.ecommerce.empleado.dto.request.EmpleadoCreateRequest;
import com.cibermax.ecommerce.empleado.dto.request.EmpleadoUpdateRequest;
import com.cibermax.ecommerce.empleado.entity.Empleado;
import com.cibermax.ecommerce.empleado.mapper.EmpleadoMapper;
import com.cibermax.ecommerce.empleado.repository.EmpleadoRepository;
import com.cibermax.ecommerce.storage.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

@Service
@Validated
@Transactional(readOnly = true)
public class EmpleadoService {
    private final PasswordEncoder passwordEncoder;
    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoMapper empleadoMapper;
    private final FileStorageService fileStorageService;

    public EmpleadoService(PasswordEncoder passwordEncoder, EmpleadoRepository empleadoRepository, UsuarioRepository usuarioRepository, EmpleadoMapper empleadoMapper, FileStorageService fileStorageService) {
        this.passwordEncoder = passwordEncoder;
        this.empleadoRepository = empleadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.empleadoMapper = empleadoMapper;
        this.fileStorageService = fileStorageService;
    }
    @Transactional
    public EmpleadoResponse crear(
            @Valid EmpleadoCreateRequest request
    ) {
        String email = normalizarEmail(request.email());

        if (empleadoRepository.existsByDni(request.dni())) {
            throw new IllegalArgumentException(
                    "El DNI ya está registrado"
            );
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setAuthProvider(AuthProvider.LOCAL);
        usuario.setRoles(new HashSet<>(request.roles()));
        usuario.setActive(true);

        usuarioRepository.save(usuario);

        Empleado empleado = empleadoMapper.toEmpleado(request);
        empleado.setUsuario(usuario);
        empleado.setActive(true);

        if (request.foto() != null && !request.foto().isEmpty()) {
            empleado.setFoto(
                    fileStorageService.guardar(request.foto())
            );
        }

        empleadoRepository.saveAndFlush(empleado);

        return empleadoMapper.toEmpleadoResponse(empleado);
    }

    @Transactional
    public EmpleadoResponse actualizar(UUID id, @Valid EmpleadoUpdateRequest request) {
        Empleado empleado = obtenerEmpleado(id);
        Usuario usuario = empleado.getUsuario();
        String email = normalizarEmail(request.email());
        if (empleadoRepository.existsByDniAndIdNot(request.dni(), id)) {
            throw new IllegalArgumentException(
                    "El DNI ya está registrado"
            );
        }
        if (usuarioRepository.existsByEmailAndIdNot(email, usuario.getId())) {
            throw new IllegalArgumentException(
                    "El correo ya está registrado"
            );
        }
        empleadoMapper.toUpdate(request, empleado);
        usuario.setEmail(email);
        usuario.getRoles().clear();
        usuario.getRoles().addAll(request.roles());
        // Sin foto nueva, conserva la actual.
        if (request.foto() != null && !request.foto().isEmpty()) {
            empleado.setFoto(fileStorageService.guardar(request.foto()));
        }
        usuarioRepository.save(usuario);
        empleadoRepository.saveAndFlush(empleado);
        return empleadoMapper.toEmpleadoResponse(empleado);
    }
    public EmpleadoResponse buscarPorId(UUID id) {
        Empleado empleado = obtenerEmpleado(id);

        return empleadoMapper.toEmpleadoResponse(empleado);
    }

    public Page<EmpleadoResponse> listar(Pageable pageable) {
        return empleadoRepository.findAll(pageable)
                .map(empleadoMapper::toEmpleadoResponse);
    }

    @Transactional
    public void desactivar(UUID id) {
        Empleado empleado = obtenerEmpleado(id);
        empleado.setActive(false);

        empleadoRepository.save(empleado);
    }

    @Transactional
    public void activar(UUID id) {
        Empleado empleado = obtenerEmpleado(id);
        empleado.setActive(true);

        empleadoRepository.save(empleado);
    }

    private Empleado obtenerEmpleado(UUID id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el empleado con ID: " + id
                ));
    }

    private String normalizarEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
