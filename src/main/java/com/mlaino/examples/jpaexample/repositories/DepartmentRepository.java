package com.mlaino.examples.jpaexample.repositories;

import com.mlaino.examples.jpaexample.models.Department;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends CrudRepository<Department, Long> {
}
