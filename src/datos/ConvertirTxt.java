package datos;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;

/*
 * Clase para crear archivos TXT desde una JTable.
 */
public class ConvertirTxt {

    private String archivoDestino;

    public ConvertirTxt() {
    }

    public ConvertirTxt(String archivoDestino) {
        this.archivoDestino = archivoDestino;
    }

    ColaArchivo cola = new ColaArchivo();

    // Auxiliar: devuelve el valor seguro de una celda
    public String obtenerValor(JTable tabla, int fila, int columna) {
        if (columna >= tabla.getColumnCount()) {
            return "";
        }
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

    // Código tipo IGV según afectación
    private String codigoTipoIgv(String indExe) {
        return indExe.equals("10") ? "1000"
                : indExe.equals("17") ? "1016"
                : indExe.equals("20") ? "9997"
                : indExe.equals("30") ? "9998"
                : indExe.equals("40") ? "9995" : "9996";
    }

    // Sustento de notas de crédito y débito
    private String codigoNotaCreditoDebito(String documento, String motivo) {
        switch (documento) {
            case "07":
                return motivo.equals("01") ? "Anulación de la operación"
                        : motivo.equals("02") ? "Anulación por error en el RUC"
                        : motivo.equals("03") ? "Corrección por error en la descripción"
                        : motivo.equals("04") ? "Descuento global"
                        : motivo.equals("05") ? "Descuento por item"
                        : motivo.equals("06") ? "Devolución total"
                        : motivo.equals("07") ? "Devolución por item"
                        : motivo.equals("08") ? "Bonificación"
                        : motivo.equals("09") ? "Disminución en el valor"
                        : motivo.equals("10") ? "Otros conceptos"
                        : motivo.equals("11") ? "Ajustes de operaciones de exportación"
                        : motivo.equals("12") ? "Ajustes afectos al IVAP" : "Ajustes montos y/o fechas de pago";
            case "08":
                return motivo.equals("01") ? "Interes por mora"
                        : motivo.equals("02") ? "Aumento en el valor"
                        : motivo.equals("03") ? "Penalidades/otros conceptos"
                        : motivo.equals("11") ? "Ajustes de operaciones de exportación" : "Ajustes afectos al IVAP";
            default:
                return "Error: Solo funciona para documento con código 08 y 07";
        }
    }

    // Cuotas múltiples para pago a crédito (listas separadas por '|')
    private void generarCuotas(String listaCuotas, String listaMontos, String listaFechas) {
        String[] cuotas = listaCuotas.split("\\|");
        String[] montos = listaMontos.split("\\|");
        String[] fechas = listaFechas.split("\\|");

        int total = cuotas.length; // los tres arrays deben tener igual longitud
        for (int i = 0; i < total; i++) {
            int indice = i + 1;
            cola.encolarLinea("A5;Cuota;" + indice + ";" + cuotas[i].trim());
            cola.encolarLinea("A5;MontoCuota;" + indice + ";" + montos[i].trim());
            cola.encolarLinea("A5;FechaVencCuota;" + indice + ";" + fechas[i].trim());
            cola.encolarLinea("");
        }
    }

    // Vencimiento a 30 días desde la fecha de emisión (ISO yyyy-MM-dd)
    private String fechaVencimiento(String fecha) {
        LocalDate emision = LocalDate.parse(fecha);
        LocalDate venc = emision.plusDays(30);
        return venc.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // Suma de montos de cuotas (para MontoNetoPendPago)
    private double pagoPendiente(String listaMontos) {
        if (listaMontos == null || listaMontos.isEmpty()) {
            return 0.0;
        }
        String[] arrayMontos = listaMontos.split("\\|");
        double total = 0;
        for (String monto : arrayMontos) {
            if (monto != null && !monto.trim().isEmpty()) {
                total += Double.parseDouble(monto.trim());
            }
        }
        return total;
    }

    // Detracción: híbrido (conjunto completo si 2003, caso simple si 3000/3001)
    // Mantener exactamente la lógica y nombres del segundo método
    private void generarDetraccion(String tipOperacion, String codDetrac, String mntDetrac, String porDetrac, String banko) {
        if (tipOperacion.equals("1001") && codDetrac.equals("2003")) {
            String[][] detalles = {
                {"2003", "0", mntDetrac, porDetrac}, // índice 1
                {"3000", "025", "0", "0"}, // índice 2
                {"3001", banko, "0", "0"} // índice 3
            };
            int indice = 1;
            for (String[] data : detalles) {
                cola.encolarLinea("A3;codiDetraccion;" + indice + ";" + data[0]);
                cola.encolarLinea("A3;valorDetraccion;" + indice + ";" + data[1]);
                cola.encolarLinea("A3;MntDetraccion;" + indice + ";" + data[2]);
                cola.encolarLinea("A3;PorcentajeDetraccion;" + indice + ";" + data[3]);
                cola.encolarLinea("");
                indice++;
            }
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

            // Datos principales del comprobante
            String tipoDocumento = obtenerValor(tabla, fila, 0);
            String serieDocumento = obtenerValor(tabla, fila, 1);
            String numeroCorrelativo = obtenerValor(tabla, fila, 2);
            String fechaEmision = obtenerValor(tabla, fila, 3);
            String tipoDocCliente = obtenerValor(tabla, fila, 4);
            String docCliente = obtenerValor(tabla, fila, 5);
            String nombreCliente = obtenerValor(tabla, fila, 6);
            String direccionCliente = obtenerValor(tabla, fila, 7);
            String tipoOperacion = obtenerValor(tabla, fila, 8);
            String monedaDocumento = obtenerValor(tabla, fila, 9);
            String montoGratuito = obtenerValor(tabla, fila, 10);
            String montoGravado = obtenerValor(tabla, fila, 11);
            String igvTotal = obtenerValor(tabla, fila, 13);
            String montoTotal = obtenerValor(tabla, fila, 14);

            // Índices extendidos (nueva versión)
            String formaPago = obtenerValor(tabla, fila, 33);
            String cuotasLista = obtenerValor(tabla, fila, 34);
            String montosCuotasLista = obtenerValor(tabla, fila, 35);
            String fechasCuotasLista = obtenerValor(tabla, fila, 36);

            String codigoDetraccion = obtenerValor(tabla, fila, 18);
            String porcentajeDetraccion = obtenerValor(tabla, fila, 19);
            String montoDetraccion = obtenerValor(tabla, fila, 20);
            String cuentaBanco = obtenerValor(tabla, fila, 21);

            String tipoNotaCodigo = obtenerValor(tabla, fila, 22); // motivo de NC/ND
            String tipoDocRef = obtenerValor(tabla, fila, 24);
            String serieRef = obtenerValor(tabla, fila, 25);
            String correlativoRef = obtenerValor(tabla, fila, 26);

            String horaActualStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Bloque A: cabecera
            cola.encolarLinea("A;CODI_EMPR;;1");
            cola.encolarLinea("A;TipoDTE;;" + tipoDocumento);
            cola.encolarLinea("A;Serie;;" + serieDocumento);
            cola.encolarLinea("A;Correlativo;;" + numeroCorrelativo);
            cola.encolarLinea("A;FchEmis;;" + fechaEmision);
            cola.encolarLinea("A;HoraEmision;;" + horaActualStr);
            cola.encolarLinea("A;TipoMoneda;;" + monedaDocumento);
            cola.encolarLinea("A;FormaPago;;" + formaPago);
            if (formaPago.equalsIgnoreCase("credito")) {
                cola.encolarLinea("A;MontoNetoPendPago;;" + pagoPendiente(montosCuotasLista));
            }
            cola.encolarLinea("");

            // Bloque A: emisor
            cola.encolarLinea("A;RUTEmis;;" + emisor.getRucDni());
            cola.encolarLinea("A;TipoRucEmis;;6");
            cola.encolarLinea("A;NomComer;;" + emisor.getNombreComercial());
            cola.encolarLinea("A;RznSocEmis;;" + emisor.getRazonSocial());
            cola.encolarLinea("A;CodigoLocalAnexo;;0000");
            cola.encolarLinea("");

            // Bloque A: receptor
            cola.encolarLinea("A;TipoRutReceptor;;" + tipoDocCliente);
            cola.encolarLinea("A;RUTRecep;;" + docCliente);
            cola.encolarLinea("A;RznSocRecep;;" + nombreCliente);
            cola.encolarLinea("A;DirRecp;;" + direccionCliente);
            cola.encolarLinea("");

            // Bloque A: sustento para NC/ND
            if ("08".equals(tipoDocumento) || "07".equals(tipoDocumento)) {
                cola.encolarLinea("A;Sustento;" + codigoNotaCreditoDebito(tipoDocumento, tipoNotaCodigo));
                cola.encolarLinea("A;TipoNotaCredito;" + tipoNotaCodigo);
                cola.encolarLinea("");
            }

            // Bloque A: montos y operación
            cola.encolarLinea("A;MntNeto;;" + (montoGravado.isEmpty() || "0".equals(montoGravado) ? "0.00" : montoGravado));
            cola.encolarLinea("A;MntExe;;" + ((montoGravado.isEmpty() || "0".equals(montoGravado)) ? montoTotal : "0.00"));
            cola.encolarLinea("A;MntExo;;0.00");
            if ("0200".equals(tipoOperacion)) {
                cola.encolarLinea("A;MntTotalIgv;;0.00"); // inafectas
            }
            if ("03".equals(tipoDocumento)) {
                cola.encolarLinea("A;MntTotGrat;;" + montoGratuito);
            }
            cola.encolarLinea("A;MnTotal;;" + montoTotal);
            if ("0200".equals(tipoOperacion)) {
                cola.encolarLinea("A;FechVencFact;;" + fechaVencimiento(fechaEmision));
            }
            if (!"08".equals(tipoDocumento) && !"07".equals(tipoDocumento)) {
                cola.encolarLinea("A;TipoOperacion;;" + tipoOperacion);
            }
            cola.encolarLinea("");

            // Bloque A2: impuestos (evita escribir IGV si gravado = 0 o inafecto)
            if (montoGravado.isEmpty() || !"0".equals(montoGravado)) {
                cola.encolarLinea("A2;CodigoImpuesto;1;" + ("0200".equals(tipoOperacion) ? "9995" : "1000"));
                cola.encolarLinea("A2;MontoImpuesto;1;" + ("0200".equals(tipoOperacion) || "0".equals(igvTotal) ? "0.00" : igvTotal));
                cola.encolarLinea("A2;MontoImpuestoBase;1;" + montoGravado);
                cola.encolarLinea("A2;TasaImpuesto;1;18");
                cola.encolarLinea("");
            }

            // Bloque A3: detracción (lógica híbrida integrada)
            generarDetraccion(tipoOperacion, codigoDetraccion, montoDetraccion, porcentajeDetraccion, cuentaBanco);

            // Bloque A5: cuotas (dinámicas) si aplica crédito
            if (formaPago.equalsIgnoreCase("credito")) {
                generarCuotas(cuotasLista, montosCuotasLista, fechasCuotasLista);
            }

            // Ítems (B) y descuentos (B1)
            final int COL_SALTO = 13;
            final int COL_QTY = 37, COL_COD = 38, COL_NOMBRE = 39, COL_PRECIO = 41, COL_PRECIO_SIN_IGV = 42;
            final int COL_EXENTO = 43, COL_IGV = 44, COL_MONTO = 45, COL_DESCUENTO = 46;

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setDecimalSeparator('.');
            DecimalFormat formato5Dec = new DecimalFormat("0.00000", symbols);
            DecimalFormat formato2Dec = new DecimalFormat("0.00", symbols);

            int lineaItem = 1;

            for (int j = 0; j < 10; j++) {
                int offset = j * COL_SALTO;
                String cantidad = obtenerValor(tabla, fila, COL_QTY + offset);
                String codigo = obtenerValor(tabla, fila, COL_COD + offset);

                if (!cantidad.isEmpty() && !codigo.isEmpty()) {
                    BigDecimal precioUnitario = new BigDecimal(obtenerValor(tabla, fila, COL_PRECIO + offset));
                    BigDecimal precioSinImpuesto = new BigDecimal(obtenerValor(tabla, fila, COL_PRECIO_SIN_IGV + offset));
                    BigDecimal montoItem = new BigDecimal(obtenerValor(tabla, fila, COL_MONTO + offset));
                    String indExe = obtenerValor(tabla, fila, COL_EXENTO + offset);
                    String impuestoIgvStr = obtenerValor(tabla, fila, COL_IGV + offset);

                    cola.encolarLinea("B;NroLinDet;" + lineaItem + ";" + lineaItem);
                    cola.encolarLinea("B;QtyItem;" + lineaItem + ";" + cantidad);
                    cola.encolarLinea("B;UnmdItem;" + lineaItem + ";NIU");
                    cola.encolarLinea("B;VlrCodigo;" + lineaItem + ";" + codigo);
                    cola.encolarLinea("B;NmbItem;" + lineaItem + ";" + obtenerValor(tabla, fila, COL_NOMBRE + offset));
                    cola.encolarLinea("B;PrcItem;" + lineaItem + ";" + formato5Dec.format(precioUnitario));
                    cola.encolarLinea("B;PrcItemSinIgv;" + lineaItem + ";" + formato5Dec.format(precioSinImpuesto));
                    cola.encolarLinea("B;MontoItem;" + lineaItem + ";" + formato2Dec.format(montoItem));
                    cola.encolarLinea("B;IndExe;" + lineaItem + ";" + indExe);
                    cola.encolarLinea("B;CodigoTipoIgv;" + lineaItem + ";" + codigoTipoIgv(indExe));
                    cola.encolarLinea("B;TasaIgv;" + lineaItem + ";18");

                    // Si el precio sin IGV coincide con el precio, el impuesto por ítem debe ser 0
                    String impuestoItem = "0.00";
                    try {
                        BigDecimal prcItemBd = new BigDecimal(obtenerValor(tabla, fila, COL_PRECIO + offset));
                        BigDecimal prcSinIgvBd = new BigDecimal(obtenerValor(tabla, fila, COL_PRECIO_SIN_IGV + offset));
                        BigDecimal impuestoIgvBd = new BigDecimal(impuestoIgvStr.isEmpty() ? "0" : impuestoIgvStr);
                        impuestoItem = prcItemBd.compareTo(prcSinIgvBd) != 0 ? formato2Dec.format(impuestoIgvBd) : "0.00";
                    } catch (Exception ignore) {
                    }
                    cola.encolarLinea("B;ImpuestoIgv;" + lineaItem + ";" + impuestoItem);
                    cola.encolarLinea("");

                    lineaItem++;
                } else {
                    break;
                }
            }

            // Descuentos por ítem (B1)
            lineaItem = 1;
            for (int p = 0; p < 10; p++) {
                int offset = p * COL_SALTO;
                String descuento = obtenerValor(tabla, fila, COL_DESCUENTO + offset);
                String montoBase = obtenerValor(tabla, fila, COL_MONTO + offset);

                if (!descuento.isEmpty()) {
                    double baseCargo = Double.parseDouble(descuento) + Double.parseDouble(montoBase);
                    double factorCargo = Double.parseDouble(descuento) / baseCargo;
                    factorCargo = Double.parseDouble(String.format("%.5f", factorCargo));

                    cola.encolarLinea("B1;NroLinDet;" + lineaItem + ";" + lineaItem);
                    cola.encolarLinea("B1;IndCargoDescuento;" + lineaItem + ";0");
                    cola.encolarLinea("B1;CodigoCargoDescuento;" + lineaItem + ";00");
                    cola.encolarLinea("B1;FactorCargoDescuento;" + lineaItem + ";" + factorCargo);
                    cola.encolarLinea("B1;MontoCargoDescuento;" + lineaItem + ";" + descuento);
                    cola.encolarLinea("B1;MBaseCargoDescuento;" + lineaItem + ";" + baseCargo);
                    cola.encolarLinea("");
                    lineaItem++;
                }
            }

            // Referencias (D)
            if ("0200".equals(tipoOperacion)) {
                cola.encolarLinea("D;NroLinRef;1;1");
                cola.encolarLinea("D;TipoDocRef;" + tipoDocRef);
                cola.encolarLinea("D;FolioRef;" + correlativoRef);
            }
            if ("08".equals(tipoDocumento) || "07".equals(tipoDocumento)) {
                cola.encolarLinea("D;NroLinRef;1;1");
                cola.encolarLinea("D;TipoDocRef;" + tipoDocRef);
                cola.encolarLinea("D;SerieRef;" + serieRef);
                cola.encolarLinea("D;FolioRef;" + correlativoRef);
            }

            // Generar archivo TXT por fila
            String rutaArchivo = carpetaDestino + tipoDocumento + "-" + serieDocumento + "-" + numeroCorrelativo + ".txt";
            new ConvertirTxt(rutaArchivo).generarTXT(cola);
        }

        JOptionPane.showMessageDialog(null, "Proceso completado");
    }

    //Contamos cuantos documentos de un tipo existen en la tabla (otro metodo)
    public int contarDocumento(JTable tabla, String docBuscado) {
        int contador = 0;
        for (int i = 0; i < tabla.getRowCount(); i++) {
            String tipoDoc = obtenerValor(tabla, i, 0);
            if (docBuscado.equalsIgnoreCase(tipoDoc)) {
                contador++;
            }
        }
        return contador;
    }
}
