package cl.duoc.promaco.dto;

import cl.duoc.promaco.model.Usuario;

// Respuesta del login: el token JWT y los datos del usuario (sin la contraseña)
public record AuthResponse(String token, String tipo, long expiraEnMs, Usuario usuario) {
}
