package org.eu.jsparrow.rules.java16.javarecords;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
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
	public void visit_ComplexCanonicalConstructor_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClass() {\n"
				+ "		class LocalClass {\n"
				+ "			private final int x, y, z;\n"
				+ "\n"
				+ "			public LocalClass(int x, int y, int z) {\n"
				+ "\n"
				+ "				if (x < 100 && y < 100 && z < 100) {\n"
				+ "					this.x = x;\n"
				+ "					this.y = y;\n"
				+ "					this.z = z;\n"
				+ "				} else {\n"
				+ "					this.x = x / 100;\n"
				+ "					this.y = y / 100;\n"
				+ "					this.z = z / 100;\n"
				+ "				}\n"
				+ "			}\n"
				+ "\n"
				+ "			public LocalClass(byte x, byte y, byte z) {\n"
				+ "				this((int) x, (int) y, (int) z);\n"
				+ "			}\n"
				+ "\n"
				+ "			public LocalClass() {\n"
				+ "				this((byte) 0, (byte) 0, (byte) 0);\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClass() {\n"
				+ "		record LocalClass(int x, int y, int z) {\n"
				+ "			;\n" //  TODO: discuss Unexpected empty statement
				+ "			public LocalClass(int x, int y, int z) {\n"
				+ "\n"
				+ "				if (x < 100 && y < 100 && z < 100) {\n"
				+ "					this.x = x;\n"
				+ "					this.y = y;\n"
				+ "					this.z = z;\n"
				+ "				} else {\n"
				+ "					this.x = x / 100;\n"
				+ "					this.y = y / 100;\n"
				+ "					this.z = z / 100;\n"
				+ "				}\n"
				+ "			}\n"
				+ "\n"
				+ "			public LocalClass(byte x, byte y, byte z) {\n"
				+ "				this((int) x, (int) y, (int) z);\n"
				+ "			}\n"
				+ "\n"
				+ "			public LocalClass() {\n"
				+ "				this((byte) 0, (byte) 0, (byte) 0);\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "	}";

		assertChange(original, expected);
	}
	
	@Test
	public void visit_LocalClassToRecord_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n" +
				"		class Point {\n" +
				"			private final int x;\n" +
				"			private final int y;\n" +
				"\n" +
				"			Point(int x, int y) {\n" +
				"				this.x = x;\n" +
				"				this.y = y;\n" +
				"			}\n" +
				"\n" +
				"			public int x() {\n" +
				"				return x;\n" +
				"			}\n" +
				"\n" +
				"			public int y() {\n" +
				"				return y;\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n" +
				"		record Point(int x, int y) {\n" +
				"		}\n" +
				"	}";

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
