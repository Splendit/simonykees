package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import eu.jsparrow.common.util.RulesTestUtil;
import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.jdtunit.JdtUnitFixtureClass;
import eu.jsparrow.jdtunit.JdtUnitFixtureProject;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

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

	protected static JdtUnitFixtureProject fixtureProject;
	protected static JdtUnitFixtureClass fixture;

	private AbstractASTRewriteASTVisitor visitor = new AbstractASTRewriteASTVisitor() {
	};

	@BeforeAll
	public static void setUpClass() throws Exception {
		fixtureProject = new JdtUnitFixtureProject();
		fixtureProject.setUp();

		fixture = fixtureProject.addCompilationUnit(CLASS_FIXTURE_NAME);
		fixture.addDefaultFixtureMethod();
	}

	@AfterAll
	public static void tearDownClass() throws CoreException {
		fixtureProject.tearDown();
	}

	@AfterEach
	public void tearDownTest() throws Exception {
		fixture.clear(true);
	}

	protected void setJavaVersion(String javaVersion) {
		fixtureProject.setJavaVersion(javaVersion);
	}

	protected void addDependency(String groupId, String artifactId, String version) throws Exception {
		IClasspathEntry classPathEntry = RulesTestUtil.generateMavenEntryFromDepedencyString(groupId, artifactId,
				version);
		fixtureProject.addClasspathEntry(classPathEntry);
	}

	protected void setVisitor(AbstractASTRewriteASTVisitor visitor) {
		this.visitor = visitor;
	}

	protected void assertNoChange(String original) throws JavaModelException, BadLocationException, JdtUnitException {
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(original), methodBlock);
	}

	protected void assertChange(String original, String expected)
			throws JavaModelException, BadLocationException, JdtUnitException {
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(expected), methodBlock);
	}

	protected void addImports(Class<?>... classes) throws Exception {
		for (Class<?> c : classes) {
			fixture.addImport(c.getName());
		}
	}
}
