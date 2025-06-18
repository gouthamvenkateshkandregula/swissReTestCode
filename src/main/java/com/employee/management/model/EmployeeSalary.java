package com.employee.management.model;

import java.time.LocalDate;

public record EmployeeSalary(
        int id,
        String name,
        String city,
        String state,
        String category,
        Integer managerId,
        double salary,
        LocalDate dateOfJoining
) {

    public boolean hasManager() {
        return managerId != null;
    }

    public boolean isManager() {
        return "manager".equalsIgnoreCase(category) || "director".equalsIgnoreCase(category);
    }

    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s', category='%s', managerId=%s, salary=%.2f}",
                id, name, category, managerId, salary);
    }
}