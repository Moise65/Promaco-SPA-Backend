package cl.duoc.promaco.controller;

import cl.duoc.promaco.model.Producto;
import cl.duoc.promaco.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
// CORS: orígenes que pueden llamar a este controller (frontend en Vite y en EC2).
// Se leen de app.cors.origins → variable CORS_ORIGINS. Para cambiar la IP, se cambia esa variable.
@CrossOrigin(origins = "${app.cors.origins}")
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "CRUD de productos (ver: público; crear/editar/eliminar: ADMIN)")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /api/productos  o  GET /api/productos?categoriaId=1
    @GetMapping
    @Operation(summary = "Lista los productos (opcional: filtrar por categoría)")
    public List<Producto> listar(@RequestParam(required = false) Long categoriaId) {
        return productoService.listar(categoriaId);
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Lista los productos con precio de oferta")
    public List<Producto> ofertas() {
        return productoService.ofertas();
    }

    @GetMapping("/criticos")
    @Operation(summary = "Lista los productos con stock crítico (ADMIN)")
    public List<Producto> criticos() {
        return productoService.criticos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por id")
    public Producto obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un producto (ADMIN)")
    public Producto crear(@Valid @RequestBody Producto producto) {
        return productoService.crear(producto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un producto (ADMIN)")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un producto (ADMIN)")
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}
