package cl.duoc.promaco.dto;

import cl.duoc.promaco.model.ClienteOrden;
import cl.duoc.promaco.model.DireccionEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Lo que envía el checkout al pagar.
// Solo viajan el id y la cantidad: los precios los calcula el backend,
// así el cliente no puede cambiarlos desde el navegador.
public record CompraRequest(
        @NotNull(message = "Faltan los datos del cliente") @Valid ClienteOrden cliente,
        @NotNull(message = "Falta la dirección de entrega") @Valid DireccionEntrega direccion,
        @NotEmpty(message = "El carrito está vacío") List<@Valid Item> items) {

    public record Item(
            @NotNull(message = "Falta el producto") Long productoId,
            @NotNull @Min(value = 1, message = "La cantidad mínima es 1") Integer cantidad) {
    }
}
