package cl.duoc.promaco.repository;

import cl.duoc.promaco.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository ya trae el CRUD: findAll, findById, save, deleteById...
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}
