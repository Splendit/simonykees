package eu.jsparrow.core.visitor.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

public class UseOffsetBasedStringMethodsASTVisitorTest extends UsesSimpleJDTUnitFixture {
	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new UseOffsetBasedStringMethodsASTVisitor());
	}
	
	@Test
	void visit_SubstringIndexOfCharacter_shouldTransform() throws Exception {
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).indexOf('d');\n";
		String expected = "" +
				"String str = \"Hello World!\";\n" +
				"int index=max(str.indexOf('d',6) - 6,-1);\n";
		
		assertChange(original, expected);
		
		List<ImportDeclaration> imports = fixture.getImports();
		assertEquals(1, imports.size());
		assertTrue(imports.get(0).isStatic());
		assertFalse(imports.get(0).isOnDemand());
		assertEquals("java.lang.Math.max", ((QualifiedName)imports.get(0).getName()).getFullyQualifiedName());
	}

	@Test
	void visit_SubstringIndexOfString_shouldTransform() throws Exception {
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).indexOf(\"d\");";
		String expected = "" +
				"String str = \"Hello World!\";\n" +
				"int index=max(str.indexOf(\"d\",6) - 6,-1);";

		assertChange(original, expected);
	}

	@Test
	void visit_SubstringLastIndexOfCharacter_shouldTransform() throws Exception {
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).lastIndexOf('d');";
		String expected = "" +
				"String str = \"Hello World!\";\n" +
				"int index=max(str.lastIndexOf('d',6) - 6,-1);";

		assertChange(original, expected);
	}

	@Test
	void visit_SubstringLastIndexOfString_shouldTransform() throws Exception {
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).lastIndexOf(\"d\");";
		String expected = "" +
				"String str = \"Hello World!\";\n" +
				"int index=max(str.lastIndexOf(\"d\",6) - 6,-1);";

		assertChange(original, expected);
	}

	@Test
	void visit_SubstringStartsWith_shouldTransform() throws Exception {
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"boolean startsWith = str.substring(6).startsWith(\"World\");";
		String expected = "" +
				"String str = \"Hello World!\";\n" +
				"boolean startsWith = str.startsWith(\"World\", 6);";

		assertChange(original, expected);
	}

	@Test
	void visit_MaxMethodAlreadyDeclared_shouldTransform() throws Exception {
		String original = "" +
				"class LocalClass {\n" +
				"	void max() {\n" +
				"	}\n" +
				"}\n" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).indexOf('d');";
		String expected = "" +
				"class LocalClass {\n" + 
				"	void max(){\n" + 
				"	}\n" + 
				"}\n" + 
				"String str=\"Hello World!\";\n" + 
				"int index=Math.max(str.indexOf('d',6) - 6,-1);";
		
		assertChange(original, expected);
		
		assertTrue(fixture.getImports().isEmpty());
	}

	@Test
	void visit_MathClassAndMaxMethodAlreadyDeclared_shouldTransform() throws Exception {
		String original = "" +
				"class Math {\n" +
				"	void max() {\n" +
				"	}\n" +
				"}\n" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).indexOf('d');\n";
		String expected = "" +
				"class Math {\n" + 
				"	void max(){\n" + 
				"	}\n" + 
				"}\n" + 
				"String str=\"Hello World!\";\n" + 
				"int index=java.lang.Math.max(str.indexOf('d',6) - 6,-1);\n";

		assertChange(original, expected);
		
		assertTrue(fixture.getImports().isEmpty());
	}
	
	@Test
	void visit_StaticImportOfMaxAlreadyExists_shouldTransform() throws Exception {
		boolean isStatic = true;
		boolean isOnDemand = false;		
		fixture.addImport(java.lang.Math.class.getName() + ".max", isStatic, isOnDemand);
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).indexOf('d');\n";
		String expected = "" +
				"String str = \"Hello World!\";\n" +
				"int index=max(str.indexOf('d',6) - 6,-1);\n";

		assertChange(original, expected);
	}
	
	@Test
	void visit_StaticImportOfMaxAlreadyExistsOnDemand_shouldTransform() throws Exception {
		boolean isStatic = true;
		boolean isOnDemand = true;		
		fixture.addImport(java.lang.Math.class.getName(), isStatic, isOnDemand);
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).indexOf('d');\n";
		String expected = "" +
				"String str = \"Hello World!\";\n" +
				"int index=max(str.indexOf('d',6) - 6,-1);\n";		
		assertChange(original, expected);
		
		List<ImportDeclaration> imports = fixture.getImports();
		assertEquals(1, imports.size());
		assertTrue(imports.get(0).isStatic());
		assertTrue(imports.get(0).isOnDemand());
		assertEquals("java.lang.Math", ((QualifiedName)imports.get(0).getName()).getFullyQualifiedName());
	}
	
	@Test
	void visit_SubstringWithTwoArguments_shouldNotTransform() throws Exception {
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6, 11).indexOf('d');";
		assertNoChange(original);
	}

	@Test
	void visit_IndexOfWithTwoArguments_shouldNotTransform() throws Exception {
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.substring(6).indexOf('d', 1);";
		assertNoChange(original);
	}

	@Test
	void visit_NonStringIndexOf_shouldNotTransform() throws Exception {
		String original = "" +
				"class HelloWorld {\n" +
				"	int indexOf(int character) {\n" +
				"		return 1;\n" +
				"	}\n" +
				"}\n" +
				"int index = new HelloWorld().indexOf('d');\n";
		assertNoChange(original);
	}

	@Test
	void visit_IndexOfExpressionNotMethodInvocation_shouldNotTransform() throws Exception {
		String original = "" +
				"String str = \"Hello World!\";\n" +
				"int index = str.indexOf('d');";
		assertNoChange(original);
	}

}
