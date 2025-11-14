package Datos;

import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Carga datos desde un archivo Excel a una JTable.
 */
public class ModeloExcel {

    Workbook wbo;
    DefaultTableModel modelo = new DefaultTableModel();

    /**
     * Lee un archivo Excel (.xls, .xlsx, .xlsm) y carga su contenido en la tabla.
     */
    public void leerExcel(String rutaArchivo, JTable tabla) {
        try {
            FileInputStream fls = new FileInputStream(new File(rutaArchivo));

            // Detectar formato del archivo
            if (rutaArchivo.toLowerCase().endsWith(".xlsx") || rutaArchivo.toLowerCase().endsWith(".xlsm")) {
                wbo = new XSSFWorkbook(fls);
            } else if (rutaArchivo.toLowerCase().endsWith(".xls")) {
                wbo = new HSSFWorkbook(fls);
            } else {
                JOptionPane.showMessageDialog(null, "Formato no válido. Use .xlsx, .xls o .xlsm");
                fls.close();
                return;
            }

            Sheet hoja = wbo.getSheetAt(1); // Segunda hoja
            DataFormatter formato = new DataFormatter(); // Convierte celdas a texto

            modelo.setRowCount(0);
            modelo.setColumnCount(0);

            for (Row fila : hoja) {
                int numCeldas = fila.getLastCellNum();
                if (numCeldas <= 0) continue;

                Object[] datoFila = new Object[numCeldas];

                for (int c = 0; c < numCeldas; c++) {
                    Cell celda = fila.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    datoFila[c] = formato.formatCellValue(celda);
                }

                // Primera fila como encabezado
                if (fila.getRowNum() == 0) {
                    for (Object encabezado : datoFila) {
                        if (encabezado != null) modelo.addColumn(encabezado.toString());
                    }
                } else {
                    modelo.addRow(datoFila);
                }
            }

            tabla.setModel(modelo);
            fls.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}