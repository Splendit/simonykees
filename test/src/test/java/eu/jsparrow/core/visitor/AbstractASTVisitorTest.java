package eu.jsparrow.core.visitor;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import eu.jsparrow.jdtunit.JdtUnitException;
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

	protected Block createBlock(String string) throws Exception {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setSource(string.toCharArray());
		astParser.setKind(ASTParser.K_STATEMENTS);
		ASTNode result = astParser.createAST(null);
		if ((result.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED) {
			throw new Exception(String.format("Failed to parse '%s'.", string)); //$NON-NLS-1$
		}
		Block block = (Block) result;
		if (block.statements().isEmpty()) {
			throw new JdtUnitException("Can not create an empty block. There might be syntax errors"); //$NON-NLS-1$
		}
		return block;
	}

}
