package com.employee.management.utility;

import com.employee.management.model.EmployeeDB;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBExcelReader {

    public static List<EmployeeDB> readEmployeesFromExcel(String filePath) throws IOException {
        List<EmployeeDB> employees = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    int id = (int) getCellNumericValue(row.getCell(0));
                    String name = getCellStringValue(row.getCell(1));
                    String role = getCellStringValue(row.getCell(2));
                    Integer managerId = null;

                    Cell managerCell = row.getCell(3);
                    if (managerCell != null && managerCell.getCellType() != CellType.BLANK) {
                        managerId = (int) getCellNumericValue(managerCell);
                    }

                    double salary = getCellNumericValue(row.getCell(4));

                    employees.add(new EmployeeDB(id, name, role, managerId, salary));

                } catch (Exception e) {
                    System.err.println("Error processing row " + (i + 1) + ": " + e.getMessage());
                }
            }
        }

        return employees;
    }

    private static double getCellNumericValue(Cell cell) {
        if (cell == null) return 0;

        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield 0;
                }
            }
            default -> 0;
        };
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }
}
