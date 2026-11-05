package com.example.employeemanager.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @NotNull
    @Positive
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Email
    @Size(max = 180)
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String department;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String designation;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(nullable = false)
    private boolean active = true;

    protected Employee() {
    }

    public Employee(Long id, String name, String email, String department, String designation,
                    BigDecimal salary, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.salary = salary;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public boolean isActive() {
        return active;
    }

    public void updateFrom(Employee employee) {
        this.name = employee.name;
        this.email = employee.email;
        this.department = employee.department;
        this.designation = employee.designation;
        this.salary = employee.salary;
        this.active = employee.active;
    }
}
