package cl.duoc.promaco;

import cl.duoc.promaco.dto.CompraRequest;
import cl.duoc.promaco.model.ClienteOrden;
import cl.duoc.promaco.model.DireccionEntrega;
import cl.duoc.promaco.model.Orden;
import cl.duoc.promaco.model.Producto;
import cl.duoc.promaco.repository.OrdenRepository;
import cl.duoc.promaco.repository.ProductoRepository;
import cl.duoc.promaco.service.OrdenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas UNITARIAS de la lógica de compra.
// Los repositorios se reemplazan por mocks (Mockito): no se usa la base de datos.
class OrdenServiceTest {

    private ProductoRepository productoRepository;
    private OrdenRepository ordenRepository;
    private OrdenService ordenService;
    private Producto cemento;

    @BeforeEach
    void preparar() {
        productoRepository = mock(ProductoRepository.class);
        ordenRepository = mock(OrdenRepository.class);
        ordenService = new OrdenService(ordenRepository, productoRepository);

        cemento = new Producto();
        cemento.setId(1L);
        cemento.setNombre("Cemento 25kg");
        cemento.setPrecio(1000);
        cemento.setStock(100);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(cemento));
        // save devuelve la misma orden que recibe
        when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CompraRequest compra(int cantidad) {
        return new CompraRequest(
                new ClienteOrden("Pedro", "Constructor", "cliente@gmail.com"),
                new DireccionEntrega("Los Crisantemos 603", "", "Región Metropolitana de Santiago", "Cerrillos", ""),
                List.of(new CompraRequest.Item(1L, cantidad)));
    }

    @Test
    void aplica15PorCientoDeDescuentoDesde50Unidades() {
        Orden orden = ordenService.procesarCompra(compra(50), null);

        assertEquals(50000, orden.getSubtotal());
        assertEquals(7500, orden.getDescuento());
        assertEquals(42500, orden.getTotal());
    }

    @Test
    void noAplicaDescuentoConMenosDe50Unidades() {
        Orden orden = ordenService.procesarCompra(compra(49), null);

        assertEquals(0, orden.getDescuento());
        assertEquals(49000, orden.getTotal());
    }

    @Test
    void compraConStockSeGuardaComoPagadaYDescuentaElStock() {
        Orden orden = ordenService.procesarCompra(compra(30), null);

        assertEquals(Orden.PAGADA, orden.getEstado());
        assertEquals(70, cemento.getStock());
        verify(productoRepository).saveAll(anyList());
    }

    @Test
    void compraSinStockSeRechazaYNoTocaElStock() {
        Orden orden = ordenService.procesarCompra(compra(150), null);

        assertEquals(Orden.RECHAZADA, orden.getEstado());
        assertTrue(orden.getMotivo().contains("Cemento 25kg"));
        assertEquals(100, cemento.getStock());
        verify(productoRepository, never()).saveAll(anyList());
    }

    @Test
    void usaElPrecioDeOfertaCuandoExiste() {
        cemento.setPrecioOferta(800);

        Orden orden = ordenService.procesarCompra(compra(10), null);

        assertEquals(800, orden.getItems().get(0).getPrecio());
        assertEquals(8000, orden.getTotal());
    }
}
