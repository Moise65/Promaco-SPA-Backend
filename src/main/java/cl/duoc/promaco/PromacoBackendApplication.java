package cl.duoc.promaco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del backend de Promaco SpA.
// Al arrancar levanta la API REST en http://localhost:8080
// y la documentación Swagger en http://localhost:8080/swagger-ui.html
@SpringBootApplication
public class PromacoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromacoBackendApplication.class, args);
    }
}
