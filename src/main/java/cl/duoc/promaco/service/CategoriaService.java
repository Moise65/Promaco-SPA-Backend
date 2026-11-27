package cl.duoc.promaco.service;

import cl.duoc.promaco.exception.RecursoNoEncontradoException;
import cl.duoc.promaco.exception.ReglaNegocioException;
import cl.duoc.promaco.model.Categoria;
import cl.duoc.promaco.repository.CategoriaRepository;
import cl.duoc.promaco.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Lógica de negocio de las categorías
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria obtener(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }

    public Categoria crear(Categoria categoria) {
        if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
        }
        categoria.setId(null);
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Long id, Categoria datos) {
        Categoria existente = obtener(id);
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setIcono(datos.getIcono());
        return categoriaRepository.save(existente);
    }

    // Regla: no se puede borrar una categoría que todavía tiene productos
    public void eliminar(Long id) {
        obtener(id);
        if (productoRepository.existsByCategoria_Id(id)) {
            throw new ReglaNegocioException("No se puede eliminar: la categoría tiene productos asociados");
        }
        categoriaRepository.deleteById(id);
    }
}
