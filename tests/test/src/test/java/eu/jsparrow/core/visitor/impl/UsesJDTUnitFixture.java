package eu.jsparrow.core.visitor.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import eu.jsparrow.jdtunit.JdtUnitFixtureProject;
import eu.jsparrow.jdtunit.JdtUnitFixtureClass;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

/**
 * This test helper provides a plain {@link JdtUnitFixtureProject} project. Any classes
 * or methods have to be populated manually in your tests using the methods of
 * {@link JdtUnitFixtureClass}. For the simple version with pre-populated class
 * and method (the only version until November 2019), have a look at
 * {@link UsesSimpleJDTUnitFixture}.
 */
public abstract class UsesJDTUnitFixture {

	protected static JdtUnitFixtureProject fixtureProject;

	@BeforeAll
	public static void setUpClass() throws Exception {
		fixtureProject = new JdtUnitFixtureProject();
		fixtureProject.setUp();
	}

	@AfterAll
	public static void tearDownClass() throws CoreException {
		fixtureProject.tearDown();
	}

	protected Block createBlock(String string) throws Exception {
		return ASTNodeBuilder.createBlockFromString(string);
	}
}
