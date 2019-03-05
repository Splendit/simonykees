package eu.jsparrow.core.visitor.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import eu.jsparrow.jdtunit.JdtUnitFixture;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

public abstract class UsesJDTUnitFixture {

	protected static JdtUnitFixture fixture;

	@BeforeAll
	public static void setUpClass() throws Exception {
		fixture = new JdtUnitFixture();
		fixture.setUp();
	}

	@AfterAll
	public static void tearDownClass() throws CoreException {
		fixture.tearDown();
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixture.clear();
	}

	protected Block createBlock(String string) throws Exception {
		return ASTNodeBuilder.createBlock(string);
	}

}
