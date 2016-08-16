package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.Test;

public class StatementTest extends AbstractTest {

	@Test
	public void veryStupidTest() throws Exception {
		String inputString = "public class X {\n}";
		String expectedString = "public class Y {\n}";

		final ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setCompilerOptions(JavaCore.getDefaultOptions());

		astParser.setSource(inputString.toCharArray());

		CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);

		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());

		SimpleName oldName = ((TypeDeclaration) astRoot.types().get(0)).getName();
		SimpleName newName = astRoot.getAST().newSimpleName("Y");
		rewrite.replace(oldName, newName, null);

		Document document = new Document(inputString);

		TextEdit edits = rewrite.rewriteAST(document, null);

		// computation of the new source code
		edits.apply(document);
		String newSource = document.get();

		assertNotNull(newSource);
		assertEquals(expectedString, newSource);

	}

}
