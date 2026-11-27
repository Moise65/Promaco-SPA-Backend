package cl.duoc.promaco.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Orden de compra. Puede quedar "pagada" o "rechazada" (por falta de stock).
@Entity
@Table(name = "ordenes")
public class Orden {

    public static final String PAGADA = "pagada";
    public static final String RECHAZADA = "rechazada";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Número visible para el cliente: fecha + correlativo (ej: 2026112601)
    private Long numero;

    private LocalDateTime fecha;

    private String estado;

    // Motivo del rechazo (solo si estado = rechazada)
    private String motivo;

    // Usuario que compró (relación N a 1). No se envía el objeto, solo su id.
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Embedded
    private ClienteOrden cliente;

    @Embedded
    private DireccionEntrega direccion;

    // Relación 1 a N: una orden tiene muchas líneas de detalle.
    // cascade ALL: al guardar la orden se guardan también sus líneas.
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleOrden> items = new ArrayList<>();

    private Integer subtotal;
    private Integer descuento;
    private Integer total;

    public Orden() {
    }

    // Agrega una línea y la enlaza con esta orden
    public void agregarItem(DetalleOrden item) {
        item.setOrden(this);
        items.add(item);
    }

    public Long getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNumero() { return numero; }
    public void setNumero(Long numero) { this.numero = numero; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public ClienteOrden getCliente() { return cliente; }
    public void setCliente(ClienteOrden cliente) { this.cliente = cliente; }

    public DireccionEntrega getDireccion() { return direccion; }
    public void setDireccion(DireccionEntrega direccion) { this.direccion = direccion; }

    public List<DetalleOrden> getItems() { return items; }
    public void setItems(List<DetalleOrden> items) { this.items = items; }

    public Integer getSubtotal() { return subtotal; }
    public void setSubtotal(Integer subtotal) { this.subtotal = subtotal; }

    public Integer getDescuento() { return descuento; }
    public void setDescuento(Integer descuento) { this.descuento = descuento; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
}
