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

    public ConvertirTxt() {
    }

    String nombreArchivo;

    public ConvertirTxt(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    //Metodo para generar el archivo TXT
    public void generarTXT(ColaArchivo colaArchivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
            while (!colaArchivo.estaVacio()) {
                String linea = colaArchivo.ObtenerLista();
                writer.write(linea);//Linea que vamos a insertar
                writer.newLine();//Salto de linea
            }
            System.out.println("Archivo " + nombreArchivo + " se genero correcto");
        } catch (Exception e) {
            System.out.println("Error al generar el archivo" + e.getMessage());
        }
    }

    //Metodo para obtener el valor de la celda de forma segura para verificar su es nulo o tiene un contenido
    public String getValorSeguro(JTable tabla, int fila, int columna) {
        //Verificamos que haya columnas y si no evita que el programa se detenga
        if (columna >= tabla.getColumnCount()) {
            return "";
        }
        Object valor = tabla.getValueAt(fila, columna);
        //Hacemos una condicional para verificar si el valor de la celda en nulo o con valor, si es nulo se coloca un espacion en blanco ("")
        return (valor == null) ? "" : valor.toString();
    }

    //Metodo para genera multiples archivos TXT y guardarlo en una archivo especifico
    public void generarTXTTabla(JTable tabla, File carpeta, DatosEmisor emisor) {
        if (carpeta == null) {
            JOptionPane.showMessageDialog(null, "Seleccionar una carpeta de salida primero");
            return;//salida del método si no hay carpeta seleccionada
            //sin ningun error
        }
        //Validar la longitud del DNI/RUC usando su getter
        //if (emisor.getRucDni().length()!=8 && emisor.getRucDni().length()!=11) 
        //  JOptionPane.showMessageDialog(null, "EL RUC/DNI debe contener exactamente 8 o 11 caracteres");

        String rutaCarpeta = carpeta.getAbsolutePath() + File.separator;

        for (int i = 0; i < tabla.getRowCount(); i++) {

            ColaArchivo cola = new ColaArchivo();

            String TIPO_DOC = tabla.getValueAt(i, 0).toString(); //Si queremos evitar errores con espacios en blanco en las celdas podemos usar esto metodo: getValorSeguro(tabla,i,0)
            String SERIE = getValorSeguro(tabla, i, 1); //Esto es el nuevo metodo si quieren evitan errores de la celda
            String CORRELATIVO = tabla.getValueAt(i, 2).toString();
            String FECHA_EMISION = tabla.getValueAt(i, 3).toString();
            String T_DOC_CLIE = tabla.getValueAt(i, 4).toString();
            String DOC_CLIENTE = tabla.getValueAt(i, 5).toString();
            String NOMBRE_CLIENTE = tabla.getValueAt(i, 6).toString();
            String DIRECCION_CLIENTE = tabla.getValueAt(i, 7).toString();
            String TIPO_OPERACION = tabla.getValueAt(i, 8).toString();
            String MONEDA = tabla.getValueAt(i, 9).toString();
            String TOT_GRATUITO = tabla.getValueAt(i, 10).toString();
            String TOT_GRAVADO = tabla.getValueAt(i, 11).toString();
            String IGV = tabla.getValueAt(i, 13).toString();
            String TOTAL = getValorSeguro(tabla, i, 14);
            //Datos de la forma de pago (Supongamos que esta en la columna
            String FORMAPAGO = getValorSeguro(tabla, i, 15);
            String PAGOPENDIENTE = getValorSeguro(tabla, i, 16);
            //Tengan cuidado que excel tenga espacio en blanco en las filas donde no hay ningun tipo de dato. Solucion: Verificar con la tabla de jtable
            //Obtener la hora actual del sistema
            LocalTime horaActual = LocalTime.now();

            //Formateamos la hora a un formato legible
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("hh:mm:ss");
            String horaFormateada = horaActual.format(formato);
            String COD_DETRAC = getValorSeguro(tabla, i, 18);
            String PORCENTAJE_DETRAC = getValorSeguro(tabla, i, 19);
            String MONTO_DETRAC = getValorSeguro(tabla, i, 20);

            //Logica para construir las lineas del archivo txt
            //Bloque primero, falta el formato de pago que no esta en el cuadro
            cola.agregarLinea("A;CODI_EMPR;;1");//Por def.
            cola.agregarLinea("A;TipoDTE;;" + TIPO_DOC);
            cola.agregarLinea("A;Serie;;" + SERIE);
            cola.agregarLinea("A;Correlativo;;" + CORRELATIVO);
            cola.agregarLinea("A;FchEmis;;" + FECHA_EMISION);
            cola.agregarLinea("A;HoraEmision;;" + horaFormateada);
            cola.agregarLinea("A;TipoMoneda;;" + MONEDA);
            //Forma de pago
            cola.agregarLinea("A;FormaPago;;"+FORMAPAGO);
            if (FORMAPAGO.equalsIgnoreCase("credito"))cola.agregarLinea("A;MontoNetoPendPago;;"+PAGOPENDIENTE);
            cola.agregarLinea("");

            //Bloque del emisor
            cola.agregarLinea("A;RUTEmis;;" + emisor.getRucDni());
            cola.agregarLinea("A;TipoRucEmis;;6");//Por def.
            cola.agregarLinea("A;NomComer;;" + emisor.getNombreComercial());
            cola.agregarLinea("A;RznSocEmis;;" + emisor.getRazonSocial());
            cola.agregarLinea("A;CodigoLocalAnexo;;0000");//Por def.
            cola.agregarLinea("");

            //Bloque del receptor
            cola.agregarLinea("A;TipoRutReceptor;;" + T_DOC_CLIE);
            cola.agregarLinea("A;RUTRecep;;" + DOC_CLIENTE);
            cola.agregarLinea("A;RznSocRecep;;" + NOMBRE_CLIENTE);
            cola.agregarLinea("A;DirRecp;;" + DIRECCION_CLIENTE);
            cola.agregarLinea("");

            //Bloque de monto
            cola.agregarLinea("A;MntNeto;;" + TOT_GRAVADO);
            cola.agregarLinea("A;MntExe;;0.00");//Por def.
            cola.agregarLinea("A;MntExo;;0.00");//Por def.
            cola.agregarLinea("A;MntTotal;;" + TOT_GRATUITO);
            cola.agregarLinea("A;MnTotal;;" + TOTAL);
            cola.agregarLinea("A;TipoOperacion;;" + TIPO_OPERACION);
            cola.agregarLinea("");

            //Bloque de impuesto
            cola.agregarLinea("A2;CodigoImpuesto;1;1000");//Por def.
            cola.agregarLinea("A2;MontoImpuesto;1;" + IGV);
            cola.agregarLinea("A2;MontoImpuestoBase;1;" + TOT_GRAVADO);
            cola.agregarLinea("A2;TasaImpuesto;1;18");//Por def.
            cola.agregarLinea("");
            
            //Bloque de detraccion si tipo de operación es 1001
            if (TIPO_OPERACION.equalsIgnoreCase("1001")) {
                cola.agregarLinea("A3;codiDetraccion;1;"+ COD_DETRAC);//Por def.
                cola.agregarLinea("A3;valorDetraccion;1;");
                cola.agregarLinea("A3;MntDetraccion;1;" + MONTO_DETRAC);
                cola.agregarLinea("A3;PorcentajeDetraccion;1;"+ PORCENTAJE_DETRAC);
                cola.agregarLinea("");
            }
            //Si el pago es a credito
            if (FORMAPAGO.equalsIgnoreCase("credito")) {
                 //Parseamos la FECHA_EMISION (String) a un objeto LocalDate
                LocalDate fechaEmision = LocalDate.parse(FECHA_EMISION);
                 //Sumamos 30 días a la fecha de emisión
                LocalDate fechaVencimiento = fechaEmision.plusDays(30);
                //Declaramos la variable para asignar el valor de pagon pendiente
                String FECHA_VENCIMIENTO = fechaVencimiento.format(DateTimeFormatter.ISO_LOCAL_DATE);// ISO_LOCAL_DATE garantiza el formato yyyy-MM-dd
                cola.agregarLinea("A5;Cuota;1;1");
                cola.agregarLinea("A5;MontoCuota;1;"+PAGOPENDIENTE);
                cola.agregarLinea("A5;FechaVencCuota;1;"+FECHA_VENCIMIENTO);
                cola.agregarLinea("");
            }
            

            //Bloque de los items (Agregar aqui los items, recordar que son 10 items como maximo)
            final int COL_QTY_ITEM_1 = 37;
            final int COL_VLR_CODIGO_1 = 38;
            final int COL_NMB_ITEM_1 = 39;
            final int COL_PRC_ITEM_1 = 41;
            final int COL_PRC_SIN_IGV_1 = 42;
            final int COL_INDEXE_ITEM_1 = 43;
            final int COL_IMPUESTO_IGV_1 = 44;
            final int COL_MONTO_ITEM_1 = 45;

            final int COLUMNAS_POR_ITEM_SALTO = 13;

            int numeroDeLinea = 1;
             
            // Preparamos los formatos una sola vez
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            // Aseguramos el uso del punto como separador decimal
            symbols.setDecimalSeparator('.'); 
    
            // Formato para 5 decimales (100.00000)
            DecimalFormat decimalf5 = new DecimalFormat("0.00000", symbols);
            // Formato para 2 decimales (ej. 123.00)
            DecimalFormat decimalf2 = new DecimalFormat("0.00", symbols);

            for (int j = 0; j < 10; j++) {

                int offset = j * COLUMNAS_POR_ITEM_SALTO;

                String qtyItem = getValorSeguro(tabla, i, COL_QTY_ITEM_1 + offset);
                String vlrCodigo = getValorSeguro(tabla, i, COL_VLR_CODIGO_1 + offset);

                if (!qtyItem.isEmpty() && !vlrCodigo.isEmpty()) {

                    String nmbItem = getValorSeguro(tabla, i, COL_NMB_ITEM_1 + offset);
                    String prcItem = getValorSeguro(tabla, i, COL_PRC_ITEM_1 + offset);
                    String prcSinIgv = getValorSeguro(tabla, i, COL_PRC_SIN_IGV_1 + offset);
                    String indExe = getValorSeguro(tabla, i, COL_INDEXE_ITEM_1 + offset);
                    String impuestoIgv = getValorSeguro(tabla, i, COL_IMPUESTO_IGV_1 + offset);
                    String montoItem = getValorSeguro(tabla, i, COL_MONTO_ITEM_1 + offset);

                    String unmdItem = "NIU";
                    String codigoTipoIgv = "1000";
                    String tasaIgv = "18";
                    
                    //Podemos usar .replace(",",".") si el string viene con comas(En ocasiones se usa valores con comoas, por ejemplo: 123,00)
                    BigDecimal prcItemBd = new BigDecimal(prcItem);
                    BigDecimal prcSinIgvBd = new BigDecimal(prcSinIgv);
                    BigDecimal montoItemBd = new BigDecimal(montoItem);
                    
                    cola.agregarLinea("B;NroLinDet;" + numeroDeLinea + ";" + numeroDeLinea);
                    cola.agregarLinea("B;QtyItem;" + numeroDeLinea + ";" + qtyItem);
                    cola.agregarLinea("B;UnmdItem;" + numeroDeLinea + ";" + unmdItem);
                    cola.agregarLinea("B;VlrCodigo;" + numeroDeLinea + ";" + vlrCodigo);
                    cola.agregarLinea("B;NmbItem;" + numeroDeLinea + ";" + nmbItem);
                    cola.agregarLinea("B;PrcItem;" + numeroDeLinea + ";" + decimalf5.format(prcItemBd));
                    cola.agregarLinea("B;PrcItemSinIgv;" + numeroDeLinea + ";" + decimalf5.format(prcSinIgvBd));
                    cola.agregarLinea("B;MontoItem;" + numeroDeLinea + ";" + decimalf2.format(montoItemBd));
                    cola.agregarLinea("B;IndExe;" + numeroDeLinea + ";" + indExe);
                    cola.agregarLinea("B;CodigoTipoIgv;" + numeroDeLinea + ";" + codigoTipoIgv);
                    cola.agregarLinea("B;TasaIgv;" + numeroDeLinea + ";" + tasaIgv);
                    cola.agregarLinea("B;ImpuestoIgv;" + numeroDeLinea + ";" + impuestoIgv);
                    cola.agregarLinea("");

                    numeroDeLinea++;

                } else {
                    break;
                }
            }
            String rutaArchivo = rutaCarpeta + TIPO_DOC + "-" + SERIE + "-" + CORRELATIVO + ".txt";
            ConvertirTxt genera = new ConvertirTxt(rutaArchivo);
            genera.generarTXT(cola);
        }
        JOptionPane.showMessageDialog(null, "Proceso completado");
    }
}
