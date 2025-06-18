package com.employee.management.utility;

import com.employee.management.validation.SalaryComparisonResult;
import com.employee.management.model.EmployeeSalary;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelWriter {

    public static void writeResultsToExcel(List<SalaryComparisonResult> results,
                                           List<EmployeeSalary> employees,
                                           String outputFileName) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            CellStyle positiveAmountStyle = createPositiveAmountStyle(workbook);
            CellStyle negativeAmountStyle = createNegativeAmountStyle(workbook);

            // Create Summary Sheet
            createSummarySheet(workbook, results, employees, titleStyle, headerStyle, currencyStyle, percentStyle);

            // Create Detailed Results Sheet
            createDetailedResultsSheet(workbook, results, titleStyle, headerStyle, currencyStyle,
                    positiveAmountStyle, negativeAmountStyle);

            // Create All Employees Sheet
            createAllEmployeesSheet(workbook, employees, titleStyle, headerStyle, currencyStyle);

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(outputFileName)) {
                workbook.write(fileOut);
            }
        }
    }

    private static void createSummarySheet(Workbook workbook, List<SalaryComparisonResult> results,
                                           List<EmployeeSalary> employees, CellStyle titleStyle,
                                           CellStyle headerStyle, CellStyle currencyStyle,
                                           CellStyle percentStyle) {
        Sheet summarySheet = workbook.createSheet("Summary");
        int rowNum = 0;

        // Title
        Row titleRow = summarySheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Salary Analysis Summary Report");
        titleCell.setCellStyle(titleStyle);

        // Generation timestamp
        rowNum++;
        Row timestampRow = summarySheet.createRow(rowNum++);
        timestampRow.createCell(0).setCellValue("Generated on:");
        timestampRow.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")));

        // Statistics section
        rowNum++;
        Row statsHeaderRow = summarySheet.createRow(rowNum++);
        Cell statsHeaderCell = statsHeaderRow.createCell(0);
        statsHeaderCell.setCellValue("STATISTICS");
        statsHeaderCell.setCellStyle(headerStyle);

        // Calculate statistics
        long totalEmployees = employees.size();
        long employeesWithManagers = employees.stream().filter(EmployeeSalary::hasManager).count();
        long directorsCount = employees.stream().filter(emp -> "director".equalsIgnoreCase(emp.category())).count();
        long managersCount = employees.stream().filter(emp -> "manager".equalsIgnoreCase(emp.category())).count();
        long regularEmployeesCount = employees.stream().filter(emp -> "employee".equalsIgnoreCase(emp.category())).count();

        // Add statistics rows
        addStatRow(summarySheet, rowNum++, "Total Employees:", totalEmployees);
        addStatRow(summarySheet, rowNum++, "Directors:", directorsCount);
        addStatRow(summarySheet, rowNum++, "Managers:", managersCount);
        addStatRow(summarySheet, rowNum++, "Regular Employees:", regularEmployeesCount);
        addStatRow(summarySheet, rowNum++, "Employees with Managers:", employeesWithManagers);
        addStatRow(summarySheet, rowNum++, "Employees Earning More Than Managers:", results.size());

        // Percentage calculation
        Row percentRow = summarySheet.createRow(rowNum++);
        percentRow.createCell(0).setCellValue("Percentage Earning More Than Managers:");
        Cell percentCell = percentRow.createCell(1);
        double percentage = employeesWithManagers > 0 ? (results.size() * 100.0) / employeesWithManagers : 0;
        percentCell.setCellValue(percentage / 100); // Excel expects decimal for percentage
        percentCell.setCellStyle(percentStyle);

        // Summary of results
        if (!results.isEmpty()) {
            rowNum++;
            Row summaryHeaderRow = summarySheet.createRow(rowNum++);
            Cell summaryHeaderCell = summaryHeaderRow.createCell(0);
            summaryHeaderCell.setCellValue("SALARY DIFFERENCE ANALYSIS");
            summaryHeaderCell.setCellStyle(headerStyle);

            double maxDifference = results.stream().mapToDouble(SalaryComparisonResult::salaryDifference).max().orElse(0);
            double minDifference = results.stream().mapToDouble(SalaryComparisonResult::salaryDifference).min().orElse(0);
            double avgDifference = results.stream().mapToDouble(SalaryComparisonResult::salaryDifference).average().orElse(0);
            double totalDifference = results.stream().mapToDouble(SalaryComparisonResult::salaryDifference).sum();

            addCurrencyStatRow(summarySheet, rowNum++, "Highest Salary Difference:", maxDifference, currencyStyle);
            addCurrencyStatRow(summarySheet, rowNum++, "Lowest Salary Difference:", minDifference, currencyStyle);
            addCurrencyStatRow(summarySheet, rowNum++, "Average Salary Difference:", avgDifference, currencyStyle);
            addCurrencyStatRow(summarySheet, rowNum++, "Total Salary Difference:", totalDifference, currencyStyle);

            // Top performer
            SalaryComparisonResult topResult = results.stream()
                    .max((r1, r2) -> Double.compare(r1.salaryDifference(), r2.salaryDifference()))
                    .orElse(null);

            if (topResult != null) {
                rowNum++;
                Row topPerformerRow = summarySheet.createRow(rowNum++);
                topPerformerRow.createCell(0).setCellValue("Top Performer:");
                topPerformerRow.createCell(1).setCellValue(String.format("%s (ID: %d) earns ₹%.2f more than manager %s",
                        topResult.employee().name(), topResult.employee().id(),
                        topResult.salaryDifference(), topResult.manager().name()));
            }
        }

        // Auto-size columns
        summarySheet.autoSizeColumn(0);
        summarySheet.autoSizeColumn(1);
    }

    private static void createDetailedResultsSheet(Workbook workbook, List<SalaryComparisonResult> results,
                                                   CellStyle titleStyle, CellStyle headerStyle,
                                                   CellStyle currencyStyle, CellStyle positiveAmountStyle,
                                                   CellStyle negativeAmountStyle) {
        Sheet resultsSheet = workbook.createSheet("Detailed Results");
        int rowNum = 0;

        // Title
        Row titleRow = resultsSheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Employees Earning More Than Their Managers");
        titleCell.setCellStyle(titleStyle);

        rowNum++; // Empty row

        if (results.isEmpty()) {
            Row noResultsRow = resultsSheet.createRow(rowNum);
            noResultsRow.createCell(0).setCellValue("No employees found who earn more than their managers.");
            return;
        }

        // Headers
        Row headerRow = resultsSheet.createRow(rowNum++);
        String[] headers = {
                "Employee ID", "Employee Name", "Employee Category", "Employee City", "Employee State",
                "Employee Salary", "Manager ID", "Manager Name", "Manager Category",
                "Manager Salary", "Salary Difference", "Percentage Difference"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }

        // Data rows
        for (SalaryComparisonResult result : results) {
            Row dataRow = resultsSheet.createRow(rowNum++);
            EmployeeSalary emp = result.employee();
            EmployeeSalary mgr = result.manager();

            dataRow.createCell(0).setCellValue(emp.id());
            dataRow.createCell(1).setCellValue(emp.name());
            dataRow.createCell(2).setCellValue(emp.category());
            dataRow.createCell(3).setCellValue(emp.city());
            dataRow.createCell(4).setCellValue(emp.state());

            Cell empSalaryCell = dataRow.createCell(5);
            empSalaryCell.setCellValue(emp.salary());
            empSalaryCell.setCellStyle(currencyStyle);

            dataRow.createCell(6).setCellValue(mgr.id());
            dataRow.createCell(7).setCellValue(mgr.name());
            dataRow.createCell(8).setCellValue(mgr.category());

            Cell mgrSalaryCell = dataRow.createCell(9);
            mgrSalaryCell.setCellValue(mgr.salary());
            mgrSalaryCell.setCellStyle(currencyStyle);

            Cell diffCell = dataRow.createCell(10);
            diffCell.setCellValue(result.salaryDifference());
            diffCell.setCellStyle(result.salaryDifference() > 0 ? positiveAmountStyle : negativeAmountStyle);

            Cell percentDiffCell = dataRow.createCell(11);
            double percentDiff = (result.salaryDifference() / mgr.salary()) * 100;
            percentDiffCell.setCellValue(percentDiff / 100); // Excel expects decimal for percentage
            percentDiffCell.setCellStyle(createPercentStyle(workbook));
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            resultsSheet.autoSizeColumn(i);
        }
    }

    private static void createAllEmployeesSheet(Workbook workbook, List<EmployeeSalary> employees,
                                                CellStyle titleStyle, CellStyle headerStyle,
                                                CellStyle currencyStyle) {
        Sheet allEmployeesSheet = workbook.createSheet("All Employees");
        int rowNum = 0;

        // Title
        Row titleRow = allEmployeesSheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Complete Employee Directory");
        titleCell.setCellStyle(titleStyle);

        rowNum++; // Empty row

        // Headers
        Row headerRow = allEmployeesSheet.createRow(rowNum++);
        String[] headers = {
                "ID", "Name", "City", "State", "Category", "Manager ID", "Salary", "Date of Joining"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(headerStyle);
        }

        // Data rows
        for (EmployeeSalary emp : employees) {
            Row dataRow = allEmployeesSheet.createRow(rowNum++);

            dataRow.createCell(0).setCellValue(emp.id());
            dataRow.createCell(1).setCellValue(emp.name());
            dataRow.createCell(2).setCellValue(emp.city());
            dataRow.createCell(3).setCellValue(emp.state());
            dataRow.createCell(4).setCellValue(emp.category());
            dataRow.createCell(5).setCellValue(emp.managerId() != null ? emp.managerId().toString() : "");

            Cell salaryCell = dataRow.createCell(6);
            salaryCell.setCellValue(emp.salary());
            salaryCell.setCellStyle(currencyStyle);

            dataRow.createCell(7).setCellValue(emp.dateOfJoining().toString());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            allEmployeesSheet.autoSizeColumn(i);
        }
    }

    // Helper methods for creating styles
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        return style;
    }

    private static CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"));
        return style;
    }

    private static CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
        return style;
    }

    private static CellStyle createPositiveAmountStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"));
        return style;
    }

    private static CellStyle createNegativeAmountStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.DARK_RED.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"));
        return style;
    }

    // Helper methods for adding rows
    private static void addStatRow(Sheet sheet, int rowNum, String label, long value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private static void addCurrencyStatRow(Sheet sheet, int rowNum, String label,
                                           double value, CellStyle currencyStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(currencyStyle);
    }
}