package datos;

import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/*
 * Utilidad para importar información desde un archivo Excel hacia una JTable.
 */
public class LectorExcel {

    private Workbook libroExcel;
    private DefaultTableModel tablaModelo = new DefaultTableModel();

    /**
     * Abre un archivo Excel (.xls, .xlsx, .xlsm) y carga su contenido en la tabla indicada.
     */
    public void cargarExcel(String rutaArchivo, JTable tablaDestino) {
        try {
            FileInputStream flujo = new FileInputStream(new File(rutaArchivo));

            // Identificar el tipo de archivo según extensión
            if (rutaArchivo.toLowerCase().endsWith(".xlsx") || rutaArchivo.toLowerCase().endsWith(".xlsm")) {
                libroExcel = new XSSFWorkbook(flujo);
            } else if (rutaArchivo.toLowerCase().endsWith(".xls")) {
                libroExcel = new HSSFWorkbook(flujo);
            } else {
                JOptionPane.showMessageDialog(null, "Formato no válido. Use .xlsx, .xls o .xlsm");
                flujo.close();
                return;
            }

            // Intentar obtener hoja por nombre (FAC_BOL), si no existe usar segunda hoja
            Sheet hoja = libroExcel.getSheet("FAC_BOL");
            if (hoja == null) {
                JOptionPane.showMessageDialog(null, "No se encontro la hoja con el nombre: FAC_BOL");
                return;//Salir si la hoja no existe
            }

            DataFormatter formateador = new DataFormatter();

            tablaModelo.setRowCount(0);
            tablaModelo.setColumnCount(0);

            for (Row fila : hoja) {
                int cantidadCeldas = fila.getLastCellNum();
                if (cantidadCeldas <= 0) continue;

                Object[] filaDatos = new Object[cantidadCeldas];

                for (int c = 0; c < cantidadCeldas; c++) {
                    Cell celda = fila.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String valor;
                    // Manejo especial para valores numéricos cercanos a cero
                    if (celda.getCellType() == CellType.NUMERIC) {
                        double numero = celda.getNumericCellValue();
                        if (Math.abs(numero) < 0.0000001) {
                            valor = "0"; // reemplazar -0 por 0
                        } else {
                            valor = formateador.formatCellValue(celda);
                        }
                    } else {
                        valor = formateador.formatCellValue(celda);
                    }
                    filaDatos[c] = valor;
                }

                // La primera fila se usa como encabezado (antigua usaba fila 0, nueva usaba fila 1)
                if (fila.getRowNum() == 1) {
                    for (Object encabezado : filaDatos) {
                        if (encabezado != null) tablaModelo.addColumn(encabezado.toString());
                    }
                } else {
                    tablaModelo.addRow(filaDatos);
                }
            }

            tablaDestino.setModel(tablaModelo);
            flujo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
