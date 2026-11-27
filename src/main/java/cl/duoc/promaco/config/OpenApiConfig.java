package cl.duoc.promaco.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Documentación Swagger: http://localhost:8080/swagger-ui.html
// Agrega el botón "Authorize" para pegar el token JWT y probar las rutas protegidas.
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI promacoOpenApi() {
        String esquema = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("API Promaco SpA")
                        .version("1.0.0")
                        .description("Tienda de materiales de construcción: productos, categorías, usuarios, "
                                + "órdenes y reportes. Autenticación con JWT y roles ADMIN / CLIENTE."))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema, new SecurityScheme()
                        .name(esquema)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
