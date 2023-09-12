package eu.jsparrow.core.visitor.impl.inline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

class SupportedReferenceAnalyzerTest extends UsesJDTUnitFixture {

	static SupportedReferenceAnalyzer createAnalyzer(Block block) throws JdtUnitException {
		VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) block.statements()
			.get(0);
		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) declarationStatement.fragments()
			.get(0);
		Expression initializer = declarationFragment.getInitializer();
		Statement followingStatement = (Statement) block.statements()
			.get(1);

		return new SupportedReferenceAnalyzer(followingStatement, initializer);
	}

	static SimpleName findUniqueSimpleName(Statement statement, String expectedIdentifier) {
		List<SimpleName> simpleNames = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(SimpleName node) {
				String identifier = node.getIdentifier();
				if (identifier.equals(expectedIdentifier)) {
					simpleNames.add(node);
				}
				return false;
			}

		};
		statement.accept(visitor);
		assertEquals(1, simpleNames.size());
		return simpleNames.get(0);
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_ReferenceAsReturnValue_shouldBeSupported() throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		int result = 1 + 2;\n" +
				"		return result;");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "result");
		assertNotNull(simpleName);
		assertTrue(analyzer.isSupportedReference(simpleName));

	}

	@Test
	void visit_ReferenceInThrowStatement_shouldBeSupported() throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		Exception exception = new Exception(message);\n" +
				"		throw exception;");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "exception");
		assertNotNull(simpleName);
		assertTrue(analyzer.isSupportedReference(simpleName));

	}

	@Test
	void visit_ReferenceAsSwitchStatementExpression_shouldNotBeSupported() throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		int x = 100;\n" +
				"		switch (x) {\n" +
				"		case 1:\n" +
				"			System.out.println(\"case 1\");\n" +
				"		}");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "x");
		assertNotNull(simpleName);
		assertFalse(analyzer.isSupportedReference(simpleName));

	}

	@Test
	void visit_ReferenceUsedInAssignment_shouldBeSupported() throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		int x = p + q;\n" +
				"		y = x;");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "x");
		assertNotNull(simpleName);
		assertTrue(analyzer.isSupportedReference(simpleName));
	}

	@Test
	void visit_ReferenceUsedInAssignment_shouldNotBeSupported() throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		int x = p + q;\n" +
				"		System.out.println();" +
				"		y = x;");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(2), "x");
		assertNotNull(simpleName);
		assertFalse(analyzer.isSupportedReference(simpleName));
	}

	@Test
	void visit_ReferenceUsedInAssignmentAsReturnValue_shouldNotBeSupported() throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		int x = p + q;\n" +
				"		return (y = x);");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "x");
		assertNotNull(simpleName);
		assertFalse(analyzer.isSupportedReference(simpleName));
	}

	@Test
	void visit_ReferenceUsedInVariableDeclarationStatement_shouldBeSupported() throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		int x = p + q;\n" +
				"		int y = x;");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "x");
		assertNotNull(simpleName);
		assertTrue(analyzer.isSupportedReference(simpleName));
	}

	@Test
	void visit_ReferenceUsedInVariableDeclarationStatementWithTwoFragments_shouldNotBeSupported() throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		int x = p + q;\n" +
				"		int y = x, z = p;");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "x");
		assertNotNull(simpleName);
		assertFalse(analyzer.isSupportedReference(simpleName));
	}

	@Test
	void visit_ReferenceUsedInVariableDeclarationStatement_shouldNotBeSupported() throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		int x = p + q;\n" +
				"		System.out.println();" +
				"		int y = x;");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(2), "x");
		assertNotNull(simpleName);
		assertFalse(analyzer.isSupportedReference(simpleName));
	}

	@Test
	void visit_ReferenceUsedInVariableDeclarationExpression_shouldNotBeSupported() throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		int x = p + q;\n" +
				"		for(int i = x; true; ) {\n" +
				"			break;\n" +
				"		}\n");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "x");
		assertNotNull(simpleName);
		assertFalse(analyzer.isSupportedReference(simpleName));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"this",
			"x",
			"xWrapper.value",
			"this.x",
			"super.x",
			"X.class",
			"1",
			"\"1\"",
			"'1'",
			"+x",
			"-x",
			"!x",
			"~x",
	})
	void visit_ReferenceAsMethodArgument_shouldBeSupported(String initializer) throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		Oject o = " + initializer + ";\n" +
				"		use(o);");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "o");
		assertNotNull(simpleName);
		assertTrue(analyzer.isSupportedReference(simpleName));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"EnclosingType.this",
			"EnclosingType.this.x",
			"method()",
			"xStructure.xWrapper.value",
			"this.xWrapper.value",
			"super.xWrapper.value",
			"pkg1.X.class",
			"1 + 1",
			"++x",
			"--x",
			"-~x"
	})
	void visit_ReferenceAsMethodArgument_shouldNotBeSupported(String initializer) throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		Oject o = " + initializer + ";\n" +
				"		use(o);");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "o");
		assertNotNull(simpleName);
		assertFalse(analyzer.isSupportedReference(simpleName));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"method(s)",
			"this.method(s)",
			"xField.method(s)",
			"super.method(s)",
			"new X(s)"

	})
	void visit_returnExpressionEnclosingReference_shouldBeSupported(String enclosingExpression) throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		String s = \"s\";\n" +
				"		return " + enclosingExpression + ";");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "s");
		assertNotNull(simpleName);
		assertTrue(analyzer.isSupportedReference(simpleName));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"method(s, sOther)",
			"X.<String> method(s)",
			"EnclosingType.this.method(s)",
			"this.xField.method(s)",
			"xStructure.xField.method(s)",
			"X.super.method(s)",
			"super.method(s, sOther)",
			"super.<String>method(s)",
			"new X(s, sOther)",
			"new X<String>(s)",
			"new pkg.X(s)",
			"this.new X(s)"
	})
	void visit_returnExpressionEnclosingReference_shouldNotBeSupported(String enclosingExpression) throws Exception {
		Block block = ASTNodeBuilder.createBlockFromString("" +
				"		String s = \"s\";\n" +
				"		return " + enclosingExpression + ";");

		SupportedReferenceAnalyzer analyzer = createAnalyzer(block);
		SimpleName simpleName = findUniqueSimpleName((Statement) block.statements()
			.get(1), "s");
		assertNotNull(simpleName);
		assertFalse(analyzer.isSupportedReference(simpleName));
	}
}
