package com.cibermax.ecommerce.empleado.dto.request;

import com.cibermax.ecommerce.acceso.enums.Rol;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public record EmpleadoCreateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombres,
        @NotBlank(message = "El apellido es obligatorio")
        String apellidos,
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
        String dni,
        @NotNull
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        String fechaNacimiento,
        @Size(max = 20)
        String telefono,
        @Size(max = 255)
        String direccion,
        @Size(max = 255)
        String referencia,
        MultipartFile foto,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato válido")
        @Size(max = 100, message = "El correo no debe superar los 100 caracteres")
        String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 4,
                message = "La contraseña debe tener minimo 4 caracteres")
        String password,
        @NotEmpty(message = "Debes selecciona al menos un rol")
        Set<@NotNull(message = "El rol no debe ser nulo") Rol> roles
) {
}
