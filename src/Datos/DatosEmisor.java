package Datos;

/**
 * Representa los datos del emisor de un comprobante.
 */
public class DatosEmisor {

    private String rucDni;
    private String razonSocial;
    private String nombreComercial;

    // Constructor con datos obligatorios
    public DatosEmisor(String rucDni, String razonSocial, String nombreComercial) {
        this.rucDni = rucDni;
        this.razonSocial = razonSocial;
        this.nombreComercial = nombreComercial;
    }

    // Getters para acceder a los atributos
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