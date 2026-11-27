package cl.duoc.promaco.service;

import cl.duoc.promaco.model.Categoria;
import cl.duoc.promaco.model.DetalleOrden;
import cl.duoc.promaco.model.Orden;
import cl.duoc.promaco.model.Producto;
import cl.duoc.promaco.repository.CategoriaRepository;
import cl.duoc.promaco.repository.OrdenRepository;
import cl.duoc.promaco.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Calcula los reportes de ventas del panel de administración
// (solo considera las órdenes pagadas).
@Service
public class ReporteService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ReporteService(OrdenRepository ordenRepository, ProductoRepository productoRepository,
                          CategoriaRepository categoriaRepository) {
        this.ordenRepository = ordenRepository;
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public record ProductoVendido(String nombre, int unidades, int monto) {}
    public record VentaCategoria(String categoria, int monto) {}
    public record ResumenVentas(int totalVendido, int cantidadOrdenes,
                                List<ProductoVendido> masVendidos, List<VentaCategoria> porCategoria) {}

    public ResumenVentas resumenVentas() {
        List<Orden> pagadas = ordenRepository.findByEstado(Orden.PAGADA);

        // Unidades y monto vendidos por producto
        Map<Long, String> nombres = new LinkedHashMap<>();
        Map<Long, Integer> unidades = new HashMap<>();
        Map<Long, Integer> montos = new HashMap<>();
        for (Orden orden : pagadas) {
            for (DetalleOrden item : orden.getItems()) {
                nombres.putIfAbsent(item.getProductoId(), item.getNombre());
                unidades.merge(item.getProductoId(), item.getCantidad(), Integer::sum);
                montos.merge(item.getProductoId(), item.subtotal(), Integer::sum);
            }
        }

        List<ProductoVendido> masVendidos = nombres.keySet().stream()
                .map(id -> new ProductoVendido(nombres.get(id), unidades.get(id), montos.get(id)))
                .sorted(Comparator.comparingInt(ProductoVendido::unidades).reversed())
                .limit(5)
                .toList();

        // Monto vendido por categoría
        List<Producto> productos = productoRepository.findAll();
        List<VentaCategoria> porCategoria = categoriaRepository.findAll().stream()
                .map((Categoria categoria) -> new VentaCategoria(categoria.getNombre(),
                        productos.stream()
                                .filter(p -> categoria.getId().equals(p.getCategoriaId()))
                                .mapToInt(p -> montos.getOrDefault(p.getId(), 0))
                                .sum()))
                .toList();

        int totalVendido = pagadas.stream().mapToInt(Orden::getTotal).sum();
        return new ResumenVentas(totalVendido, pagadas.size(), masVendidos, porCategoria);
    }
}
