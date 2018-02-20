package eu.jsparrow.core.visitor.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.Block;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import eu.jsparrow.jdtunit.JdtUnitFixture;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

public abstract class UsesJDTUnitFixture {

	protected static JdtUnitFixture fixture;

	@BeforeClass
	public static void setUpClass() throws Exception {
		fixture = new JdtUnitFixture();
		fixture.setUp();
	}

	@AfterClass
	public static void tearDownClass() throws CoreException {
		fixture.tearDown();
	}

	@After
	public void tearDown() throws Exception {
		fixture.clear();
	}

	protected Block createBlock(String string) throws Exception {
		return ASTNodeBuilder.createBlock(string);
	}

}
