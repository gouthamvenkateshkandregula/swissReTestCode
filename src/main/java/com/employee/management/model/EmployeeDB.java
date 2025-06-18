package com.employee.management.model;

public class EmployeeDB {
    private int id;
    private String name;
    private String role;
    private Integer managerId;
    private double salary;

    public EmployeeDB() {}

    public EmployeeDB(int id, String name, String role, Integer managerId, double salary) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.managerId = managerId;
        this.salary = salary;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    @Override
    public String toString() {
        return String.format("EmployeeDB{id=%d, name='%s', role='%s', managerId=%s, salary=%.2f}",
                id, name, role, managerId, salary);
    }
}
