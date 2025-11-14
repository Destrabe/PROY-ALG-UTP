package Datos;
import java.util.*;

public class ColaArchivo {
    private Queue<String> cola;

    public ColaArchivo() {
        cola = new LinkedList<>();
    }
    //Metodo para agregar linea por linea
    public void agregarLinea(String linea){
        cola.add(linea);
    }
    
    public boolean estaVacio(){
        return cola.isEmpty();
    }
    
    //Obtener los primeros elementos e ir eliminando 
   public String ObtenerLista(){
       return cola.poll();
   }

    public Queue<String> getCola() {
        return cola;
    }
   
}
