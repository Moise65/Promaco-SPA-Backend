package cl.duoc.promaco.controller;

import cl.duoc.promaco.dto.CompraRequest;
import cl.duoc.promaco.model.Orden;
import cl.duoc.promaco.model.Usuario;
import cl.duoc.promaco.service.OrdenService;
import cl.duoc.promaco.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@Tag(name = "Órdenes", description = "Compras: pagar (cliente/admin), ver las propias y listado completo (ADMIN)")
public class OrdenController {

    private final OrdenService ordenService;
    private final UsuarioService usuarioService;

    public OrdenController(OrdenService ordenService, UsuarioService usuarioService) {
        this.ordenService = ordenService;
        this.usuarioService = usuarioService;
    }

    // El usuario se obtiene del TOKEN (no del JSON), así nadie compra a nombre de otro.
    // Devuelve la orden con estado "pagada" o "rechazada" (si falta stock).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Procesa una compra (requiere iniciar sesión)")
    public Orden comprar(@Valid @RequestBody CompraRequest compra, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorCorreo(authentication.getName());
        return ordenService.procesarCompra(compra, usuario);
    }

    @GetMapping("/mias")
    @Operation(summary = "Compras del usuario que inició sesión")
    public List<Orden> mias(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorCorreo(authentication.getName());
        return ordenService.deUsuario(usuario.getId());
    }

    @GetMapping
    @Operation(summary = "Lista todas las órdenes (ADMIN)")
    public List<Orden> listar() {
        return ordenService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una orden / boleta (ADMIN)")
    public Orden obtener(@PathVariable Long id) {
        return ordenService.obtener(id);
    }
}
