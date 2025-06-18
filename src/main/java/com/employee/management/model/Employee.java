package com.employee.management.model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Employee {
    private int id;
    private String name;
    private String city;
    private String state;
    private String category;
    private Integer managerId;
    private double salary;
    private LocalDate doj;

    public Employee() {}

    public Employee(int id, String name, String city, String state, String category,
                    Integer managerId, double salary, LocalDate doj) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.category = category;
        this.managerId = managerId;
        this.salary = salary;
        this.doj = doj;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public LocalDate getDoj() { return doj; }
    public void setDoj(LocalDate doj) { this.doj = doj; }

    public void setDojFromString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            this.doj = null;
            return;
        }

        dateStr = dateStr.trim();

        // Try different date formats
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("d-MMM-yyyy"),      // 4-Jun-2023
                DateTimeFormatter.ofPattern("dd-MMM-yyyy"),     // 04-Jun-2023
                DateTimeFormatter.ofPattern("d/M/yyyy"),        // 4/6/2023
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),      // 04/06/2023
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),      // 2023-06-04
                DateTimeFormatter.ofPattern("d-M-yyyy"),        // 4-6-2023
                DateTimeFormatter.ofPattern("MM/dd/yyyy")       // 06/04/2023
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                this.doj = LocalDate.parse(dateStr, formatter);
                return;
            } catch (DateTimeParseException e) {
                // Continue to next format
            }
        }

        System.err.println("Could not parse date: " + dateStr + " for employee: " + name);
        this.doj = null;
    }

    public long getMonthsOfService() {
        if (doj == null) return 0;
        return Period.between(doj, LocalDate.now()).toTotalMonths();
    }

    public boolean isEligibleForGratuity() {
        return getMonthsOfService() > 60;
    }

    public String getServiceDuration() {
        if (doj == null) return "N/A";

        Period period = Period.between(doj, LocalDate.now());
        long totalMonths = period.toTotalMonths();
        int years = (int) (totalMonths / 12);
        int months = (int) (totalMonths % 12);

        if (years > 0 && months > 0) {
            return years + " years, " + months + " months";
        } else if (years > 0) {
            return years + " years";
        } else {
            return months + " months";
        }
    }

    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s', city='%s', state='%s', category='%s', " +
                        "managerId=%s, salary=%.2f, doj=%s, service=%s, eligible=%s}",
                id, name, city, state, category, managerId, salary, doj,
                getServiceDuration(), isEligibleForGratuity());
    }
}