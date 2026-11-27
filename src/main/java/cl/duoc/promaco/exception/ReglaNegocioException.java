package cl.duoc.promaco.exception;

// Se lanza cuando una operación no cumple una regla del negocio,
// por ejemplo un correo repetido o borrar una categoría con productos (responde 409)
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
