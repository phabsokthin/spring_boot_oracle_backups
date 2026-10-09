package com.spring_oracle.spring_oracle_tutorial.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.spring_oracle.spring_oracle_tutorial.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
   

}
