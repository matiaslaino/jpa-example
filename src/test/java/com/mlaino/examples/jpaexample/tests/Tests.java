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
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
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

	// To manage transactions explicitly, we need to use the EntityManagerFactory instead of injecting the manager directly.
	@PersistenceUnit
	private EntityManagerFactory entityManagerFactory;

	private long exampleEmployeeId;

	private static Employee createEmployee(String firstName, String lastName) {
		var emp = new Employee();
		emp.setFirstName(firstName);
		emp.setLastName(lastName);

		return emp;
	}

	private static void functionThatMayThrowAnException() {
		throw new RuntimeException();
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

		Assert.assertNotEquals(null, emp.getParkingLot());
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

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
	public void testTransactionRollbacksOnException() {

		try {
			entityManager.getTransaction().begin();

			var emp1 = createEmployee("Jean-Luc", "Picard");
			var emp2 = createEmployee("William", "Riker");

			entityManager.persist(emp1);
			entityManager.persist(emp2);

			functionThatMayThrowAnException();

			entityManager.getTransaction().commit();
			Assert.fail("Transaction should not have commited");
		} catch (Exception ex) {
			// Empty catch so that it doesn't end the test
		}

		// emp1 and emp2 should not have persisted, the exception should have made the transaction rollback
		Assert.assertTrue(employeeRepository.findByFirstName("Jean-Luc").isEmpty());
		Assert.assertTrue(employeeRepository.findByFirstName("William").isEmpty());
	}

	@Test
	@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
	public void testAttachedAndDetachedEntities() {
		var em = entityManagerFactory.createEntityManager();

		var picard = employeeRepository.save(createEmployee("Jean-Luc", "Picard"));
		var riker = employeeRepository.save(createEmployee("William", "Riker"));

		Assert.assertFalse(employeeRepository.findByFirstName("Jean-Luc").isEmpty());
		Assert.assertFalse(employeeRepository.findByFirstName("William").isEmpty());

		em.getTransaction().begin();

		// picard and riker are not in the persistence context, they are not being tracked by the entity manager
		Assert.assertFalse(em.contains(picard));
		Assert.assertFalse(em.contains(riker));

		// the merge method returns a reference to a COPY of the object passed as parameter
		var attachedPicard = em.merge(picard);
		var attachedRiker = em.merge(riker);
		// in other words, attachedPicard != picard

		// the original objects are not on the persistence context, they are not being tracked
		Assert.assertFalse(em.contains(picard));
		Assert.assertFalse(em.contains(riker));

		// but those returned by the merge operation are being tracked.
		Assert.assertTrue(em.contains(attachedPicard));
		Assert.assertTrue(em.contains(attachedRiker));

		// if we do any changes to the picard object, it will not be persisted back to the database.
		// instead of assigning to a new variable, it's customary to do the following:
		picard = em.merge(picard);
		// this way we don't have to worry about which one is being tracked and which one isn't.
		Assert.assertTrue(em.contains(picard));

		// picard is now in the persistence context, riker is not (attachedRiker is).
		picard.setLastName("Picard_MODIFIED");
		riker.setLastName("Riker_MODIFIED");

		em.getTransaction().commit();

		var picardFromRepository = employeeRepository.findByFirstName("Jean-Luc").get(0);
		var rikerFromRepository = employeeRepository.findByFirstName("William").get(0);

		Assert.assertEquals("Picard_MODIFIED", picardFromRepository.getLastName());
		Assert.assertEquals("Riker", rikerFromRepository.getLastName());

		// riker was modifier, but it was not being tracked, so it was not persisted to the db.
		// picard was.
	}
}
