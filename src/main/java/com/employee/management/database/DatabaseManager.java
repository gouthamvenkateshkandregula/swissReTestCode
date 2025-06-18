package com.employee.management.database;

import com.employee.management.model.EmployeeDB;

import java.sql.*;
import java.util.List;
public class DatabaseManager {
    private static final String DB_URL = "jdbc:h2:mem:employees;DB_CLOSE_DELAY=-1";
    private Connection connection;

    public DatabaseManager() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        createTable();
    }

    private void createTable() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS employees (
                id INTEGER PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                role VARCHAR(255) NOT NULL,
                manager_id INTEGER,
                salary DECIMAL(12,2) NOT NULL
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    public void insertEmployees(List<EmployeeDB> employees) throws SQLException {
        String insertSQL = """
            INSERT INTO employees (id, name, role, manager_id, salary) 
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            for (EmployeeDB emp : employees) {
                pstmt.setInt(1, emp.getId());
                pstmt.setString(2, emp.getName());
                pstmt.setString(3, emp.getRole());

                if (emp.getManagerId() != null) {
                    pstmt.setInt(4, emp.getManagerId());
                } else {
                    pstmt.setNull(4, Types.INTEGER);
                }

                pstmt.setDouble(5, emp.getSalary());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }


    // Method : Using subquery with COUNT (works with any SQL database)
    public EmployeeDB findNthHighestSalaryEmployee_Subquery(int n) throws SQLException {
        String sql = """
            SELECT e1.id, e1.name, e1.role, e1.manager_id, e1.salary
            FROM employees e1
            WHERE (
                SELECT COUNT(DISTINCT e2.salary)
                FROM employees e2
                WHERE e2.salary > e1.salary
            ) = ? - 1
            ORDER BY e1.salary DESC
            LIMIT 1
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, n);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        }
        return null;
    }

    private EmployeeDB mapResultSetToEmployee(ResultSet rs) throws SQLException {
        EmployeeDB emp = new EmployeeDB();
        emp.setId(rs.getInt("id"));
        emp.setName(rs.getString("name"));
        emp.setRole(rs.getString("role"));

        int managerId = rs.getInt("manager_id");
        if (!rs.wasNull()) {
            emp.setManagerId(managerId);
        }

        emp.setSalary(rs.getDouble("salary"));
        return emp;
    }

    public void printAllEmployeesByStatus() throws SQLException {
        String sql = """
            SELECT id, name, role, manager_id, salary,
                   DENSE_RANK() OVER (ORDER BY salary DESC) AS salary_rank
            FROM employees
            ORDER BY salary DESC
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== All Employees Ranked by Salary ===");
            System.out.printf("%-4s %-15s %-12s %-10s %-12s %-4s%n",
                    "ID", "Name", "Role", "Manager", "Salary", "Rank");
            System.out.println("-".repeat(65));

            while (rs.next()) {
                System.out.printf("%-4d %-15s %-12s %-10s $%-11.2f %-4d%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getObject("manager_id") != null ? rs.getString("manager_id") : "N/A",
                        rs.getDouble("salary"),
                        rs.getInt("salary_rank"));
            }
        }
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

