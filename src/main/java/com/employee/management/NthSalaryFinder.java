package com.employee.management;

import com.employee.management.database.DatabaseManager;
import com.employee.management.model.EmployeeDB;
import com.employee.management.utility.DBExcelReader;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class NthSalaryFinder {

    public static void main(String[] args) {
        String excelFilePath = "employees.xlsx";

        // Check for command line arguments
        if (args.length > 0) {
            excelFilePath = args[0];
        }

        try {
            System.out.println("Reading employee data from: " + excelFilePath);
            List<EmployeeDB> employees = DBExcelReader.readEmployeesFromExcel(excelFilePath);

            System.out.println("Found " + employees.size() + " employees");

            // Initialize database and insert data
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.insertEmployees(employees);

            // Show all employees ranked by salary
            dbManager.printAllEmployeesByStatus();

            // Interactive menu for finding nth highest salary
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n=== Nth Highest Salary Finder ===");
                System.out.println("1. Find using ROW_NUMBER()");
                System.out.println("2. Find using DENSE_RANK()");
                System.out.println("3. Find using LIMIT/OFFSET");
                System.out.println("4. Find using Subquery");
                System.out.println("5. Find using CTE (Common Table Expression)");
                System.out.println("6. Compare all methods");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();

                if (choice == 0) break;

                if (choice >= 1 && choice <= 2) {
                    System.out.print("Enter n (position): ");
                    int n = scanner.nextInt();

                    if (n <= 0) {
                        System.out.println("Invalid position. Please enter a positive number.");
                        continue;
                    }

                    switch (choice) {
                        case 1 -> findAndDisplayEmployee(dbManager, n, "Subquery",
                                () -> dbManager.findNthHighestSalaryEmployee_Subquery(n));
                        case 2 -> compareAllMethods(dbManager, n);
                    }
                }
            }

            dbManager.close();
            scanner.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    interface EmployeeFinder {
        EmployeeDB find() throws SQLException;
    }

    private static void findAndDisplayEmployee(DatabaseManager dbManager, int n,
                                               String method, EmployeeFinder finder) {
        try {
            long startTime = System.nanoTime();
            EmployeeDB employee = finder.find();
            long endTime = System.nanoTime();

            System.out.println("\n--- " + method + " Method ---");
            if (employee != null) {
                System.out.println("Employee with " + n + "th highest salary:");
                System.out.println(employee);
            } else {
                System.out.println("No employee found at position " + n);
            }
            System.out.printf("Execution time: %.3f ms%n", (endTime - startTime) / 1_000_000.0);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    private static void compareAllMethods(DatabaseManager dbManager, int n) {
        System.out.println("\n=== Comparing All Methods for Position " + n + " ===");

        String[] methods = {"Subquery"};
        EmployeeFinder[] finders = {
                () -> dbManager.findNthHighestSalaryEmployee_Subquery(n),
        };

        for (int i = 0; i < methods.length; i++) {
            try {
                long startTime = System.nanoTime();
                EmployeeDB emp = finders[i].find();
                long endTime = System.nanoTime();

                System.out.printf("%-15s: %s (%.3f ms)%n",
                        methods[i],
                        emp != null ? emp.getName() + " - $" + emp.getSalary() : "Not found",
                        (endTime - startTime) / 1_000_000.0);

            } catch (SQLException e) {
                System.out.printf("%-15s: Error - %s%n", methods[i], e.getMessage());
            }
        }
    }
}

