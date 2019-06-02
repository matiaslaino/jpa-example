package com.mlaino.examples.jpaexample.models;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.Collection;

@Entity
public class Project {
    private String name;

    @ManyToMany(mappedBy = "projects")
    private Collection<Employee> employees;
}
