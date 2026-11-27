package cl.duoc.promaco.dto;

import jakarta.validation.constraints.NotBlank;

// Lo que envía el frontend al iniciar sesión
public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio") String correo,
        @NotBlank(message = "La contraseña es obligatoria") String clave) {
}
