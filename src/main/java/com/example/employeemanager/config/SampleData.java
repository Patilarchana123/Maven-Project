package com.example.employeemanager.config;

import com.example.employeemanager.employee.Employee;
import com.example.employeemanager.employee.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class SampleData {

    @Bean
    CommandLineRunner loadSampleEmployees(EmployeeRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new Employee(1001L, "Ava Patel", "ava.patel@example.com", "Engineering",
                                "Software Engineer", new BigDecimal("92000.00"), true),
                        new Employee(1002L, "Noah Williams", "noah.williams@example.com", "Human Resources",
                                "HR Manager", new BigDecimal("78000.00"), true),
                        new Employee(1003L, "Mia Chen", "mia.chen@example.com", "Finance",
                                "Financial Analyst", new BigDecimal("81500.00"), true),
                        new Employee(1004L, "Liam Johnson", "liam.johnson@example.com", "Engineering",
                                "QA Engineer", new BigDecimal("73500.00"), false)
                ));
            }
        };
    }
}
