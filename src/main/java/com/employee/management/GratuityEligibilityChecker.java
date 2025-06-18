package com.employee.management;

import com.employee.management.model.Employee;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GratuityEligibilityChecker {
    
    private static final String INPUT_FILE = "employees.xlsx";
    private static final String OUTPUT_FILE = "gratuity_eligible_employees.xlsx";
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Gratuity Eligibility Checker ===");
            System.out.println("Reading employee data from: " + INPUT_FILE);
            
            List<Employee> employees = readEmployeesFromExcel(INPUT_FILE);
            System.out.println("Total employees loaded: " + employees.size());
            
            if (employees.isEmpty()) {
                System.out.println("No employees found in the Excel file.");
                return;
            }
            
            analyzeGratuityEligibility(employees);
            
        } catch (Exception e) {
            System.err.println("Error processing employee data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<Employee> readEmployeesFromExcel(String fileName) throws IOException {
        List<Employee> employees = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(fileName)) {
            Workbook workbook;
            
            // Determine file type and create appropriate workbook
            if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                throw new IllegalArgumentException("Unsupported file format. Please use .xlsx or .xls files.");
            }
            
            Sheet sheet = workbook.getSheetAt(0); // Read first sheet
            
            // Find header row and columns
            Map<String, Integer> columnIndices = findColumnIndices(sheet);
            
            if (columnIndices.isEmpty()) {
                System.err.println("Could not find required columns in the Excel file.");
                workbook.close();
                return employees;
            }
            
            System.out.println("Found columns: " + columnIndices.keySet());
            
            // Read data rows
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                
                try {
                    Employee employee = parseEmployeeFromRow(row, columnIndices);
                    if (employee != null) {
                        employees.add(employee);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing row " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }
            
            workbook.close();
        }
        
        return employees;
    }
    
    private static Map<String, Integer> findColumnIndices(Sheet sheet) {
        Map<String, Integer> columnIndices = new HashMap<>();
        Row headerRow = sheet.getRow(0);
        
        if (headerRow == null) return columnIndices;
        
        for (Cell cell : headerRow) {
            if (cell.getCellType() == CellType.STRING) {
                String header = cell.getStringCellValue().toLowerCase().trim();
                
                // Map various possible header names into standard names
                switch (header) {
                    case "id" -> columnIndices.put("id", cell.getColumnIndex());
                    case "name" -> columnIndices.put("name", cell.getColumnIndex());
                    case "city" -> columnIndices.put("city", cell.getColumnIndex());
                    case "state" -> columnIndices.put("state", cell.getColumnIndex());
                    case "category" -> columnIndices.put("category", cell.getColumnIndex());
                    case "manager_id", "manager id", "managerid" -> columnIndices.put("manager_id", cell.getColumnIndex());
                    case "salary" -> columnIndices.put("salary", cell.getColumnIndex());
                    case "doj", "date of joining", "joining date" -> columnIndices.put("doj", cell.getColumnIndex());
                }
            }
        }
        
        return columnIndices;
    }
    
    private static Employee parseEmployeeFromRow(Row row, Map<String, Integer> columnIndices) {
        Employee employee = new Employee();
        
        try {
            // ID
            if (columnIndices.containsKey("id")) {
                Cell idCell = row.getCell(columnIndices.get("id"));
                if (idCell != null) {
                    employee.setId((int) getCellValueAsDouble(idCell));
                }
            }
            
            // Name
            if (columnIndices.containsKey("name")) {
                Cell nameCell = row.getCell(columnIndices.get("name"));
                if (nameCell != null) {
                    employee.setName(getCellValueAsString(nameCell));
                }
            }
            
            // City
            if (columnIndices.containsKey("city")) {
                Cell cityCell = row.getCell(columnIndices.get("city"));
                if (cityCell != null) {
                    employee.setCity(getCellValueAsString(cityCell));
                }
            }
            
            // State
            if (columnIndices.containsKey("state")) {
                Cell stateCell = row.getCell(columnIndices.get("state"));
                if (stateCell != null) {
                    employee.setState(getCellValueAsString(stateCell));
                }
            }
            
            // Category
            if (columnIndices.containsKey("category")) {
                Cell categoryCell = row.getCell(columnIndices.get("category"));
                if (categoryCell != null) {
                    employee.setCategory(getCellValueAsString(categoryCell));
                }
            }
            
            // Manager ID
            if (columnIndices.containsKey("manager_id")) {
                Cell managerCell = row.getCell(columnIndices.get("manager_id"));
                if (managerCell != null) {
                    String managerValue = getCellValueAsString(managerCell);
                    if (!managerValue.equalsIgnoreCase("null") && !managerValue.trim().isEmpty()) {
                        try {
                            employee.setManagerId((int) Double.parseDouble(managerValue));
                        } catch (NumberFormatException e) {
                            employee.setManagerId(null);
                        }
                    }
                }
            }
            
            // Salary
            if (columnIndices.containsKey("salary")) {
                Cell salaryCell = row.getCell(columnIndices.get("salary"));
                if (salaryCell != null) {
                    employee.setSalary(getCellValueAsDouble(salaryCell));
                }
            }
            
            // DOJ
            if (columnIndices.containsKey("doj")) {
                Cell dojCell = row.getCell(columnIndices.get("doj"));
                if (dojCell != null) {
                    if (dojCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dojCell)) {
                        // Excel date
                        LocalDate date = dojCell.getLocalDateTimeCellValue().toLocalDate();
                        employee.setDoj(date);
                    } else {
                        // String date
                        String dateStr = getCellValueAsString(dojCell);
                        employee.setDojFromString(dateStr);
                    }
                }
            }
            
            return employee;
            
        } catch (Exception e) {
            System.err.println("Error parsing employee from row: " + e.getMessage());
            return null;
        }
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        yield String.valueOf((long) numValue);
                    } else {
                        yield String.valueOf(numValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    yield cell.getStringCellValue();
                }
            }
            default -> "";
        };
    }
    
    private static double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    yield 0.0;
                }
            }
            case FORMULA -> cell.getNumericCellValue();
            default -> 0.0;
        };
    }
    
    private static void analyzeGratuityEligibility(List<Employee> employees) throws IOException {
        System.out.println("\n=== GRATUITY ELIGIBILITY ANALYSIS ===");
        System.out.println("Eligibility Criteria: More than 60 months of service");
        System.out.println("Current Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        
        // Filter eligible employees
        List<Employee> eligibleEmployees = employees.stream()
                .filter(Employee::isEligibleForGratuity)
                .sorted(Comparator.comparing(Employee::getDoj))
                .collect(Collectors.toList());
        
        // Filter non-eligible employees
        List<Employee> nonEligibleEmployees = employees.stream()
                .filter(emp -> !emp.isEligibleForGratuity())
                .sorted(Comparator.comparing(Employee::getDoj))
                .collect(Collectors.toList());
        
        // Display summary
        System.out.println("\n=== SUMMARY ===");
        System.out.println("Total Employees: " + employees.size());
        System.out.println("Eligible for Gratuity: " + eligibleEmployees.size());
        System.out.println("Not Eligible: " + nonEligibleEmployees.size());
        
        // Display eligible employees
        if (!eligibleEmployees.isEmpty()) {
            System.out.println("\n=== EMPLOYEES ELIGIBLE FOR GRATUITY ===");
            System.out.printf("%-5s %-15s %-12s %-15s %-12s %-12s %-20s%n", 
                    "ID", "Name", "Category", "City", "DOJ", "Service", "Months");
            System.out.println("-".repeat(95));
            
            for (Employee emp : eligibleEmployees) {
                System.out.printf("%-5d %-15s %-12s %-15s %-12s %-20s %-8d%n",
                        emp.getId(),
                        emp.getName(),
                        emp.getCategory(),
                        emp.getCity(),
                        emp.getDoj() != null ? emp.getDoj().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "N/A",
                        emp.getServiceDuration(),
                        emp.getMonthsOfService());
            }
        }
        
        // Display non-eligible employees
        if (!nonEligibleEmployees.isEmpty()) {
            System.out.println("\n=== EMPLOYEES NOT ELIGIBLE FOR GRATUITY ===");
            System.out.printf("%-5s %-15s %-12s %-15s %-12s %-20s%n", 
                    "ID", "Name", "Category", "City", "DOJ", "Service");
            System.out.println("-".repeat(83));
            
            for (Employee emp : nonEligibleEmployees) {
                System.out.printf("%-5d %-15s %-12s %-15s %-12s %-20s%n",
                        emp.getId(),
                        emp.getName(),
                        emp.getCategory(),
                        emp.getCity(),
                        emp.getDoj() != null ? emp.getDoj().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "N/A",
                        emp.getServiceDuration());
            }
        }
        
        // Export eligible employees to Excel
        if (!eligibleEmployees.isEmpty()) {
            exportEligibleEmployees(eligibleEmployees);
            System.out.println("\n✓ Eligible employees exported to: " + OUTPUT_FILE);
        }
        
    }
    
    private static void exportEligibleEmployees(List<Employee> eligibleEmployees) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Gratuity Eligible Employees");
        
        // Create header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "City", "State", "Category", "Manager ID", 
                           "Salary", "DOJ", "Service Duration", "Months of Service", "Eligible"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        for (int i = 0; i < eligibleEmployees.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Employee emp = eligibleEmployees.get(i);
            
            row.createCell(0).setCellValue(emp.getId());
            row.createCell(1).setCellValue(emp.getName());
            row.createCell(2).setCellValue(emp.getCity());
            row.createCell(3).setCellValue(emp.getState());
            row.createCell(4).setCellValue(emp.getCategory());
            row.createCell(5).setCellValue(emp.getManagerId() != null ? emp.getManagerId().toString() : "N/A");
            row.createCell(6).setCellValue(emp.getSalary());
            row.createCell(7).setCellValue(emp.getDoj() != null ? emp.getDoj().format(dateFormatter) : "N/A");
            row.createCell(8).setCellValue(emp.getServiceDuration());
            row.createCell(9).setCellValue(emp.getMonthsOfService());
            row.createCell(10).setCellValue("YES");
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(OUTPUT_FILE)) {
            workbook.write(fileOut);
        }
        
        workbook.close();
    }
    
}