package cl.duoc.promaco;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas de INTEGRACIÓN: levantan la API completa con una base H2 en memoria
// y comprueban la seguridad con JWT y roles, como lo haría el frontend.
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String PRODUCTO_NUEVO = """
            {"codigo":"TEST-001","nombre":"Producto de prueba","precio":1000,"stock":10,
             "stockCritico":2,"categoriaId":1}
            """;

    private String token(String correo, String clave) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(respuesta);
        return json.get("token").asText();
    }

    @Test
    void elCatalogoEsPublico() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("CEM-001"));
    }

    @Test
    void loginCorrectoDevuelveTokenYUsuarioSinClave() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"mathias@duoc.cl\",\"clave\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("admin"))
                .andExpect(jsonPath("$.usuario.clave").doesNotExist());
    }

    @Test
    void loginConClaveIncorrectaResponde401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"mathias@duoc.cl\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearProductoSinTokenResponde401() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unClienteNoPuedeCrearProductos403() throws Exception {
        String token = token("cliente@gmail.com", "cliente1");
        mvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isForbidden());
    }

    @Test
    void unAdminSiPuedeCrearProductos() throws Exception {
        String token = token("mathias@duoc.cl", "admin123");
        mvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.categoriaId").value(1));
    }

    @Test
    void unTokenAlteradoResponde401() throws Exception {
        String token = token("cliente@gmail.com", "cliente1") + "x";
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unClienteNoPuedeVerLosUsuarios403() throws Exception {
        String token = token("cliente@gmail.com", "cliente1");
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
