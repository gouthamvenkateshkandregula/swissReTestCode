package com.employee.management.utility;

import com.employee.management.model.EmployeeSalary;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class ExcelReader {
    
    public static List<EmployeeSalary> readEmployeesFromExcel(String filePath) throws IOException {
        List<EmployeeSalary> employees = new ArrayList<>();
        
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            
            // Skip header row (row 0) and start from row 1
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                
                try {
                    EmployeeSalary employee = parseEmployeeFromRow(row, rowIndex + 1);
                    if (employee != null) {
                        employees.add(employee);
                    }
                } catch (Exception e) {
                    System.err.printf("Error parsing row %d: %s%n", rowIndex + 1, e.getMessage());
                }
            }
        }
        
        return employees;
    }
    
    private static EmployeeSalary parseEmployeeFromRow(Row row, int rowNumber) {
        try {
            // Column mapping: ID, Name, City, State, Category, Manager ID, Salary, DOJ
            int id = (int) getCellValueAsDouble(row.getCell(0));
            String name = getCellValueAsString(row.getCell(1));
            String city = getCellValueAsString(row.getCell(2));
            String state = getCellValueAsString(row.getCell(3));
            String category = getCellValueAsString(row.getCell(4));
            
            // Manager ID can be null for directors
            Cell managerIdCell = row.getCell(5);
            Integer managerId = null;
            if (managerIdCell != null && managerIdCell.getCellType() != CellType.BLANK) {
                String managerIdStr = getCellValueAsString(managerIdCell);
                if (!"null".equalsIgnoreCase(managerIdStr) && !managerIdStr.trim().isEmpty()) {
                    managerId = (int) Double.parseDouble(managerIdStr);
                }
            }
            
            double salary = getCellValueAsDouble(row.getCell(6));
            
            Cell dateCell = row.getCell(7);
            LocalDate dateOfJoining = parseDate(dateCell);
            
            return new EmployeeSalary(id, name, city, state, category, managerId, salary, dateOfJoining);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse row " + rowNumber + ": " + e.getMessage(), e);
        }
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    // If it's a whole number, return without decimal
                    if (numValue == Math.floor(numValue)) {
                        yield String.valueOf((long) numValue);
                    } else {
                        yield String.valueOf(numValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private static double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                String stringValue = cell.getStringCellValue();
                try {
                    yield Double.parseDouble(stringValue);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Cannot convert '" + stringValue + "' to number");
                }
            }
            default -> throw new RuntimeException("Cannot convert cell type " + cell.getCellType() + " to number");
        };
    }
    
    private static LocalDate parseDate(Cell cell) {
        if (cell == null) return LocalDate.now();
        
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            String dateStr = cell.getStringCellValue();
            try {
                return LocalDate.parse(dateStr);
            } catch (Exception e) {
                System.err.println("Could not parse date: " + dateStr + ", using current date");
                return LocalDate.now();
            }
        }
        
        return LocalDate.now();
    }
}