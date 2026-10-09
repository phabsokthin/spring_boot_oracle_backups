package com.spring_oracle.spring_oracle_tutorial.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.spring_oracle.spring_oracle_tutorial.entity.Department;
import com.spring_oracle.spring_oracle_tutorial.repository.DepartmentRepository;

// bussiness logic for project
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // get all
    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    //create

    public Department create(Department department){
        return departmentRepository.save(department);
    }



    //get by id
    public Department getById(Long id){
        return departmentRepository.findById(id).orElse(null);
    }

    //update
    public Department update(Long id, Department department){
        Department existingDepartment = departmentRepository.findById(id).orElse(null);

        if(existingDepartment != null){
            existingDepartment.setDepartmentName(department.getDepartmentName());
            return departmentRepository.save(existingDepartment);
        }
        return null;
    }

        //delete
    public void delete(Long id){
        departmentRepository.deleteById(id);
    }
}