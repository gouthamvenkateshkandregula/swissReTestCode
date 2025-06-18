package com.employee.management;

import com.employee.management.model.Employee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EmployeeDataGenerator {
    
    private static final String[] FIRST_NAMES = {
        "Ravi", "Shivam", "Rama", "Krishna", "Sreekanth", "Manoj", "Anitha", "Priya", 
        "Rajesh", "Sunitha", "Vikram", "Deepika", "Arjun", "Kavitha", "Suresh", "Meera",
        "Kiran", "Sowmya", "Anil", "Divya", "Mahesh", "Lakshmi", "Venkat", "Sangeetha",
        "Ramesh", "Nisha", "Gopal", "Radha", "Satish", "Vani", "Harish", "Sushma",
        "Ganesh", "Shruti", "Mohan", "Rekha", "Charan", "Padma", "Naveen", "Shalini"
    };
    
    private static final String[] CITIES_STATES = {
        "Hyderabad,Telangana", "Bangalore,Karnataka", "Chennai,Tamil Nadu", "Mumbai,Maharashtra",
        "Pune,Maharashtra", "Kochi,Kerala", "Coimbatore,Tamil Nadu", "Mysore,Karnataka",
        "Vizag,Andhra Pradesh", "Vijayawada,Andhra Pradesh", "Mangalore,Karnataka",
        "Trivandrum,Kerala", "Madurai,Tamil Nadu", "Nashik,Maharashtra", "Warangal,Telangana",
        "Guntur,Andhra Pradesh", "Salem,Tamil Nadu", "Tirupati,Andhra Pradesh",
        "Hubli,Karnataka", "Calicut,Kerala"
    };
    
    public static void main(String[] args) {
        try {
            List<Employee> employees = generateEmployees();
            writeToExcel(employees, "employees.xlsx");
            System.out.println("Successfully generated " + employees.size() + " employee records in employees.xlsx");
        } catch (Exception e) {
            System.err.println("Error generating employee data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<Employee> generateEmployees() {
        List<Employee> employees = new ArrayList<>();
        Random random = new Random();
        
        // Create some directors first (top level)
        List<Integer> directorIds = Arrays.asList(501, 502, 503);
        for (int directorId : directorIds) {
            employees.add(createEmployee(directorId, "director", null, random));
        }
        
        // Create managers under directors
        List<Integer> managerIds = Arrays.asList(401, 402, 403, 404, 405, 406, 407, 408);
        for (int managerId : managerIds) {
            Integer directorId = directorIds.get(random.nextInt(directorIds.size()));
            employees.add(createEmployee(managerId, "manager", directorId, random));
        }
        
        // Create employees under managers
        int currentId = 1001;
        while (employees.size() < 50) {
            Integer managerId = managerIds.get(random.nextInt(managerIds.size()));
            employees.add(createEmployee(currentId++, "employee", managerId, random));
        }
        
        // Sort by ID
        employees.sort(Comparator.comparingInt(Employee::getId));
        
        return employees;
    }
    
    private static Employee createEmployee(int id, String category, Integer managerId, Random random) {
        String name = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String[] cityState = CITIES_STATES[random.nextInt(CITIES_STATES.length)].split(",");
        String city = cityState[0];
        String state = cityState[1];
        
        int salary = generateSalary(category, random);
        LocalDate doj = generateRandomDate(random);
        
        return new Employee(id, name, city, state, category, managerId, salary, doj);
    }
    
    private static int generateSalary(String category, Random random) {
        switch (category) {
            case "director":
                return 120000 + random.nextInt(80000); // between 120k - 200k
            case "manager":
                return 70000 + random.nextInt(50000);  // between 70k - 120k
            case "employee":
                return 35000 + random.nextInt(40000);  //between  35k - 75k
            default:
                return 45000;
        }
    }
    
    private static LocalDate generateRandomDate(Random random) {
        // Generate dates between 2018 and 2024
        LocalDate startDate = LocalDate.of(2018, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        
        long daysBetween = endDate.toEpochDay() - startDate.toEpochDay();
        long randomDays = random.nextLong() % daysBetween;
        if (randomDays < 0) randomDays = -randomDays;
        
        return startDate.plusDays(randomDays);
    }
    
    private static void writeToExcel(List<Employee> employees, String fileName) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "City", "State", "Category", "Manager ID", "Salary", "DOJ"};
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Create data rows
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d-MMM-yyyy");
        for (int i = 0; i < employees.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Employee emp = employees.get(i);
            
            row.createCell(0).setCellValue(emp.getId());
            row.createCell(1).setCellValue(emp.getName());
            row.createCell(2).setCellValue(emp.getCity());
            row.createCell(3).setCellValue(emp.getState());
            row.createCell(4).setCellValue(emp.getCategory());
            
            Cell managerCell = row.createCell(5);
            if (emp.getManagerId() != null) {
                managerCell.setCellValue(emp.getManagerId());
            } else {
                managerCell.setCellValue("null");
            }
            
            row.createCell(6).setCellValue(emp.getSalary());
            row.createCell(7).setCellValue(emp.getDoj().format(dateFormatter));
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
        }
        
        workbook.close();
    }
}