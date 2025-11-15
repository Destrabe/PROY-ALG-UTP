package datos;

/**
 * Contiene la información del emisor de un documento.
 */
public class InfoEmisor {

    private String identificacionFiscal;
    private String razonSocial;
    private String nombreComercial;

    // Constructor con parámetros requeridos
    public InfoEmisor(String identificacionFiscal, String razonSocial, String nombreComercial) {
        this.identificacionFiscal = identificacionFiscal;
        this.razonSocial = razonSocial;
        this.nombreComercial = nombreComercial;
    }

    // Métodos de acceso (getters)
    public String getRucDni() {
        return identificacionFiscal;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }
}
