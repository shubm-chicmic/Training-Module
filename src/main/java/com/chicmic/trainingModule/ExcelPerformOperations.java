package com.chicmic.trainingModule;
import com.chicmic.trainingModule.Entity.Course;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelPerformOperations {
    private static final Course course = new Course();
    public static void excelPerformOperations(String excelFilePath) {
        try {
            File excelFile = new File(excelFilePath);
            FileInputStream fis = new FileInputStream(excelFile);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            List<Row> rows = new ArrayList<>();
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                rows.add(row);
            }


            int rowCounter = 0;
            for (Row row : sheet) {
                // Print phase and main topics as separators
                if (rowCounter == 0 || rowCounter == 1 || rowCounter == 14) {
                    System.out.println("-------- " + row.getCell(0).getStringCellValue() + " --------");
                }

                // Print the content of each row
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.NUMERIC) {
                        System.out.print(cell.getNumericCellValue() + " | ");
                    } else if (cell.getCellType() == CellType.STRING) {
                        System.out.print(cell.getStringCellValue() + " | ");
                    }
                    // Add more conditions if you have other cell types
                }
                System.out.println(); // Move to the next line
                rowCounter++;
            }


            fis.close(); // Close the input stream when done
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
