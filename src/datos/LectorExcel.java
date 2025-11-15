package datos;

import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
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

            Sheet hoja = libroExcel.getSheetAt(1); // Se toma la segunda hoja
            DataFormatter formateador = new DataFormatter(); // Convierte cualquier celda a texto

            tablaModelo.setRowCount(0);
            tablaModelo.setColumnCount(0);

            for (Row fila : hoja) {
                int cantidadCeldas = fila.getLastCellNum();
                if (cantidadCeldas <= 0) continue;

                Object[] filaDatos = new Object[cantidadCeldas];

                for (int c = 0; c < cantidadCeldas; c++) {
                    Cell celda = fila.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    filaDatos[c] = formateador.formatCellValue(celda);
                }

                // La primera fila se usa como encabezado
                if (fila.getRowNum() == 0) {
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
