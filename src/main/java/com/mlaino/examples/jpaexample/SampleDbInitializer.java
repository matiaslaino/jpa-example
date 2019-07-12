package com.mlaino.examples.jpaexample;

import com.mlaino.examples.jpaexample.models.Employee;
import com.mlaino.examples.jpaexample.repositories.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SampleDbInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;

    public SampleDbInitializer(EmployeeRepository employeeRepository) {

        this.employeeRepository = employeeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
//        var emp = new Employee();
//        emp.setFirstName("Matias");
//        emp.setLastName("Laino");
//
//        employeeRepository.save(emp);
    }
}
