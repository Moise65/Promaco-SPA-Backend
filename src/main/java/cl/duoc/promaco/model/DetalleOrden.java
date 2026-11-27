package cl.duoc.promaco.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Una línea de la orden: qué producto, a qué precio y cuántas unidades.
// Se guarda el nombre y el precio del momento de la compra, para que la
// boleta no cambie aunque después se edite el producto.
@Entity
@Table(name = "detalle_orden")
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    private Long productoId;
    private String nombre;
    private Integer precio;
    private Integer cantidad;

    // Relación N a 1: muchas líneas pertenecen a una orden
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "orden_id")
    private Orden orden;

    public DetalleOrden() {
    }

    public DetalleOrden(Long productoId, String nombre, Integer precio, Integer cantidad) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public int subtotal() {
        return precio * cantidad;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Orden getOrden() { return orden; }
    public void setOrden(Orden orden) { this.orden = orden; }
}
