package com.employee.management;

import com.employee.management.model.EmployeeSalary;
import com.employee.management.utility.ExcelReader;
import com.employee.management.utility.ExcelWriter;
import com.employee.management.validation.SalaryComparisonResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeeSalaryAnalyzer {

    public static List<SalaryComparisonResult> findEmployeesEarningMoreThanManagers(List<EmployeeSalary> employees) {
        // Create a map for fast employee lookup by ID
        Map<Integer, EmployeeSalary> employeeMap = employees.stream()
                .collect(Collectors.toMap(EmployeeSalary::id, emp -> emp));

        return employees.stream()
                .filter(EmployeeSalary::hasManager) // Only consider employees with managers
                .map(employee -> {
                    Optional<EmployeeSalary> manager = Optional.ofNullable(employeeMap.get(employee.managerId()));
                    return manager.map(mgr -> new Object[]{employee, mgr}).orElse(null);
                })
                .filter(pair -> pair != null) // Filter out employeees whose managers are not found
                .map(pair -> {
                    EmployeeSalary employee = (EmployeeSalary) pair[0];
                    EmployeeSalary manager = (EmployeeSalary) pair[1];
                    return new Object[]{employee, manager, employee.salary() - manager.salary()};
                })
                .filter(result -> (Double) result[2] > 0) // Only employees earning more than their managers
                .map(result -> new SalaryComparisonResult(
                        (EmployeeSalary) result[0],
                        (EmployeeSalary) result[1],
                        (Double) result[2]
                ))
                .sorted((r1, r2) -> Double.compare(r2.salaryDifference(), r1.salaryDifference())) // Sorting by salary difference descending
                .collect(Collectors.toList());
    }

    public static void printResults(List<SalaryComparisonResult> results) {
        if (results.isEmpty()) {
            System.out.println("No employees found who earn more than their managers.");
            return;
        }

        System.out.println("\nEMPLOYEES EARNING MORE THAN THEIR MANAGERS:");
        System.out.println("-".repeat(120));
        System.out.printf("%-15s %-15s %-12s %-15s %-15s %-12s %-12s%n",
                "Employee Name", "Employee ID", "Emp Salary", "Manager Name", "Manager ID", "Mgr Salary", "Difference");
        System.out.println("-".repeat(120));

        for (SalaryComparisonResult result : results) {
            System.out.printf("%-15s %-15d ₹%-11.2f %-15s %-15d ₹%-11.2f ₹%-11.2f%n",
                    result.employee().name(),
                    result.employee().id(),
                    result.employee().salary(),
                    result.manager().name(),
                    result.manager().id(),
                    result.manager().salary(),
                    result.salaryDifference());
        }
        System.out.println("-".repeat(120));
    }

    public static void main(String[] args) {
        String excelFilePath = "employees.xlsx"; // Default input file path
        String outputFilePath = "salary_analysis_results.xlsx"; // Default output file path

        if (args.length > 0) {
            excelFilePath = args[0];
        }
        if (args.length > 1) {
            outputFilePath = args[1];
        }

        try {
            System.out.println("Reading employee data from: " + excelFilePath);
            List<EmployeeSalary> employees = ExcelReader.readEmployeesFromExcel(excelFilePath);

            System.out.printf("Successfully loaded %d employees.%n%n", employees.size());

            // Find employeees earning more than their managers
            List<SalaryComparisonResult> results = findEmployeesEarningMoreThanManagers(employees);

            // Print detailed results in console
            printResults(results);

            // Print summary in console
            if (!results.isEmpty()) {
                System.out.println("\nSUMMARY:");
                double maxDifference = results.get(0).salaryDifference();
                SalaryComparisonResult topResult = results.get(0);
                System.out.printf("Highest salary difference: ₹%.2f (%s vs %s)%n",
                        maxDifference, topResult.employee().name(), topResult.manager().name());

                double avgDifference = results.stream()
                        .mapToDouble(SalaryComparisonResult::salaryDifference)
                        .average()
                        .orElse(0.0);
                System.out.printf("Average salary difference: ₹%.2f%n", avgDifference);
            }

            // Write results to Excel file
            System.out.println("\n" + "=".repeat(80));
            System.out.println("Writing results to Excel file: " + outputFilePath);
            ExcelWriter.writeResultsToExcel(results, employees, outputFilePath);
            System.out.println("Excel file created successfully!");
            System.out.println("The file contains the following sheets:");
            System.out.println("  1. Summary - Overall statistics and analysis");
            System.out.println("  2. Detailed Results - Complete comparison results");
            System.out.println("  3. All Employees - Complete employee directory");
            System.out.println("=".repeat(80));

        } catch (IOException e) {
            System.err.println("Error processing Excel files: " + e.getMessage());
            System.err.println("Please ensure the input file exists and you have write permissions for the output location.");
        } catch (Exception e) {
            System.err.println("An error occurred during analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}