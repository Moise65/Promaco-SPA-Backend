package cl.duoc.promaco.controller;

import cl.duoc.promaco.model.Orden;
import cl.duoc.promaco.model.Usuario;
import cl.duoc.promaco.service.OrdenService;
import cl.duoc.promaco.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Todo este controlador es solo para ADMIN (ver SecurityConfig)
@RestController
// CORS: orígenes que pueden llamar a este controller (frontend en Vite y en EC2).
// Se leen de app.cors.origins → variable CORS_ORIGINS. Para cambiar la IP, se cambia esa variable.
@CrossOrigin(origins = "${app.cors.origins}")
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "CRUD de usuarios (solo ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final OrdenService ordenService;

    public UsuarioController(UsuarioService usuarioService, OrdenService ordenService) {
        this.usuarioService = usuarioService;
        this.ordenService = ordenService;
    }

    @GetMapping
    @Operation(summary = "Lista todos los usuarios")
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario por id")
    public Usuario obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @GetMapping("/{id}/ordenes")
    @Operation(summary = "Historial de compras de un usuario")
    public List<Orden> historial(@PathVariable Long id) {
        usuarioService.obtener(id);
        return ordenService.deUsuario(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario (admin o cliente)")
    public Usuario crear(@Valid @RequestBody Usuario usuario) {
        return usuarioService.crear(usuario);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un usuario (la contraseña es opcional)")
    public Usuario actualizar(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        return usuarioService.actualizar(id, usuario);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un usuario (no puede ser el propio)")
    public void eliminar(@PathVariable Long id, Authentication authentication) {
        usuarioService.eliminar(id, authentication.getName());
    }
}
