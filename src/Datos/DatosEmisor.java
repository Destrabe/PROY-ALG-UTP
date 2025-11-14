
package Datos;

public class DatosEmisor {
    private String rucDni;
    private String razonSocial;
    private String nombreComercial;

    public DatosEmisor(String rucDni, String razonSocial, String nombreComercial) {
        this.rucDni = rucDni;
        this.razonSocial = razonSocial;
        this.nombreComercial = nombreComercial;
    }
    
    //Usamos el metodo getter para acceder los datos de forma segura (encapsulamiento)

    public String getRucDni() {
        return rucDni;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }
}
