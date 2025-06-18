package com.employee.management.utility;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class DBExcelWriter {

    public static void createExcel(String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employees");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Role");
            headerRow.createCell(3).setCellValue("Manager_ID");
            headerRow.createCell(4).setCellValue("Salary");

            // input data with salaries
            Object[][] data = {
                    {789, "Rama", "Director", null, 150000.00},
                    {456, "Shivam", "Manager", 789, 120000.00},
                    {123, "Ravi", "Employee", 456, 75000.00},
                    {124, "Priya", "Employee", 456, 80000.00},
                    {790, "Anjali", "Director", null, 160000.00},
                    {457, "Kiran", "Manager", 790, 115000.00},
                    {125, "Suresh", "Employee", 457, 70000.00},
                    {458, "Neha", "Senior Manager", 790, 135000.00},
                    {126, "Rohit", "Senior Employee", 458, 90000.00},
                    {127, "Kavya", "Employee", 457, 72000.00},
                    {128, "Arjun", "Senior Employee", 456, 85000.00}
            };

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                Object[] rowData = data[i];

                row.createCell(0).setCellValue((Integer) rowData[0]);
                row.createCell(1).setCellValue((String) rowData[1]);
                row.createCell(2).setCellValue((String) rowData[2]);

                if (rowData[3] != null) {
                    row.createCell(3).setCellValue((Integer) rowData[3]);
                }

                row.createCell(4).setCellValue((Double) rowData[4]);
            }

            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    public static void main(String[] args) {
        try {
            createExcel("employees.xlsx");
            System.out.println("Excel file with salary data created: employees.xlsx");
        } catch (IOException e) {
            System.err.println("Error creating sample file: " + e.getMessage());
        }
    }
}