package eu.jsparrow.core.visitor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.java16.javarecords.UseJavaRecordsASTVisitor;

@SuppressWarnings("nls")
public class UseJavaRecordsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new UseJavaRecordsASTVisitor());
		fixtureProject.setJavaVersion(JavaCore.VERSION_16);
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_LocalClassToRecord_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		if (true) {\n"
				+ "			class Point {\n"
				+ "				private final int x;\n"
				+ "				private final int y;\n"
				+ "\n"
				+ "				Point(int x, int y) {\n"
				+ "					this.x = x;\n"
				+ "					this.y = y;\n"
				+ "				}\n"
				+ "\n"
				+ "				public int x() {\n"
				+ "					return x;\n"
				+ "				}\n"
				+ "\n"
				+ "				public int y() {\n"
				+ "					return y;\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		if (true) {\n"
				+ "			record Point(int x, int y) {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Override
	protected void assertChange(String actual, String expected)
			throws JdtUnitException, JavaModelException, BadLocationException {
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actual);
		AbstractASTRewriteASTVisitor defaultVisitor = getDefaultVisitor();
		defaultVisitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(defaultVisitor);

		TypeDeclaration expectedNode = ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				expected);
		TypeDeclaration actualNode = defaultFixture.getTypeDeclaration();
		assertEquals(expectedNode.toString(), actualNode.toString());
	}
}
