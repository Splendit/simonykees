package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.jdtunit.JdtUnitFixtureClass;
import eu.jsparrow.jdtunit.JdtUnitFixtureProject;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This test helper provides a plain {@link JdtUnitFixtureProject} project. Any
 * classes or methods have to be populated manually in your tests using the
 * methods of {@link JdtUnitFixtureClass}. For the simple version with
 * pre-populated class and method (the only version until November 2019), have a
 * look at {@link UsesSimpleJDTUnitFixture}.
 */
public abstract class UsesJDTUnitFixture {
	
	public static final String DEFAULT_TYPE_DECLARATION_NAME = "TestCU"; //$NON-NLS-1$

	protected static JdtUnitFixtureProject fixtureProject;
	private AbstractASTRewriteASTVisitor defaultVisitor = new AbstractASTRewriteASTVisitor () {};
	protected JdtUnitFixtureClass defaultFixture;

	@BeforeAll
	public static void setUpClass() throws Exception {
		fixtureProject = new JdtUnitFixtureProject();
		fixtureProject.setUp();
	}

	@AfterAll
	public static void tearDownClass() throws CoreException {
		fixtureProject.tearDown();
	}
	
	@BeforeEach
	public void setUpDefaultFixture() throws Exception {
		defaultFixture = fixtureProject.addCompilationUnit(DEFAULT_TYPE_DECLARATION_NAME);
	}
	
	protected void setDefaultVisitor(AbstractASTRewriteASTVisitor visitor) {
		this.defaultVisitor = visitor;
	}
	
	protected void assertChange(String actual, String expected)
			throws JdtUnitException, JavaModelException, BadLocationException {
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actual);

		defaultVisitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(defaultVisitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, expected),
				defaultFixture.getTypeDeclaration());
	}
	
	protected void assertNoChange(String actualAndExpected)
			throws JdtUnitException, JavaModelException, BadLocationException {
		assertChange(actualAndExpected, actualAndExpected);
	}
	
	protected AbstractASTRewriteASTVisitor getDefaultVisitor() {
		return defaultVisitor;
	}
}
