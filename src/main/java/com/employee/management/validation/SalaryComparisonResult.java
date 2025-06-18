package com.employee.management.validation;

import com.employee.management.model.EmployeeSalary;

public record SalaryComparisonResult(
        EmployeeSalary employee,
        EmployeeSalary manager,
        double salaryDifference
) {

    public SalaryComparisonResult {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (manager == null) {
            throw new IllegalArgumentException("Manager cannot be null");
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Employee '%s' (ID: %d, Salary: ₹%.2f) earns ₹%.2f more than their manager '%s' (ID: %d, Salary: ₹%.2f)",
                employee.name(), employee.id(), employee.salary(),
                salaryDifference,
                manager.name(), manager.id(), manager.salary()
        );
    }
}