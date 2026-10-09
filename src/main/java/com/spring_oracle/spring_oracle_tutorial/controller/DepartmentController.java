package com.spring_oracle.spring_oracle_tutorial.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring_oracle.spring_oracle_tutorial.entity.Department;
import com.spring_oracle.spring_oracle_tutorial.services.DepartmentService;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // POST

    @PostMapping
    public ResponseEntity<Department> create(@RequestBody Department department) {
        return ResponseEntity.ok(
                departmentService.create(department));
    }

    @GetMapping
    public List<Department> getAll() {
        return departmentService.getAll();
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Department> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                departmentService.getById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Department> update(
            @PathVariable Long id,
            @RequestBody Department department) {

        return ResponseEntity.ok(
                departmentService.update(id, department));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        departmentService.delete(id);

        return ResponseEntity.noContent().build();
    }
}