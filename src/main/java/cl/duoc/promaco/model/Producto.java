package cl.duoc.promaco.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Producto del catálogo. Cada producto pertenece a una categoría.
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 1000)
    @Column(length = 1000)
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Integer precio;

    // Si tiene valor, el producto está en oferta y se cobra este precio
    @Min(value = 0, message = "El precio de oferta no puede ser negativo")
    private Integer precioOferta;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    // Cuando el stock llega a este número, el producto aparece como "crítico"
    @NotNull(message = "El stock crítico es obligatorio")
    @Min(value = 0, message = "El stock crítico no puede ser negativo")
    private Integer stockCritico;

    private String imagen;

    // Relación N a 1: muchos productos pertenecen a una categoría.
    // No se envía el objeto completo al frontend, solo su id (ver getCategoriaId).
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Campo que llega desde el frontend (ej: "categoriaId": 1).
    // No se guarda como columna: el servicio lo usa para buscar la Categoria.
    @Transient
    private Long categoriaId;

    public Producto() {
    }

    // Precio que realmente se cobra: el de oferta si existe
    public int precioFinal() {
        return precioOferta != null ? precioOferta : precio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public Integer getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(Integer precioOferta) { this.precioOferta = precioOferta; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getStockCritico() { return stockCritico; }
    public void setStockCritico(Integer stockCritico) { this.stockCritico = stockCritico; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    // En el JSON aparece "categoriaId": el de la categoría asociada
    public Long getCategoriaId() {
        return categoria != null ? categoria.getId() : categoriaId;
    }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
}
