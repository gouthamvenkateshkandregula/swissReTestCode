package com.employee.management;

import com.employee.management.model.EmployeeHierarchy;
import com.employee.management.utility.ExcelHierarchyReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeHierarchyBuilder {

    public static void main(String[] args) {
        String excelFilePath = "employees.xlsx"; // Default file name
        String jsonOutputPath = "employee_hierarchy.json";

        // Checking for command line arguments
        if (args.length > 0) {
            excelFilePath = args[0];
        }
        if (args.length > 1) {
            jsonOutputPath = args[1];
        }

        try {
            System.out.println("Reading employee data from: " + excelFilePath);
            List<EmployeeHierarchy> employees = ExcelHierarchyReader.readEmployeesFromExcel(excelFilePath);

            System.out.println("Found " + employees.size() + " employees");

            List<EmployeeHierarchy> hierarchy = buildHierarchy(employees);

            writeHierarchyToJson(hierarchy, jsonOutputPath);

            System.out.println("Employee hierarchy successfully written to: " + jsonOutputPath);

            printHierarchySummary(hierarchy);

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<EmployeeHierarchy> buildHierarchy(List<EmployeeHierarchy> employees) {
        // Creatie a map for fast lookup
        Map<Integer, EmployeeHierarchy> employeeMap = employees.stream()
                .collect(Collectors.toMap(EmployeeHierarchy::getId, emp -> emp));

        // Find top-level employees (those without managers or whose manager doesn't exist)
        List<EmployeeHierarchy> topLevel = new ArrayList<>();

        for (EmployeeHierarchy employee : employees) {
            Integer managerId = employee.getManagerId();

            if (managerId == null || !employeeMap.containsKey(managerId)) {
                // we get top-level employee
                topLevel.add(employee);
            } else {
                // Add this employee as a reportee to their manager
                EmployeeHierarchy manager = employeeMap.get(managerId);
                manager.addReportee(employee);
            }
        }

        return topLevel;
    }

    private static void writeHierarchyToJson(List<EmployeeHierarchy> hierarchy, String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // If there's only one top-level employee, write it directly as an object
        // Otherwise, write as an array
        if (hierarchy.size() == 1) {
            mapper.writeValue(new File(filePath), hierarchy.get(0));
        } else {
            mapper.writeValue(new File(filePath), hierarchy);
        }
    }

    private static void printHierarchySummary(List<EmployeeHierarchy> hierarchy) {
        System.out.println("\n=== Hierarchy Summary ===");
        for (EmployeeHierarchy topEmployee : hierarchy) {
            printEmployeeHierarchy(topEmployee, 0);
        }
    }

    private static void printEmployeeHierarchy(EmployeeHierarchy employee, int level) {
        String indent = "  ".repeat(level);
        System.out.println(indent + "├─ " + employee.getName() +
                " (" + employee.getRole() + ") [ID: " + employee.getId() + "]");

        for (EmployeeHierarchy reportee : employee.getReportees()) {
            printEmployeeHierarchy(reportee, level + 1);
        }
    }
}
