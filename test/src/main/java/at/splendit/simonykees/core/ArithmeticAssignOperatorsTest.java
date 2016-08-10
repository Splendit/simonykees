package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import at.splendit.simonykees.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;

@RunWith(Parameterized.class)
public class ArithmeticAssignOperatorsTest extends AbstractTest {
	private String inputString;
	private String expectedString;

	private ASTParser astParser;

	@Before
	public void setUP() {
		astParser = ASTParser.newParser(AST.JLS8);
		astParser.setCompilerOptions(JavaCore.getDefaultOptions());
		astParser.setKind(ASTParser.K_EXPRESSION);
	}

	@After
	public void tearDown() {
		astParser = null;
	}

	public ArithmeticAssignOperatorsTest(String inputString, String expectedString) {
		this.inputString = inputString;
		this.expectedString = expectedString;
	}

	@Parameterized.Parameters
	public static Collection primeNumbers() {
		return Arrays.asList(new Object[][] { { "i = i + 3", "i += 3" }, { "i = i + 4 - 3", "i += 4 - 3" },
				{ "i = i + 4 + 3", "i += 4 + 3" }, { "i = 7 + 4 - 3", "i = 7 + 4 - 3" }
				 });
	}

	@Test
	public void arithmeticAssignmentASTVisitorTest() throws Exception {

		astParser.setSource(inputString.toCharArray());

		Assignment astRoot = (Assignment) astParser.createAST(null);
		ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

		// ASTPrinter.print(astRoot, 0);

		astRoot.accept(new ArithmethicAssignmentASTVisitor(astRewrite));
		Document document = new Document(inputString);
		TextEdit edits = astRewrite.rewriteAST(document, null);

		// computation of the new source code
		edits.apply(document);
		String newSource = document.get();

		assertNotNull(newSource);
		assertEquals(expectedString, newSource);

	}

}
