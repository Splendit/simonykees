package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.SwitchHeaderExpressionVisitor;

class SwitchHeaderExpressionVisitorTest extends UsesJDTUnitFixture {

	private List<IfStatement> collectIfStatements(ASTNode node) {
		List<IfStatement> ifStatements = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(IfStatement node) {
				ifStatements.add(node);
				return false;
			}
		};
		node.accept(visitor);
		return ifStatements;
	}

	@BeforeEach
	void setUp() {
		setDefaultVisitor(new ReplaceMultiBranchIfBySwitchASTVisitor());
		setJavaVersion(JavaCore.VERSION_14);
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"value.equals(\"A\")",
			"\"A\".equals(value)",
			"value.equals(\"A\") || value.equals(\"a\")"
	})
	void visit_StringEqualsMethod_shouldFindSwitchHeaderExpression(String codeForIfExpression) throws Exception {
		String original = ""
				+ "	void exampleWithEqualsMethod(String value) {\n"
				+ "		if (" + codeForIfExpression + ") {\n"
				+ "		}\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();

		List<IfStatement> ifStatements = collectIfStatements(typeDeclaration);

		Expression expression = ifStatements.get(0)
			.getExpression();
		SwitchHeaderExpressionVisitor visitor;
		visitor = new SwitchHeaderExpressionVisitor();
		expression.accept(visitor);

		String switchHeaderExpresssionName = visitor.getSwitchHeaderExpression()
			.map(SimpleName::getIdentifier)
			.orElse(null);
		assertEquals("value", switchHeaderExpresssionName);
		ITypeBinding switchHeaderExpressionType = visitor.getSwitchHeaderExpressionType()
			.orElse(null);
		assertTrue(ClassRelationUtil.isContentOfType(switchHeaderExpressionType, java.lang.String.class.getName()));
	}

	public static Stream<Arguments> arguments_EqualsInfix() throws Exception {
		return Stream.of(
				Arguments.of(int.class, "value == 1"),
				Arguments.of(int.class, "1 == value"),
				Arguments.of(int.class, "value == 1 || value == 2"),
				Arguments.of(char.class, "value == 'A'"),
				Arguments.of(char.class, "'A' == value"),
				Arguments.of(char.class, "value == 'A' || value == 'a'"));

	}

	@ParameterizedTest
	@MethodSource("arguments_EqualsInfix")
	void visit_EqualsInfix_shouldFindSwitchHeaderExpression(Class<?> primitiveType, String codeForIfExpression)
			throws Exception {
		String primitiveTypeName = primitiveType.getName();
		String original = ""
				+ "	void exalpleWithInt(" + primitiveTypeName + " value) {\n"
				+ "		if (" + codeForIfExpression + ") {\n"
				+ "		}\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();

		List<IfStatement> ifStatements = collectIfStatements(typeDeclaration);

		Expression expression = ifStatements.get(0)
			.getExpression();
		SwitchHeaderExpressionVisitor visitor;
		visitor = new SwitchHeaderExpressionVisitor();
		expression.accept(visitor);
		String switchHeaderExpresssionName = visitor.getSwitchHeaderExpression()
			.map(SimpleName::getIdentifier)
			.orElse(null);
		assertEquals("value", switchHeaderExpresssionName);
		ITypeBinding switchHeaderExpressionType = visitor.getSwitchHeaderExpressionType()
			.orElse(null);
		assertTrue(ClassRelationUtil.isContentOfType(switchHeaderExpressionType, primitiveTypeName));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			// breaks the test:
			// "value == 1",
			// breaks the test:
			// "value == 1 || value2 == 1",
			"(value == 1 || value2 == 1)",
			"value == 1 && value2 == 1",
			"value == 1 & value2 == 1",
			"value == 1 | value2 == 1",
			"value == 1 ^ value2 == 1",
			"value != 1",
			"value < 1",
			"value <= 1",
			"value > 1",
			"value >= 1",
			"value + 1 == 1",
			"1 + 2 + 3 + value == 10",
			"value - 1 == 1",
			"value * 1 == 1",
			"value / 1 == 1",
			"value % 1 == 1",
			"-value == 1",
			"+value == 1",
			"~value == 1",
			"value == 1 == true",
	})
	void visit_NotSupportedOperator_shouldNotFindSwitchHeaderExpression(String codeForIfExpression)
			throws Exception {
		String methodWithIf = "" +
				"		void notSupportedIfExpression(int value, int value2) {\n"
				+ "			if (" + codeForIfExpression + ") {\n"
				+ "			}\n"
				+ "		}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithIf);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();

		List<IfStatement> ifStatements = collectIfStatements(typeDeclaration);

		Expression expression = ifStatements.get(0)
			.getExpression();
		SwitchHeaderExpressionVisitor visitor;
		visitor = new SwitchHeaderExpressionVisitor();
		expression.accept(visitor);
		assertNull(visitor.getSwitchHeaderExpression()
			.orElse(null));
		assertNull(visitor.getSwitchHeaderExpressionType()
			.orElse(null));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"	void ifEmptyStringEqualsEmptyString() {\n"
					+ "		if (\"\".equals(\"\")) {\n"
					+ "		}\n"
					+ "	}",
			"" +
					"	void ifOneEqualsOne() {\n" +
					"		if (1 == 1) {\n" +
					"		}\n" +
					"	}",
			"" +
					"	void ifTrue() {\n" +
					"		if (true) {\n" +
					"		}\n" +
					"	}",
			"" +
					"	int value;\n" +
					"	void ifThisValueEqualsOne() {\n" +
					"		if (value == 1) {\n" +
					"		}\n" +
					"	}",
			"" +
					"	int value;\n" +
					"	void ifThisValueEqualsOne() {\n" +
					"		if (this.value == 1) {\n" +
					"		}" +
					"	}",
			"" +
					"	int getValue() {\n" +
					"		return 1;\n" +
					"	}\n" +
					"	void ifGetValueEqualsOne() {\n" +
					"		if (getValue() == 1) {\n" +
					"		}\n" +
					"	}",
			"" +
					"	int getValue() {\n" +
					"		return 1;\n" +
					"	}\n" +
					"	void ifOneEqualsGetValue() {\n" +
					"		if (1 == getValue()) {\n" +
					"		}\n" +
					"	}",
			"" +
					"	static class StaticNestedClass {\n"
					+ "		static boolean equals(String value) {\n"
					+ "			return false;\n"
					+ "		}\n"
					+ "		void useStaticEqualsMethod(String value) {\n"
					+ "			if(StaticNestedClass.equals(value)) {				\n"
					+ "			}\n"
					+ "		}\n"
					+ "	}"
	})
	void visit_NoLocalVariableName_shouldNotFindSwitchHeaderExpression(String codeExample)
			throws Exception {

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, codeExample);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();

		List<IfStatement> ifStatements = collectIfStatements(typeDeclaration);

		Expression expression = ifStatements.get(0)
			.getExpression();
		SwitchHeaderExpressionVisitor visitor;
		visitor = new SwitchHeaderExpressionVisitor();
		expression.accept(visitor);
		assertNull(visitor.getSwitchHeaderExpression()
			.orElse(null));
		assertNull(visitor.getSwitchHeaderExpressionType()
			.orElse(null));
	}

	@Test
	void visit_UndefinedVariable_shouldNotFindSwitchHeaderExpression() throws Exception {
		String original = ""
				+ "	void ifUndefinedVariableEqualsOne() {\n"
				+ "		if (value == 1) {\n"
				+ "		}\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();

		List<IfStatement> ifStatements = collectIfStatements(typeDeclaration);

		Expression expression = ifStatements.get(0)
			.getExpression();
		SwitchHeaderExpressionVisitor visitor;
		visitor = new SwitchHeaderExpressionVisitor();
		expression.accept(visitor);
		assertNull(visitor.getSwitchHeaderExpression()
			.orElse(null));
		assertNull(visitor.getSwitchHeaderExpressionType()
			.orElse(null));

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"	void ifStringEqualsIgnoreCase(String value) {\n" +
					"		if(value.equalsIgnoreCase(value)) {\n" +
					"		}\n" +
					"	}",
			"" +
					"	class NestedClassWithEqualsMethod {\n"
					+ "		boolean equals(String value) {\n"
					+ "			return false;\n"
					+ "		}\n"
					+ "		void ifStringEqualsIgnoreCase(String value) {\n"
					+ "			if (equals(value)) {\n"
					+ "			}\n"
					+ "		}\n"
					+ "	}",
			"" +
					"	void useEqualsWithoutArguments() {\n"
					+ "		class LocalClassWithEqualsMethod {\n"
					+ "			boolean equals() {\n"
					+ "				return false;\n"
					+ "			}\n"
					+ "		}\n"
					+ "		LocalClassWithEqualsMethod value = new LocalClassWithEqualsMethod();\n"
					+ "		if (value.equals()) {\n"
					+ "		}\n"
					+ "	}",
	})
	void visit_UnsupportedMethodInvocation_shouldNotFindSwitchHeaderExpression(String codeExample) throws Exception {
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, codeExample);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();

		List<IfStatement> ifStatements = collectIfStatements(typeDeclaration);
		Expression expression = ifStatements.get(0)
			.getExpression();
		SwitchHeaderExpressionVisitor visitor;
		visitor = new SwitchHeaderExpressionVisitor();
		expression.accept(visitor);
		assertNull(visitor.getSwitchHeaderExpression()
			.orElse(null));
		assertNull(visitor.getSwitchHeaderExpressionType()
			.orElse(null));

	}
}
