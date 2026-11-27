package cl.duoc.promaco.service;

import cl.duoc.promaco.dto.RegistroRequest;
import cl.duoc.promaco.exception.RecursoNoEncontradoException;
import cl.duoc.promaco.exception.ReglaNegocioException;
import cl.duoc.promaco.model.Usuario;
import cl.duoc.promaco.repository.OrdenRepository;
import cl.duoc.promaco.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Lógica de negocio de los usuarios
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final OrdenRepository ordenRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, OrdenRepository ordenRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.ordenRepository = ordenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    public Usuario obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    // Registro desde la tienda: siempre queda como cliente
    public Usuario registrar(RegistroRequest datos) {
        Usuario usuario = new Usuario();
        usuario.setNombre(datos.nombre());
        usuario.setApellidos(datos.apellidos());
        usuario.setRut(datos.rut());
        usuario.setCorreo(datos.correo());
        usuario.setClave(datos.clave());
        usuario.setRegion(datos.region());
        usuario.setComuna(datos.comuna());
        usuario.setDireccion(datos.direccion());
        usuario.setRol(Usuario.ROL_CLIENTE);
        return crear(usuario);
    }

    // Creación desde el panel admin (puede elegir el rol)
    public Usuario crear(Usuario usuario) {
        String correo = usuario.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ese correo ya está registrado");
        }
        if (usuario.getClave() == null || usuario.getClave().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria");
        }
        usuario.setId(null);
        usuario.setCorreo(correo);
        usuario.setClave(passwordEncoder.encode(usuario.getClave())); // se guarda encriptada
        if (usuario.getRol() == null) usuario.setRol(Usuario.ROL_CLIENTE);
        return usuarioRepository.save(usuario);
    }

    // Si no viene contraseña nueva, se mantiene la actual
    public Usuario actualizar(Long id, Usuario datos) {
        Usuario existente = obtener(id);
        String correo = datos.getCorreo().trim().toLowerCase();
        usuarioRepository.findByCorreoIgnoreCase(correo)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ReglaNegocioException("Ese correo ya está registrado");
                });
        existente.setNombre(datos.getNombre());
        existente.setApellidos(datos.getApellidos());
        existente.setRut(datos.getRut());
        existente.setCorreo(correo);
        existente.setRegion(datos.getRegion());
        existente.setComuna(datos.getComuna());
        existente.setDireccion(datos.getDireccion());
        if (datos.getRol() != null) existente.setRol(datos.getRol());
        if (datos.getClave() != null && !datos.getClave().isBlank()) {
            existente.setClave(passwordEncoder.encode(datos.getClave()));
        }
        return usuarioRepository.save(existente);
    }

    // Regla: un admin no puede eliminar su propia cuenta.
    // Sus órdenes se conservan (guardan los datos del cliente), pero quedan sin usuario.
    @Transactional
    public void eliminar(Long id, String correoActual) {
        Usuario usuario = obtener(id);
        if (usuario.getCorreo().equalsIgnoreCase(correoActual)) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta");
        }
        ordenRepository.findByUsuario_IdOrderByFechaDesc(id).forEach(orden -> orden.setUsuario(null));
        usuarioRepository.delete(usuario);
    }
}
