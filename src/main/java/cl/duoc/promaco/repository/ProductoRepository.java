package cl.duoc.promaco.repository;

import cl.duoc.promaco.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Spring arma la consulta a partir del nombre del método.
    // "Categoria_Id" significa: el id de la categoría asociada al producto.
    List<Producto> findByCategoria_IdOrderByIdAsc(Long categoriaId);

    List<Producto> findByPrecioOfertaIsNotNullOrderByIdAsc();

    boolean existsByCategoria_Id(Long categoriaId);

    boolean existsByCodigoIgnoreCase(String codigo);

    // Productos con stock igual o menor a su stock crítico (consulta JPQL)
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockCritico ORDER BY p.stock ASC")
    List<Producto> findCriticos();
}
