package com.employee.management.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EmployeeHierarchy {
    private int id;
    private String name;
    private String role;
    private Integer managerId;
    private List<EmployeeHierarchy> reportees;

    public EmployeeHierarchy(int id, String name, String role, Integer managerId) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.managerId = managerId;
        this.reportees = new ArrayList<>();
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

    public List<EmployeeHierarchy> getReportees() { return reportees; }
    public void setReportees(List<EmployeeHierarchy> reportees) { this.reportees = reportees; }

    public void addReportee(EmployeeHierarchy employee) {
        this.reportees.add(employee);
    }

    @Override
    public String toString() {
        return String.format("EmployeeHierarchy{id=%d, name='%s', role='%s', managerId=%s}",
                id, name, role, managerId);
    }
}
