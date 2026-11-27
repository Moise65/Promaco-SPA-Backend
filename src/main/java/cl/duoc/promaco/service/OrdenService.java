package cl.duoc.promaco.service;

import cl.duoc.promaco.dto.CompraRequest;
import cl.duoc.promaco.exception.RecursoNoEncontradoException;
import cl.duoc.promaco.model.DetalleOrden;
import cl.duoc.promaco.model.Orden;
import cl.duoc.promaco.model.Producto;
import cl.duoc.promaco.model.Usuario;
import cl.duoc.promaco.repository.OrdenRepository;
import cl.duoc.promaco.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Lógica de negocio de las compras.
// Reglas de Promaco:
//  1. "Promoción Constructor": 15% de descuento en cada línea con 50 o más unidades.
//  2. No se vende sin stock: si falta stock de algún producto, la orden se RECHAZA.
//  3. Si el pago es exitoso, se descuenta el stock de cada producto.
@Service
public class OrdenService {

    public static final int PROMO_MINIMO_UNIDADES = 50;
    public static final double PROMO_DESCUENTO = 0.15;

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;

    public OrdenService(OrdenRepository ordenRepository, ProductoRepository productoRepository) {
        this.ordenRepository = ordenRepository;
        this.productoRepository = productoRepository;
    }

    public List<Orden> listar() {
        return ordenRepository.findAllByOrderByFechaDesc();
    }

    public Orden obtener(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
    }

    public List<Orden> deUsuario(Long usuarioId) {
        return ordenRepository.findByUsuario_IdOrderByFechaDesc(usuarioId);
    }

    // @Transactional: si algo falla a mitad de camino, no queda nada a medias
    // (ni stock descontado sin orden, ni orden sin stock descontado).
    @Transactional
    public Orden procesarCompra(CompraRequest compra, Usuario usuario) {
        Orden orden = new Orden();
        orden.setFecha(LocalDateTime.now());
        orden.setNumero(generarNumero(orden.getFecha()));
        orden.setUsuario(usuario);
        orden.setCliente(compra.cliente());
        orden.setDireccion(compra.direccion());

        // 1. Arma las líneas con el precio REAL de la base de datos
        List<String> sinStock = new ArrayList<>();
        List<Producto> productos = new ArrayList<>();
        for (CompraRequest.Item item : compra.items()) {
            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto " + item.productoId() + " no encontrado"));
            orden.agregarItem(new DetalleOrden(producto.getId(), producto.getNombre(), producto.precioFinal(), item.cantidad()));
            productos.add(producto);
            if (producto.getStock() < item.cantidad()) {
                sinStock.add(producto.getNombre());
            }
        }

        // 2. Calcula subtotal, descuento y total
        calcularTotales(orden);

        // 3. Sin stock suficiente: se guarda como rechazada y NO se toca el stock
        if (!sinStock.isEmpty()) {
            orden.setEstado(Orden.RECHAZADA);
            orden.setMotivo("Stock insuficiente: " + String.join(", ", sinStock));
            return ordenRepository.save(orden);
        }

        // 4. Pago exitoso: descuenta el stock y guarda la orden como pagada
        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            producto.setStock(producto.getStock() - orden.getItems().get(i).getCantidad());
        }
        productoRepository.saveAll(productos);
        orden.setEstado(Orden.PAGADA);
        return ordenRepository.save(orden);
    }

    // Promoción Constructor: 15% en cada línea con 50 o más unidades
    public void calcularTotales(Orden orden) {
        int subtotal = 0;
        int descuento = 0;
        for (DetalleOrden item : orden.getItems()) {
            int linea = item.subtotal();
            subtotal += linea;
            if (item.getCantidad() >= PROMO_MINIMO_UNIDADES) {
                descuento += (int) Math.round(linea * PROMO_DESCUENTO);
            }
        }
        orden.setSubtotal(subtotal);
        orden.setDescuento(descuento);
        orden.setTotal(subtotal - descuento);
    }

    // Número de orden: fecha + correlativo. Ej: 2026112603
    private Long generarNumero(LocalDateTime fecha) {
        String dia = fecha.format(DateTimeFormatter.BASIC_ISO_DATE);
        long correlativo = ordenRepository.count() + 1;
        return Long.parseLong(dia + String.format("%02d", correlativo));
    }
}
