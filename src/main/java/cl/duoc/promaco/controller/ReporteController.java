package cl.duoc.promaco.controller;

import cl.duoc.promaco.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
// CORS: orígenes que pueden llamar a este controller (frontend en Vite y en EC2).
// Se leen de app.cors.origins → variable CORS_ORIGINS. Para cambiar la IP, se cambia esa variable.
@CrossOrigin(origins = "${app.cors.origins}")
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Resumen de ventas (solo ADMIN)")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/ventas")
    @Operation(summary = "Total vendido, cantidad de órdenes, más vendidos y ventas por categoría")
    public ReporteService.ResumenVentas ventas() {
        return reporteService.resumenVentas();
    }
}
