package Datos;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;

public class ConvertirTxt {

    private String nombreArchivo;

    public ConvertirTxt() {}

    public ConvertirTxt(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    // Genera un archivo TXT a partir de una cola de líneas
    public void generarTXT(ColaArchivo colaArchivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
            while (!colaArchivo.estaVacio()) {
                writer.write(colaArchivo.obtenerLinea());
                writer.newLine();
            }
            System.out.println("Archivo " + nombreArchivo + " se generó correctamente");
        } catch (Exception e) {
            System.out.println("Error al generar el archivo: " + e.getMessage());
        }
    }

    // Obtiene el valor de una celda, evitando errores por índices inválidos o valores nulos
    public String getValorSeguro(JTable tabla, int fila, int columna) {
        if (columna >= tabla.getColumnCount()) return "";
        Object valor = tabla.getValueAt(fila, columna);
        return (valor == null) ? "" : valor.toString();
    }

    // Genera múltiples archivos TXT a partir de una tabla y los guarda en la carpeta indicada
    public void generarTXTTabla(JTable tabla, File carpeta, DatosEmisor emisor) {
        if (carpeta == null) {
            JOptionPane.showMessageDialog(null, "Seleccionar una carpeta de salida primero");
            return;
        }

        String rutaCarpeta = carpeta.getAbsolutePath() + File.separator;

        for (int i = 0; i < tabla.getRowCount(); i++) {
            ColaArchivo cola = new ColaArchivo();

            // Datos principales del comprobante
            String TIPO_DOC = getValorSeguro(tabla, i, 0);
            String SERIE = getValorSeguro(tabla, i, 1);
            String CORRELATIVO = getValorSeguro(tabla, i, 2);
            String FECHA_EMISION = getValorSeguro(tabla, i, 3);
            String T_DOC_CLIE = getValorSeguro(tabla, i, 4);
            String DOC_CLIENTE = getValorSeguro(tabla, i, 5);
            String NOMBRE_CLIENTE = getValorSeguro(tabla, i, 6);
            String DIRECCION_CLIENTE = getValorSeguro(tabla, i, 7);
            String TIPO_OPERACION = getValorSeguro(tabla, i, 8);
            String MONEDA = getValorSeguro(tabla, i, 9);
            String TOT_GRATUITO = getValorSeguro(tabla, i, 10);
            String TOT_GRAVADO = getValorSeguro(tabla, i, 11);
            String IGV = getValorSeguro(tabla, i, 13);
            String TOTAL = getValorSeguro(tabla, i, 14);
            String FORMAPAGO = getValorSeguro(tabla, i, 15);
            String PAGOPENDIENTE = getValorSeguro(tabla, i, 16);
            String COD_DETRAC = getValorSeguro(tabla, i, 18);
            String PORCENTAJE_DETRAC = getValorSeguro(tabla, i, 19);
            String MONTO_DETRAC = getValorSeguro(tabla, i, 20);

            String horaFormateada = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Bloque principal
            cola.agregarLinea("A;CODI_EMPR;;1");
            cola.agregarLinea("A;TipoDTE;;" + TIPO_DOC);
            cola.agregarLinea("A;Serie;;" + SERIE);
            cola.agregarLinea("A;Correlativo;;" + CORRELATIVO);
            cola.agregarLinea("A;FchEmis;;" + FECHA_EMISION);
            cola.agregarLinea("A;HoraEmision;;" + horaFormateada);
            cola.agregarLinea("A;TipoMoneda;;" + MONEDA);
            cola.agregarLinea("A;FormaPago;;" + FORMAPAGO);
            if (FORMAPAGO.equalsIgnoreCase("credito"))
                cola.agregarLinea("A;MontoNetoPendPago;;" + PAGOPENDIENTE);
            cola.agregarLinea("");

            // Emisor
            cola.agregarLinea("A;RUTEmis;;" + emisor.getRucDni());
            cola.agregarLinea("A;TipoRucEmis;;6");
            cola.agregarLinea("A;NomComer;;" + emisor.getNombreComercial());
            cola.agregarLinea("A;RznSocEmis;;" + emisor.getRazonSocial());
            cola.agregarLinea("A;CodigoLocalAnexo;;0000");
            cola.agregarLinea("");

            // Receptor
            cola.agregarLinea("A;TipoRutReceptor;;" + T_DOC_CLIE);
            cola.agregarLinea("A;RUTRecep;;" + DOC_CLIENTE);
            cola.agregarLinea("A;RznSocRecep;;" + NOMBRE_CLIENTE);
            cola.agregarLinea("A;DirRecp;;" + DIRECCION_CLIENTE);
            cola.agregarLinea("");

            // Montos
            cola.agregarLinea("A;MntNeto;;" + TOT_GRAVADO);
            cola.agregarLinea("A;MntExe;;0.00");
            cola.agregarLinea("A;MntExo;;0.00");
            cola.agregarLinea("A;MntTotal;;" + TOT_GRATUITO);
            cola.agregarLinea("A;MnTotal;;" + TOTAL);
            cola.agregarLinea("A;TipoOperacion;;" + TIPO_OPERACION);
            cola.agregarLinea("");

            // Impuestos
            cola.agregarLinea("A2;CodigoImpuesto;1;1000");
            cola.agregarLinea("A2;MontoImpuesto;1;" + IGV);
            cola.agregarLinea("A2;MontoImpuestoBase;1;" + TOT_GRAVADO);
            cola.agregarLinea("A2;TasaImpuesto;1;18");
            cola.agregarLinea("");

            // Detracción
            if (TIPO_OPERACION.equalsIgnoreCase("1001")) {
                cola.agregarLinea("A3;codiDetraccion;1;" + COD_DETRAC);
                cola.agregarLinea("A3;valorDetraccion;1;-");
                cola.agregarLinea("A3;MntDetraccion;1;" + MONTO_DETRAC);
                cola.agregarLinea("A3;PorcentajeDetraccion;1;" + PORCENTAJE_DETRAC);
                cola.agregarLinea("");
            }

            // Pago a crédito
            if (FORMAPAGO.equalsIgnoreCase("credito")) {
                LocalDate fechaVencimiento = LocalDate.parse(FECHA_EMISION).plusDays(30);
                cola.agregarLinea("A5;Cuota;1;1");
                cola.agregarLinea("A5;MontoCuota;1;" + PAGOPENDIENTE);
                cola.agregarLinea("A5;FechaVencCuota;1;" + fechaVencimiento.format(DateTimeFormatter.ISO_LOCAL_DATE));
                cola.agregarLinea("");
            }

            // Items
            final int COL_SALTO = 13;
            final int COL_QTY = 37, COL_COD = 38, COL_NOMBRE = 39, COL_PRECIO = 41, COL_PRECIO_SIN_IGV = 42;
            final int COL_EXENTO = 43, COL_IGV = 44, COL_MONTO = 45;

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setDecimalSeparator('.');
            DecimalFormat df5 = new DecimalFormat("0.00000", symbols);
            DecimalFormat df2 = new DecimalFormat("0.00", symbols);

            int nroLinea = 1;

            for (int j = 0; j < 10; j++) {
                int offset = j * COL_SALTO;
                String qty = getValorSeguro(tabla, i, COL_QTY + offset);
                String cod = getValorSeguro(tabla, i, COL_COD + offset);

                if (!qty.isEmpty() && !cod.isEmpty()) {
                    BigDecimal precio = new BigDecimal(getValorSeguro(tabla, i, COL_PRECIO + offset));
                    BigDecimal precioSinIgv = new BigDecimal(getValorSeguro(tabla, i, COL_PRECIO_SIN_IGV + offset));
                    BigDecimal monto = new BigDecimal(getValorSeguro(tabla, i, COL_MONTO + offset));

                    cola.agregarLinea("B;NroLinDet;" + nroLinea + ";" + nroLinea);
                    cola.agregarLinea("B;QtyItem;" + nroLinea + ";" + qty);
                    cola.agregarLinea("B;UnmdItem;" + nroLinea + ";NIU");
                    cola.agregarLinea("B;VlrCodigo;" + nroLinea + ";" + cod);
                    cola.agregarLinea("B;NmbItem;" + nroLinea + ";" + getValorSeguro(tabla, i, COL_NOMBRE + offset));
                    cola.agregarLinea("B;PrcItem;" + nroLinea + ";" + df5.format(precio));
                    cola.agregarLinea("B;PrcItemSinIgv;" + nroLinea + ";" + df5.format(precioSinIgv));
                    cola.agregarLinea("B;MontoItem;" + nroLinea + ";" + df2.format(monto));
                    cola.agregarLinea("B;IndExe;" + nroLinea + ";" + getValorSeguro(tabla, i, COL_EXENTO + offset));
                    cola.agregarLinea("B;CodigoTipoIgv;" + nroLinea + ";1000");
                    cola.agregarLinea("B;TasaIgv;" + nroLinea + ";18");
                    cola.agregarLinea("B;ImpuestoIgv;" + nroLinea + ";" + getValorSeguro(tabla, i, COL_IGV + offset));
                    cola.agregarLinea("");

                    nroLinea++;
                } else {
                    break;
                }
            }

            // Generar archivo TXT con nombre estructurado
            String rutaArchivo = rutaCarpeta + TIPO_DOC + "-" + SERIE + "-" + CORRELATIVO + ".txt";
            new ConvertirTxt(rutaArchivo).generarTXT(cola);
        }

        JOptionPane.showMessageDialog(null, "Proceso completado");
    }
}