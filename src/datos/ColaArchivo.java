package datos;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Estructura tipo cola para manejar cadenas de texto.
 */
public class ColaArchivo {

    private Queue<String> cola;

    // Constructor: prepara la cola interna
    public ColaArchivo() {
        cola = new LinkedList<>();
    }

    // Indica si la cola no contiene elementos
    public boolean estaVacia() {
        return cola.isEmpty();
    }

    // Inserta una nueva línea en la cola
    public void encolarLinea(String contenido) {
        cola.add(contenido);
    }

    // Obtiene y elimina la primera línea disponible
    public String desencolarLinea() {
        return cola.poll();
    }

    // Devuelve la referencia completa de la cola
    public Queue<String> getCola() {
        return cola;
    }
}
