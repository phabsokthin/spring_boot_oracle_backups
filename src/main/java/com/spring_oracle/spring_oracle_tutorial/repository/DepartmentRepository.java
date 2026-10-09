package com.spring_oracle.spring_oracle_tutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.spring_oracle.spring_oracle_tutorial.entity.Department;

// database layer
public interface  DepartmentRepository extends JpaRepository<Department, Long> {
    
    boolean departmentName(String departmentname);
}
