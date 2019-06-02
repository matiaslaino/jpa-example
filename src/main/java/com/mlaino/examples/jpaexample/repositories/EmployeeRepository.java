package com.mlaino.examples.jpaexample.repositories;

import com.mlaino.examples.jpaexample.models.Employee;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    Collection<Employee> findByFirstName(String firstName);
    Collection<Employee> findByProject_Name(String projectName);
}
