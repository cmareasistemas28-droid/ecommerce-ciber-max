package com.cibermax.ecommerce.empleado.dto.Response;

import com.cibermax.ecommerce.acceso.enums.Rol;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record EmpleadoResponse(
        UUID id,
        String nombres,
        String apellidos,
        String dni,
        LocalDate fechaNacimiento,
        Integer edad,
        String telefono,
        String direccion,
        String referencia,
        String foto,
        String email,
        Set<Rol> roles,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
