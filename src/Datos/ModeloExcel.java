package Datos;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ModeloExcel {

    Workbook wbo;
    DefaultTableModel modelo = new DefaultTableModel();

    public void leerExcel(String rutaArchivo, JTable tabla) {
        try {
            FileInputStream fls = new FileInputStream(new File(rutaArchivo));
            //Workbook wbo;
            //Determinar si es XLS o XLSX
            if (rutaArchivo.toLowerCase().endsWith(".xlsx")) {
                wbo = new XSSFWorkbook(fls);
            } else if (rutaArchivo.toLowerCase().endsWith(".xls")) {
                wbo = new HSSFWorkbook(fls);
            } else {
                JOptionPane.showMessageDialog(null, "Formato de archivo no aceptado. Use .xlsx o .xls");
                fls.close();
            }
            Sheet hoja = wbo.getSheetAt(0);//Obtener la primera hoja
            //Iterator<Row> filaIterator = hoja.iterator();
            //Instanciamos DataFormatter
            //DataFormatter maneja diversas formatos de celda, incluidas fechas y numeros grandes, y los devuelve como cadena formateada.
            //Antes agarra los datos como un double que era pasado a texto(String), lo cual lo toma como notacion cientifica y daba eso formato
            //Con DataFormatter copia el dato tal y como es
            DataFormatter formato = new DataFormatter();
            modelo.setRowCount(0);
            modelo.setColumnCount(0);

            for (Row fila : hoja) {
                int numCeldas = fila.getLastCellNum();
                if (numCeldas <= 0) {
                    continue;
                }

                Object[] datoFila = new Object[numCeldas];

                //Recorremos por índice para NO perder celdas vacías
                for (int c = 0; c < numCeldas; c++) {
                    Cell celda = fila.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    datoFila[c] = formato.formatCellValue(celda);
                }
                // Si es la primera fila, úsala para los encabezados de la tabla
                if (fila.getRowNum() == 0) {
                    for (Object cabeza : datoFila) {
                        if (cabeza != null) {
                            modelo.addColumn(cabeza.toString());
                        }
                    }
                } else {
                    modelo.addRow(datoFila);
                }
            }
            tabla.setModel(modelo);
            fls.close();//Ceramos el flujo de entrada
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
