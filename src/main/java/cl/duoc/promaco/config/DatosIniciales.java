package cl.duoc.promaco.config;

import cl.duoc.promaco.model.Categoria;
import cl.duoc.promaco.model.ClienteOrden;
import cl.duoc.promaco.model.DetalleOrden;
import cl.duoc.promaco.model.DireccionEntrega;
import cl.duoc.promaco.model.Orden;
import cl.duoc.promaco.model.Producto;
import cl.duoc.promaco.model.Usuario;
import cl.duoc.promaco.repository.CategoriaRepository;
import cl.duoc.promaco.repository.OrdenRepository;
import cl.duoc.promaco.repository.ProductoRepository;
import cl.duoc.promaco.repository.UsuarioRepository;
import cl.duoc.promaco.service.OrdenService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Carga los datos de ejemplo la PRIMERA vez que se arranca con la base vacía
// (los mismos que tenía la entrega 2 en datosIniciales.js).
@Component
public class DatosIniciales implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrdenRepository ordenRepository;
    private final OrdenService ordenService;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
                          UsuarioRepository usuarioRepository, OrdenRepository ordenRepository,
                          OrdenService ordenService, PasswordEncoder passwordEncoder) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ordenRepository = ordenRepository;
        this.ordenService = ordenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return; // la base ya tiene datos
        }

        Categoria cemento = categoriaRepository.save(new Categoria("Cemento y Áridos", "Cemento, arena, gravilla y materiales para obra gruesa.", "bi-bricks"));
        Categoria herramientas = categoriaRepository.save(new Categoria("Herramientas Eléctricas", "Taladros, esmeriles, sierras y equipos a batería.", "bi-tools"));
        Categoria pinturas = categoriaRepository.save(new Categoria("Pinturas", "Látex, esmaltes y productos para terminaciones.", "bi-palette"));
        Categoria fijaciones = categoriaRepository.save(new Categoria("Fijaciones y Ferretería", "Tornillos, clavos y elementos de fijación.", "bi-nut"));
        Categoria maderas = categoriaRepository.save(new Categoria("Maderas y Tableros", "Madera dimensionada, OSB y tableros.", "bi-tree"));

        Producto p1 = producto("CEM-001", "Cemento Polpaico Especial 25kg", cemento, 5990, null, 320, 20, "/img/cemento.jpg",
                "Cemento de albañilería y uso general de alta resistencia inicial, diseñado bajo estrictos estándares industriales.");
        Producto p2 = producto("HER-001", "Taladro Percutor Inalámbrico 20V", herramientas, 129990, 109990, 14, 5, "/img/taladro_inalambrico.jpg",
                "Taladro percutor a batería de ion de litio de 20V, ideal para faenas en concreto, madera y metal.");
        Producto p3 = producto("CEM-002", "Arena Gruesa Saco 25kg", cemento, 2490, null, 500, 30, "/img/productos/arena.svg",
                "Arena gruesa lavada, apta para hormigones y morteros de pega.");
        producto("CEM-003", "Gravilla Saco 25kg", cemento, 2690, null, 4, 10, "/img/productos/gravilla.svg",
                "Gravilla de tamaño uniforme para hormigones, radieres y drenajes.");
        producto("HER-002", "Esmeril Angular 4 1/2\" 850W", herramientas, 49990, 42990, 8, 5, "/img/productos/esmeril.svg",
                "Esmeril angular de 850W para corte y desbaste de metal y albañilería.");
        producto("HER-003", "Sierra Circular 7 1/4\" 1400W", herramientas, 89990, null, 3, 5, "/img/productos/sierra.svg",
                "Sierra circular con guía paralela y ajuste de profundidad para cortes precisos en madera.");
        Producto p7 = producto("PIN-001", "Pintura Látex Blanca 1 Galón", pinturas, 15990, 12990, 60, 10, "/img/productos/latex.svg",
                "Látex lavable de alto rendimiento para interiores y exteriores.");
        Producto p8 = producto("PIN-002", "Esmalte Sintético Gris 1/4 Galón", pinturas, 8990, null, 35, 8, "/img/productos/esmalte.svg",
                "Esmalte sintético brillante para metal y madera, con protección anticorrosiva.");
        Producto p9 = producto("FER-001", "Tornillo Volcanita 6x1\" Caja 100u", fijaciones, 3290, null, 200, 25, "/img/productos/tornillos.svg",
                "Tornillos punta fina para fijar planchas de yeso-cartón a perfiles metálicos.");
        producto("FER-002", "Clavo Corriente 3\" 1kg", fijaciones, 2190, 1790, 150, 20, "/img/productos/clavos.svg",
                "Clavos de acero corriente para carpintería y moldajes.");
        producto("MAD-001", "Pino Dimensionado 2x4\" 3,2m", maderas, 4590, null, 90, 15, "/img/productos/pino.svg",
                "Pino seco dimensionado para tabiquería y estructuras livianas.");
        producto("MAD-002", "Plancha OSB 9,5mm 1,22x2,44m", maderas, 11990, null, 2, 6, "/img/productos/osb.svg",
                "Tablero de virutas orientadas para revestimientos, pisos y techumbres.");

        // Contraseñas encriptadas con BCrypt
        usuario("Mathias", "Soto", "19011022K", "mathias@duoc.cl", "admin123", Usuario.ROL_ADMIN, "Santiago", "Av. Libertador 1234");
        usuario("Moise", "Fonseca", "20111333K", "moise@duoc.cl", "admin123", Usuario.ROL_ADMIN, "Maipú", "Pajaritos 4500");
        usuario("Camilo", "Godoy", "20555666", "camilo@duoc.cl", "admin123", Usuario.ROL_ADMIN, "Puente Alto", "Concha y Toro 980");
        Usuario pedro = usuario("Pedro", "Constructor", "15444777", "cliente@gmail.com", "cliente1", Usuario.ROL_CLIENTE, "Cerrillos", "Los Crisantemos 603");

        // Órdenes de ejemplo para los reportes
        String rm = "Región Metropolitana de Santiago";
        orden(20261001L, LocalDateTime.of(2026, 10, 1, 10, 15), pedro,
                new ClienteOrden("Pedro", "Constructor", "cliente@gmail.com"),
                new DireccionEntrega("Los Crisantemos 603", "", rm, "Cerrillos", ""),
                List.of(new DetalleOrden(p1.getId(), p1.getNombre(), 5990, 60), new DetalleOrden(p3.getId(), p3.getNombre(), 2490, 40)));
        orden(20261008L, LocalDateTime.of(2026, 10, 8, 17, 40), pedro,
                new ClienteOrden("Pedro", "Constructor", "cliente@gmail.com"),
                new DireccionEntrega("Los Crisantemos 603", "", rm, "Cerrillos", "Dejar con conserje"),
                List.of(new DetalleOrden(p2.getId(), p2.getNombre(), 109990, 1), new DetalleOrden(p9.getId(), p9.getNombre(), 3290, 3)));
        orden(20261015L, LocalDateTime.of(2026, 10, 15, 9, 5), null,
                new ClienteOrden("Ana", "Pérez", "ana.perez@gmail.com"),
                new DireccionEntrega("Pedro de Valdivia 2100", "Depto 41", "Región de Valparaíso", "Viña del Mar", ""),
                List.of(new DetalleOrden(p7.getId(), p7.getNombre(), 12990, 4), new DetalleOrden(p8.getId(), p8.getNombre(), 8990, 2)));
    }

    private Producto producto(String codigo, String nombre, Categoria categoria, int precio, Integer precioOferta,
                              int stock, int stockCritico, String imagen, String descripcion) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setPrecio(precio);
        p.setPrecioOferta(precioOferta);
        p.setStock(stock);
        p.setStockCritico(stockCritico);
        p.setImagen(imagen);
        p.setDescripcion(descripcion);
        return productoRepository.save(p);
    }

    private Usuario usuario(String nombre, String apellidos, String rut, String correo, String clave,
                            String rol, String comuna, String direccion) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setRut(rut);
        u.setCorreo(correo);
        u.setClave(passwordEncoder.encode(clave));
        u.setRol(rol);
        u.setRegion("Región Metropolitana de Santiago");
        u.setComuna(comuna);
        u.setDireccion(direccion);
        return usuarioRepository.save(u);
    }

    private void orden(Long numero, LocalDateTime fecha, Usuario usuario, ClienteOrden cliente,
                       DireccionEntrega direccion, List<DetalleOrden> items) {
        Orden orden = new Orden();
        orden.setNumero(numero);
        orden.setFecha(fecha);
        orden.setUsuario(usuario);
        orden.setCliente(cliente);
        orden.setDireccion(direccion);
        orden.setEstado(Orden.PAGADA);
        items.forEach(orden::agregarItem);
        ordenService.calcularTotales(orden);
        ordenRepository.save(orden);
    }
}
