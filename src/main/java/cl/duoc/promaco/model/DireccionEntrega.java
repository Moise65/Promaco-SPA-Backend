package cl.duoc.promaco.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

// Dirección de despacho de una orden (se guarda dentro de la tabla "ordenes").
@Embeddable
public class DireccionEntrega {

    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    private String departamento;

    @NotBlank(message = "La región es obligatoria")
    private String region;

    @NotBlank(message = "La comuna es obligatoria")
    private String comuna;

    private String indicaciones;

    public DireccionEntrega() {
    }

    public DireccionEntrega(String calle, String departamento, String region, String comuna, String indicaciones) {
        this.calle = calle;
        this.departamento = departamento;
        this.region = region;
        this.comuna = comuna;
        this.indicaciones = indicaciones;
    }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
}
