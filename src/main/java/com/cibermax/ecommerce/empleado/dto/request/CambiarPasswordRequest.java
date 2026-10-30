package com.cibermax.ecommerce.empleado.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(
        String passwordActual,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 64,
                message = "La contraseña debe tener entre 8 y 64 caracteres")
        String passwordNueva
) {
}
