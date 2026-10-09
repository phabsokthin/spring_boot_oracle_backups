package com.spring_oracle.spring_oracle_tutorial.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.spring_oracle.spring_oracle_tutorial.entity.Employee;
import com.spring_oracle.spring_oracle_tutorial.repository.EmployeeRepository;

@Service
public class EmployeeService {


    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        
    }

    // Read Data from database
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    // created
    public Employee create(Employee employee) {
        return employeeRepository.save(employee);
    }

    // update
    public Employee update(Long id, Employee employee) {
        Employee employeeExist = employeeRepository.findById(id).orElse(null);

        if (employeeExist != null) {
            employeeExist.setName(employee.getName());
            employeeExist.setEmail(employee.getEmail());
            employeeExist.setSalary(employee.getSalary());
            employeeExist.setDepartmentId(employee.getDepartmentId());

            return employeeRepository.save(employeeExist);
        }
        return null;
    }

    // delete
    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }

}
