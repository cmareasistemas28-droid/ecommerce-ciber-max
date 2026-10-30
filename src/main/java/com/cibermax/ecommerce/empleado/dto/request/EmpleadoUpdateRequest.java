package com.cibermax.ecommerce.empleado.dto.request;

import com.cibermax.ecommerce.acceso.enums.Rol;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;

public record EmpleadoUpdateRequest(
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no deben superar los 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no deben superar los 100 caracteres")
        String apellidos,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
        String dni,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fechaNacimiento,

        @Size(max = 20, message = "El teléfono no debe superar los 20 caracteres")
        String telefono,

        @Size(max = 255, message = "La dirección no debe superar los 255 caracteres")
        String direccion,

        @Size(max = 255, message = "La referencia no debe superar los 255 caracteres")
        String referencia,

        MultipartFile foto,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato válido")
        @Size(max = 254, message = "El correo no debe superar los 254 caracteres")
        String email,

        @NotEmpty(message = "Debes seleccionar al menos un rol")
        Set<@NotNull(message = "El rol no puede ser nulo") Rol> roles

) {
}
