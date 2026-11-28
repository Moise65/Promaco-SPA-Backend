package cl.duoc.promaco.controller;

import cl.duoc.promaco.dto.AuthResponse;
import cl.duoc.promaco.dto.LoginRequest;
import cl.duoc.promaco.dto.RegistroRequest;
import cl.duoc.promaco.model.Usuario;
import cl.duoc.promaco.security.JwtService;
import cl.duoc.promaco.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
// CORS: orígenes que pueden llamar a este controller (frontend en Vite y en EC2).
// Se leen de app.cors.origins → variable CORS_ORIGINS. Para cambiar la IP, se cambia esa variable.
@CrossOrigin(origins = "${app.cors.origins}")
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login, registro y renovación del token JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
                          JwtService jwtService, UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    // 1. Spring Security compara el correo y la contraseña (BCrypt) con la base de datos.
    // 2. Si son correctos, se genera el token JWT y se devuelve junto al usuario.
    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve el token JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correo().trim(), request.clave()));
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return respuesta(user);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una cuenta de cliente y devuelve su token")
    public AuthResponse registro(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = usuarioService.registrar(request);
        return respuesta(userDetailsService.loadUserByUsername(usuario.getCorreo()));
    }

    // Devuelve el usuario dueño del token (sirve para comprobar que el token sigue vigente)
    @GetMapping("/me")
    @Operation(summary = "Datos del usuario que envía el token")
    public Usuario me(Authentication authentication) {
        return usuarioService.obtenerPorCorreo(authentication.getName());
    }

    // Renovación: con un token vigente se pide uno nuevo con 2 horas más
    @PostMapping("/renovar")
    @Operation(summary = "Entrega un token nuevo a partir de uno vigente")
    public AuthResponse renovar(Authentication authentication) {
        return respuesta(userDetailsService.loadUserByUsername(authentication.getName()));
    }

    private AuthResponse respuesta(UserDetails user) {
        String token = jwtService.generateToken(user);
        Usuario usuario = usuarioService.obtenerPorCorreo(user.getUsername());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(), usuario);
    }
}
