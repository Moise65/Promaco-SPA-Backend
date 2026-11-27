package cl.duoc.promaco.repository;

import cl.duoc.promaco.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findAllByOrderByFechaDesc();

    List<Orden> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<Orden> findByEstado(String estado);
}
