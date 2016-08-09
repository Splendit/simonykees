package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.Test;

import at.splendit.simonykees.core.visitor.ArithmethicAssignmentASTVisitor;



public class ArithmeticAssignOperatorsTest extends AbstractTest {
	
	@Test
	public void veryStupidTest() throws Exception {
		String inputString = "i = i + 3 ";
		String expectedString = "i += 3";

		final ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setCompilerOptions(JavaCore.getDefaultOptions());
		
		astParser.setKind(ASTParser.K_EXPRESSION);
		
		astParser.setSource(inputString.toCharArray());

		Assignment astRoot = (Assignment) astParser.createAST(null);

		ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		
		//ASTPrinter.print(astRoot, 0);
		
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
