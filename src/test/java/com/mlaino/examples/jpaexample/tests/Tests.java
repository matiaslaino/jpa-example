package com.mlaino.examples.jpaexample.tests;

import com.mlaino.examples.jpaexample.models.Department;
import com.mlaino.examples.jpaexample.models.Employee;
import com.mlaino.examples.jpaexample.models.ParkingLot;
import com.mlaino.examples.jpaexample.models.Project;
import com.mlaino.examples.jpaexample.repositories.DepartmentRepository;
import com.mlaino.examples.jpaexample.repositories.EmployeeRepository;
import com.mlaino.examples.jpaexample.repositories.ProjectRepository;
import org.hibernate.LazyInitializationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Tests {
    private static final String METROMILE = "Metromile";
    private static final String AUTODESK = "Autodesk";
    private static final String DEVELOPMENT = "Development";
    private static final String HR = "HR";
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private long exampleEmployeeId;

    private Employee createEmployee(String firstName, String lastName) {
        var emp = new Employee();
        emp.setFirstName(firstName);
        emp.setLastName(lastName);

        return emp;
    }

    @Before
    public void setup() {
        var departmentNames = Arrays.asList(DEVELOPMENT, HR);
        for (var name : departmentNames) {
            var department = new Department();
            department.setName(name);
            departmentRepository.save(department);
        }

        var projectNames = Arrays.asList(METROMILE, AUTODESK);
        for (var name : projectNames) {
            var project = new Project();
            project.setName(name);
            projectRepository.save(project);
        }

        var emp = createEmployee("Matias", "Laino");
        var parkingLot = new ParkingLot();
        parkingLot.setLotNumber(1);
        emp.setParkingLot(parkingLot);
        emp.addProject(projectRepository.findByName(METROMILE).get());
        emp = employeeRepository.save(emp);
        exampleEmployeeId = emp.getId();
    }

    /**
     * ParkingLot is a one-to-one relationship with the default FetchType of EAGER.
     */
    @Test
    // This annotation is needed so that the context is correctly cleared before each test.
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    public void testGetEmployeeAndParkingLot() {
        var emp = employeeRepository.findByFirstName("Matias").get(0);

        assert (emp.getParkingLot() != null);
    }

    /**
     * Projects is a many-to-many relationship with LAZY FetchType, an exception will be thrown when attempting to access it,
     * since there is no transaction open.
     */
    @Test(expected = LazyInitializationException.class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    public void testProjectsNotReturnedFromRepository() {
        var emp = entityManager.find(Employee.class, exampleEmployeeId);
        var projects = emp.getProjects();
        Assert.assertNotEquals(0, projects.size());
        Assert.fail("LazyInitializationException should have been thrown by now");
    }

    /**
     * Projects is a many-to-many relationship with LAZY FetchType, an exception would be thrown when attempting to access it,
     * but the transactional annotation will have JPA open a transaction when executing the method.
     */
    @Test
    @Transactional
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    public void testProjectsReturnedFromRepositoryTransactional() {
        var emp = entityManager.find(Employee.class, exampleEmployeeId);
        Assert.assertNotEquals(0, emp.getProjects().size());
    }
}
