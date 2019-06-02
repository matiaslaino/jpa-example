package com.mlaino.examples.jpaexample.models;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Collection;

@Entity
public class Department {
    private String name;

    @OneToMany(mappedBy = "department")
    private Collection<Employee> employees;
}
