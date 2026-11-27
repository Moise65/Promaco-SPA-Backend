package cl.duoc.promaco.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// Configuración de seguridad: qué rutas son públicas y qué rol necesita cada una.
@Configuration
public class SecurityConfig {

    @Value("${app.cors.origins}")
    private String corsOrigins;

    // Las contraseñas se guardan y comparan con BCrypt (hash con "sal")
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring arma el AuthenticationManager con nuestro UsuarioDetailsService y BCrypt
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                // STATELESS: el servidor no guarda sesiones; cada petición trae su token
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Público: login, registro y Swagger
                        .requestMatchers("/api/auth/login", "/api/auth/registro").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Catálogo: cualquiera puede VER productos y categorías...
                        .requestMatchers(HttpMethod.GET, "/api/productos/criticos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**").permitAll()
                        // ...pero solo el ADMIN puede crear, editar o eliminar
                        .requestMatchers("/api/productos/**", "/api/categorias/**").hasRole("ADMIN")

                        // Compras: hay que iniciar sesión (cliente o admin)
                        .requestMatchers(HttpMethod.POST, "/api/ordenes").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/ordenes/mias").hasAnyRole("CLIENTE", "ADMIN")

                        // Panel de administración
                        .requestMatchers("/api/ordenes/**", "/api/usuarios/**", "/api/reportes/**").hasRole("ADMIN")

                        // Todo lo demás (ej: /api/auth/me, /api/auth/renovar) requiere token
                        .anyRequest().authenticated())
                // Respuestas JSON cuando falta el token (401) o falta el rol (403)
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"estado\":401,\"mensaje\":\"Debes iniciar sesión\"}");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"estado\":403,\"mensaje\":\"No tienes permisos para esta acción\"}");
                        }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // CORS: permite que el frontend (http://localhost:5173) llame a la API
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(corsOrigins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
