package com.automation.e2e.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    /**
     * Original framework method: Fetches spreadsheet data as a raw Object matrix.
     */
    public static Object[][] getTestData(String filePath, String sheetName) {
        Object[][] data = null;
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            int rowCount = sheet.getLastRowNum();
            int colCount = sheet.getRow(0).getLastCellNum();

            data = new Object[rowCount][colCount];

            for (int i = 0; i < rowCount; i++) {
                Row row = sheet.getRow(i + 1); // Skip the header row
                for (int j = 0; j < colCount; j++) {
                    Cell cell = row.getCell(j);
                    data[i][j] = (cell == null) ? "" : new DataFormatter().formatCellValue(cell);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel matrix file at: " + filePath, e);
        }
        return data;
    }

    /**
     * Extension method: Reads data rows mapped explicitly to their column header strings.
     */
    public static List<Map<String, String>> getTestDataAsMap(String filePath, String sheetName) {
        List<Map<String, String>> dataList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            Row headerRow = sheet.getRow(0);
            int rowCount = sheet.getLastRowNum();

            for (int i = 1; i <= rowCount; i++) {
                Row currentRow = sheet.getRow(i);
                if (currentRow == null) continue;

                Map<String, String> rowMap = new HashMap<>();
                for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                    String headerKey = headerRow.getCell(j).getStringCellValue().trim();
                    Cell currentCell = currentRow.getCell(j);
                    String cellValue = (currentCell == null) ? "" : new DataFormatter().formatCellValue(currentCell).trim();
                    rowMap.put(headerKey, cellValue);
                }
                dataList.add(rowMap);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to map Excel spreadsheet data from layout source: " + filePath, e);
        }
        return dataList;
    }
}