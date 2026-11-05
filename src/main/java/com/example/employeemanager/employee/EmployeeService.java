package com.example.employeemanager.employee;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll(String name, String department) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasDepartment = department != null && !department.isBlank();
        if (hasName && hasDepartment) {
            return repository.findByNameContainingIgnoreCaseAndDepartmentIgnoreCase(name.trim(), department.trim());
        }
        if (hasName) {
            return repository.findByNameContainingIgnoreCase(name.trim());
        }
        if (hasDepartment) {
            return repository.findByDepartmentIgnoreCase(department.trim());
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Employee findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    public Employee create(Employee employee) {
        if (repository.existsById(employee.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An employee with this ID already exists");
        }
        if (repository.existsByEmailIgnoreCase(employee.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An employee with this email already exists");
        }
        try {
            return repository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An employee with this email already exists");
        }
    }

    public Employee update(Long id, Employee employee) {
        Employee existing = findById(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(employee.getEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An employee with this email already exists");
        }
        existing.updateFrom(employee);
        try {
            return repository.saveAndFlush(existing);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An employee with this email already exists");
        }
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    @Transactional(readOnly = true)
    public List<Employee> findByDepartment(String department) {
        return repository.findByDepartmentIgnoreCase(department);
    }
}
