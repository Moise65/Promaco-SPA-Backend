package cl.duoc.promaco.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Datos del cliente tal como se escribieron en el checkout.
// @Embeddable: se guardan como columnas dentro de la tabla "ordenes".
@Embeddable
public class ClienteOrden {

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "cliente_nombre")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(name = "cliente_apellidos")
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Column(name = "cliente_correo")
    private String correo;

    public ClienteOrden() {
    }

    public ClienteOrden(String nombre, String apellidos, String correo) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
