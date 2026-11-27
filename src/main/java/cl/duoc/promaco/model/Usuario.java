package cl.duoc.promaco.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Usuario del sistema. El campo "rol" define qué puede hacer:
//  - admin:   administra productos, categorías, usuarios, órdenes y reportes
//  - cliente: compra en la tienda y ve sus propias compras
@Entity
@Table(name = "usuarios")
public class Usuario {

    public static final String ROL_ADMIN = "admin";
    public static final String ROL_CLIENTE = "cliente";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    private String apellidos;

    @Size(max = 12)
    private String rut;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    // La contraseña se guarda ENCRIPTADA con BCrypt.
    // WRITE_ONLY: se puede recibir en el JSON, pero nunca se devuelve.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String clave;

    @Pattern(regexp = "admin|cliente", message = "El rol debe ser admin o cliente")
    @Column(nullable = false, length = 20)
    private String rol = ROL_CLIENTE;

    private String region;
    private String comuna;
    private String direccion;

    public Usuario() {
    }

    public boolean esAdmin() {
        return ROL_ADMIN.equals(rol);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
