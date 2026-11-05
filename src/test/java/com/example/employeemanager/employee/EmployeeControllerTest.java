package com.example.employeemanager.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearEmployees() {
        repository.deleteAll();
    }

    @Test
    void supportsEmployeeCrudSearchAndDepartmentCount() throws Exception {
        Employee employee = new Employee(42L, "Taylor Example", "taylor@example.com", "Engineering",
                "Developer", new BigDecimal("70000.00"), true);
        String request = objectMapper.writeValueAsString(employee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42));

        mockMvc.perform(get("/api/employees?name=taylor&department=engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/employees/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
        mockMvc.perform(get("/api/employees/department/Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        Employee updated = new Employee(42L, "Taylor Updated", "taylor@example.com", "Product",
                "Product Engineer", new BigDecimal("75000.00"), false);
        mockMvc.perform(put("/api/employees/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Taylor Updated"))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/employees/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("Product"));
        mockMvc.perform(delete("/api/employees/42"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/employees/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidEmployeePayload() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":0,"name":"","email":"not-an-email","department":"","designation":"",
                                "salary":-1,"active":true}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Missing ID","email":"missing.id@example.com","department":"Engineering",
                                "designation":"Developer","salary":50000,"active":true}
                                """))
                .andExpect(status().isBadRequest());
    }
}
