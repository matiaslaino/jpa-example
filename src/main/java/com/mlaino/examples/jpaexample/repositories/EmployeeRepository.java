package com.mlaino.examples.jpaexample.repositories;

import com.mlaino.examples.jpaexample.models.Employee;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    List<Employee> findByFirstName(String firstName);

    List<Employee> findByProjects_Name(String projectName);
}
