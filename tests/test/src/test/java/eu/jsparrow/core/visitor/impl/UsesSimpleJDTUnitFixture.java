package eu.jsparrow.core.visitor.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import eu.jsparrow.jdtunit.JdtUnitFixtureClass;
import eu.jsparrow.jdtunit.JdtUnitFixtureProject;

/**
 * This test helper provides a {@link JdtUnitFixtureProject} project
 * pre-populated with a {@link JdtUnitFixtureClass} and a single method. This
 * simulates the simple approach, which we have used until November 2019. By
 * November 2019, the {@link JdtUnitFixutre} has been extended to also support
 * the addition of classes and multiple methods per class. To achieve this,
 * please use {@link UsesJDTUnitFixture} instead.
 *
 */
public abstract class UsesSimpleJDTUnitFixture {

	private static final String CLASS_FIXTURE_NAME = "FixtureClass"; //$NON-NLS-1$

	private static final String METHOD_FIXTURE_NAME = "FixtureMethod"; //$NON-NLS-1$

	protected static JdtUnitFixtureProject fixtureProject;
	protected static JdtUnitFixtureClass fixture;
	protected static MethodDeclaration fixtureMethod;

	@BeforeAll
	public static void setUpClass() throws Exception {
		fixtureProject = new JdtUnitFixtureProject();
		fixtureProject.setUp();

		fixture = fixtureProject.addCompilationUnit(CLASS_FIXTURE_NAME);
		fixture.addMethod(METHOD_FIXTURE_NAME);
	}

	@AfterAll
	public static void tearDownClass() throws CoreException {
		fixtureProject.tearDown();
	}

	@AfterEach
	public void tearDownTest() throws Exception {
		fixture.clear(true);
	}
}
