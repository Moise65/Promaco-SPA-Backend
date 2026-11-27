package cl.duoc.promaco.service;

import cl.duoc.promaco.exception.RecursoNoEncontradoException;
import cl.duoc.promaco.exception.ReglaNegocioException;
import cl.duoc.promaco.model.Categoria;
import cl.duoc.promaco.model.Producto;
import cl.duoc.promaco.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Lógica de negocio de los productos
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    public ProductoService(ProductoRepository productoRepository, CategoriaService categoriaService) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
    }

    // Lista todos, o solo los de una categoría si viene categoriaId
    public List<Producto> listar(Long categoriaId) {
        if (categoriaId != null) {
            return productoRepository.findByCategoria_IdOrderByIdAsc(categoriaId);
        }
        return productoRepository.findAll().stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    public List<Producto> ofertas() {
        return productoRepository.findByPrecioOfertaIsNotNullOrderByIdAsc();
    }

    public List<Producto> criticos() {
        return productoRepository.findCriticos();
    }

    public Producto obtener(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }

    public Producto crear(Producto producto) {
        if (productoRepository.existsByCodigoIgnoreCase(producto.getCodigo())) {
            throw new ReglaNegocioException("Ya existe un producto con el código " + producto.getCodigo());
        }
        validarOferta(producto);
        producto.setId(null);
        producto.setCategoria(buscarCategoria(producto.getCategoriaId()));
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto existente = obtener(id);
        validarOferta(datos);
        existente.setCodigo(datos.getCodigo());
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecio(datos.getPrecio());
        existente.setPrecioOferta(datos.getPrecioOferta());
        existente.setStock(datos.getStock());
        existente.setStockCritico(datos.getStockCritico());
        existente.setImagen(datos.getImagen());
        existente.setCategoria(buscarCategoria(datos.getCategoriaId()));
        return productoRepository.save(existente);
    }

    public void eliminar(Long id) {
        obtener(id);
        productoRepository.deleteById(id);
    }

    // Regla: el precio de oferta debe ser menor que el precio normal
    private void validarOferta(Producto producto) {
        if (producto.getPrecioOferta() != null && producto.getPrecioOferta() >= producto.getPrecio()) {
            throw new ReglaNegocioException("El precio de oferta debe ser menor que el precio normal");
        }
    }

    private Categoria buscarCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new ReglaNegocioException("Debes elegir una categoría");
        }
        return categoriaService.obtener(categoriaId);
    }
}
