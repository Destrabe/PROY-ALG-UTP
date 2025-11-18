package datos;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;


 //Clase para crear archivos TXT desde una JTable.
public class ConvertirTxt {

    private String archivoDestino;

    public ConvertirTxt() {}

    public ConvertirTxt(String archivoDestino) {
        this.archivoDestino = archivoDestino;
    }

    // Método auxiliar: devuelve el valor seguro de una celda
    public String obtenerValor(JTable tabla, int fila, int columna) {
        if (columna >= tabla.getColumnCount()) return "";
        Object valor = tabla.getValueAt(fila, columna);
        return (valor == null) ? "" : valor.toString();
    }

    // Genera un archivo TXT a partir de la cola de datos
    public void generarTXT(ColaArchivo colaArchivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoDestino))) {
            while (!colaArchivo.estaVacia()) {
                writer.write(colaArchivo.desencolarLinea()); 
                writer.newLine();
            }
            System.out.println("Archivo " + archivoDestino + " creado correctamente");
        } catch (Exception e) {
            System.out.println("Error al crear el archivo: " + e.getMessage());
        }
    }
    // Genera múltiples archivos TXT desde la tabla
    public void generarTXTTabla(JTable tabla, File carpeta, InfoEmisor emisor) {
        if (carpeta == null) {
            JOptionPane.showMessageDialog(null, "Seleccione una carpeta de salida primero");
            return;
        }

        String carpetaDestino = carpeta.getAbsolutePath() + File.separator;

        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            ColaArchivo cola = new ColaArchivo();

            // Datos principales del comprobante
            String tipoDocumento   = obtenerValor(tabla, fila, 0);
            String serieDocumento  = obtenerValor(tabla, fila, 1);
            String numeroCorrelativo = obtenerValor(tabla, fila, 2);
            String fechaEmision    = obtenerValor(tabla, fila, 3);
            String tipoDocCliente  = obtenerValor(tabla, fila, 4);
            String docCliente      = obtenerValor(tabla, fila, 5);
            String nombreCliente   = obtenerValor(tabla, fila, 6);
            String direccionCliente = obtenerValor(tabla, fila, 7);
            String tipoOperacion   = obtenerValor(tabla, fila, 8);
            String monedaDocumento = obtenerValor(tabla, fila, 9);
            String montoGratuito   = obtenerValor(tabla, fila, 10);
            String montoGravado    = obtenerValor(tabla, fila, 11);
            String igvTotal        = obtenerValor(tabla, fila, 13);
            String montoTotal      = obtenerValor(tabla, fila, 14);
            String formaDePago     = obtenerValor(tabla, fila, 15);
            String saldoPendiente  = obtenerValor(tabla, fila, 16);
            String codigoDetraccion = obtenerValor(tabla, fila, 18);
            String porcentajeDetraccion = obtenerValor(tabla, fila, 19);
            String montoDetraccion = obtenerValor(tabla, fila, 20);
            String cuentaBanco     = obtenerValor(tabla, fila, 21);

            String horaActualStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            // Bloque principal del comprobante
            cola.encolarLinea("A;CODI_EMPR;;1");
            cola.encolarLinea("A;TipoDTE;;" + tipoDocumento);
            cola.encolarLinea("A;Serie;;" + serieDocumento);
            cola.encolarLinea("A;Correlativo;;" + numeroCorrelativo);
            cola.encolarLinea("A;FchEmis;;" + fechaEmision);
            cola.encolarLinea("A;HoraEmision;;" + horaActualStr);
            cola.encolarLinea("A;TipoMoneda;;" + monedaDocumento);
            cola.encolarLinea("A;FormaPago;;" + formaDePago);
            if (formaDePago.equalsIgnoreCase("credito"))
                cola.encolarLinea("A;MontoNetoPendPago;;" + saldoPendiente);
            cola.encolarLinea("");

            // Datos del emisor
            cola.encolarLinea("A;RUTEmis;;" + emisor.getRucDni());
            cola.encolarLinea("A;TipoRucEmis;;6");
            cola.encolarLinea("A;NomComer;;" + emisor.getNombreComercial());
            cola.encolarLinea("A;RznSocEmis;;" + emisor.getRazonSocial());
            cola.encolarLinea("A;CodigoLocalAnexo;;0000");
            cola.encolarLinea("");

            // Datos del receptor
            cola.encolarLinea("A;TipoRutReceptor;;" + tipoDocCliente);
            cola.encolarLinea("A;RUTRecep;;" + docCliente);
            cola.encolarLinea("A;RznSocRecep;;" + nombreCliente);
            cola.encolarLinea("A;DirRecp;;" + direccionCliente);
            cola.encolarLinea("");

            // Totales y operación
            cola.encolarLinea("A;MntNeto;;" + montoGravado);
            cola.encolarLinea("A;MntExe;;0.00");
            cola.encolarLinea("A;MntExo;;0.00");
            cola.encolarLinea("A;MntTotal;;" + montoGratuito);
            cola.encolarLinea("A;MnTotal;;" + montoTotal);
            cola.encolarLinea("A;TipoOperacion;;" + tipoOperacion);
            cola.encolarLinea("");

            // Impuestos aplicados
            cola.encolarLinea("A2;CodigoImpuesto;1;1000");
            cola.encolarLinea("A2;MontoImpuesto;1;" + igvTotal);
            cola.encolarLinea("A2;MontoImpuestoBase;1;" + montoGravado);
            cola.encolarLinea("A2;TasaImpuesto;1;18");
            cola.encolarLinea("");

            // Detracción con lógica refinada
            if (tipoOperacion.equalsIgnoreCase("1001")) {
                int indice = codigoDetraccion.equalsIgnoreCase("2003") ? 1 :
                             codigoDetraccion.equalsIgnoreCase("3001") ? 2 : 3;

                String valorDetr = codigoDetraccion.equalsIgnoreCase("2003") ? "-" :
                                   codigoDetraccion.equalsIgnoreCase("3001") ? cuentaBanco : "025";

                String montoDetr = codigoDetraccion.equalsIgnoreCase("2003") ? montoDetraccion : "0";
                String porcDetr  = codigoDetraccion.equalsIgnoreCase("2003") ? porcentajeDetraccion : "0";

                cola.encolarLinea("A3;codiDetraccion;" + indice + ";" + codigoDetraccion);
                cola.encolarLinea("A3;valorDetraccion;" + indice + ";" + valorDetr);
                cola.encolarLinea("A3;MntDetraccion;" + indice + ";" + montoDetr);
                cola.encolarLinea("A3;PorcentajeDetraccion;" + indice + ";" + porcDetr);
                cola.encolarLinea("");
            }

            // Información de cuotas si aplica crédito
            if (formaDePago.equalsIgnoreCase("credito")) {
                LocalDate fechaVencimiento = LocalDate.parse(fechaEmision).plusDays(30);
                cola.encolarLinea("A5;Cuota;1;1");
                cola.encolarLinea("A5;MontoCuota;1;" + saldoPendiente);
                cola.encolarLinea("A5;FechaVencCuota;1;" + fechaVencimiento.format(DateTimeFormatter.ISO_LOCAL_DATE));
                cola.encolarLinea("");
            }
            // Detalle de productos/servicios (ítems)
            final int COL_SALTO = 13;
            final int COL_QTY = 37, COL_COD = 38, COL_NOMBRE = 39, COL_PRECIO = 41, COL_PRECIO_SIN_IGV = 42;
            final int COL_EXENTO = 43, COL_IGV = 44, COL_MONTO = 45;

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setDecimalSeparator('.');
            DecimalFormat formato5Dec = new DecimalFormat("0.00000", symbols);
            DecimalFormat formato2Dec = new DecimalFormat("0.00", symbols);

            int lineaItem = 1;

            for (int j = 0; j < 10; j++) {
                int offset = j * COL_SALTO;
                String cantidad = obtenerValor(tabla, fila, COL_QTY + offset);
                String codigo   = obtenerValor(tabla, fila, COL_COD + offset);

                if (!cantidad.isEmpty() && !codigo.isEmpty()) {
                    BigDecimal precioUnitario   = new BigDecimal(obtenerValor(tabla, fila, COL_PRECIO + offset));
                    BigDecimal precioSinImpuesto = new BigDecimal(obtenerValor(tabla, fila, COL_PRECIO_SIN_IGV + offset));
                    BigDecimal montoItem        = new BigDecimal(obtenerValor(tabla, fila, COL_MONTO + offset));

                    // Bloque B: detalle de cada ítem
                    cola.encolarLinea("B;NroLinDet;" + lineaItem + ";" + lineaItem);
                    cola.encolarLinea("B;QtyItem;" + lineaItem + ";" + cantidad);
                    cola.encolarLinea("B;UnmdItem;" + lineaItem + ";NIU");
                    cola.encolarLinea("B;VlrCodigo;" + lineaItem + ";" + codigo);
                    cola.encolarLinea("B;NmbItem;" + lineaItem + ";" + obtenerValor(tabla, fila, COL_NOMBRE + offset));
                    cola.encolarLinea("B;PrcItem;" + lineaItem + ";" + formato5Dec.format(precioUnitario));
                    cola.encolarLinea("B;PrcItemSinIgv;" + lineaItem + ";" + formato5Dec.format(precioSinImpuesto));
                    cola.encolarLinea("B;MontoItem;" + lineaItem + ";" + formato2Dec.format(montoItem));
                    cola.encolarLinea("B;IndExe;" + lineaItem + ";" + obtenerValor(tabla, fila, COL_EXENTO + offset));
                    cola.encolarLinea("B;CodigoTipoIgv;" + lineaItem + ";1000");
                    cola.encolarLinea("B;TasaIgv;" + lineaItem + ";18");
                    cola.encolarLinea("B;ImpuestoIgv;" + lineaItem + ";" + obtenerValor(tabla, fila, COL_IGV + offset));
                    cola.encolarLinea("");

                    lineaItem++;
                } else {
                    break; // si no hay más ítems válidos, salir del bucle
                }
            }

            // Generar archivo TXT con nombre estructurado
            String rutaArchivo = carpetaDestino + tipoDocumento + "-" + serieDocumento + "-" + numeroCorrelativo + ".txt";
            new ConvertirTxt(rutaArchivo).generarTXT(cola);
        }

        JOptionPane.showMessageDialog(null, "Proceso completado");
    }
}
