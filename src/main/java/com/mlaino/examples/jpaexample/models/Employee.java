package com.mlaino.examples.jpaexample.models;

import javax.persistence.*;
import java.util.Collection;

@Entity
public class Employee {
    private String firstName;
    private String lastName;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToMany
    private Collection<Project> projects;

    @ManyToOne
    private Department department;

    @OneToOne
    private ParkingLot parkingLot;
}
