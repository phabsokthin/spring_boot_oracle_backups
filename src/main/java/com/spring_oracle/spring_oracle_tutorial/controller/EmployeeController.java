package com.spring_oracle.spring_oracle_tutorial.controller;

import org.springframework.web.bind.annotation.RestController;

import com.spring_oracle.spring_oracle_tutorial.entity.Employee;
import com.spring_oracle.spring_oracle_tutorial.services.EmployeeService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
@RequestMapping("api/employee")
public class EmployeeController {


    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
  
    }

    // Read All

    @GetMapping
    public List<Employee> getAll() {
        return employeeService.getAll();
    }

    // Create
    @PostMapping()
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        return ResponseEntity.ok(
            employeeService.create(employee)
        );
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @RequestBody Employee employee) {
        return ResponseEntity.ok(
            employeeService.update(id, employee)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity <Void> delete(@PathVariable Long id){

         employeeService.delete(id);
         return  ResponseEntity.noContent().build();
    } 
    

}
