package Datos;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Cola para almacenar líneas de texto.
 */
public class ColaArchivo {

    private Queue<String> cola;

    // Constructor: inicializa la cola
    public ColaArchivo() {
        cola = new LinkedList<>();
    }

    // Verifica si la cola está vacía
    public boolean estaVacio() {
        return cola.isEmpty();
    }

    // Agrega una línea a la cola
    public void agregarLinea(String linea) {
        cola.add(linea);
    }

    // Extrae y elimina la primera línea
    public String obtenerLinea() {
        return cola.poll();
    }

    // Devuelve la cola completa
    public Queue<String> getCola() {
        return cola;
    }
}