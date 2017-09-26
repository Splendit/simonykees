package eu.jsparrow.core.visitor;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import eu.jsparrow.jdtunit.JdtUnitFixture;

public abstract class AbstractASTVisitorTest {

	protected AbstractASTRewriteASTVisitor visitor;

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

	protected Block createBlock(String string) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setSource(string.toCharArray());
		astParser.setKind(ASTParser.K_STATEMENTS);
		return (Block) astParser.createAST(null);
	}

}
