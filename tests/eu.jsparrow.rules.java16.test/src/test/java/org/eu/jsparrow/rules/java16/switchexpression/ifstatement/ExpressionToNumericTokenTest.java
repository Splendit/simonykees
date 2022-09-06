package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;
// eu.jsparrow.rules.java16.switchexpression.ifstatement;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ExpressionToNumericToken;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;

class ExpressionToNumericTokenTest extends UsesJDTUnitFixture {

	private static List<NumberLiteral> collectNumberLiterals(ASTNode node) {
		List<NumberLiteral> numberLiterals = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(NumberLiteral node) {
				numberLiterals.add(node);
				return false;
			}
		};
		node.accept(visitor);
		return numberLiterals;
	}

	// TODO: replace by a method collection only PrefixExpression nodes,
	private static List<Expression> collectExpressions(ASTNode node) {
		List<Expression> expressions = new ArrayList<>();

		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(NumberLiteral node) {
				expressions.add(node);
				return false;
			}

			@Override
			public boolean visit(PrefixExpression node) {
				expressions.add(node);
				return false;
			}
		};
		node.accept(visitor);
		return expressions;
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

	// TODO: replace by corresponding parameterized test
	@Test
	void test_findNumericTokensInInfix() throws Exception {
		String original = ""
				+ "	void numbersAsList() {\n"
				+ "		Object o = java.util.Arrays.asList(-1, - 1, - -1, +- -1, - - -1, + 1, +- 1, - +1, + + 1);\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();

		List<Integer> numericTokens = collectExpressions(typeDeclaration).stream()
			.map(ExpressionToNumericToken::expressionToInteger)
			.filter(Optional<Integer>::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		assertEquals(java.util.Arrays.asList(-1, -1, 1, 1, -1, 1, -1, -1, 1), numericTokens);

	}

	// TODO: extend to parameterized test
	@Test
	void test_expressionToInteger_shouldBeMinusOne() throws Exception {
		String original = "int x =  - + - (- 1);";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		List<Expression> expressions = collectExpressions(typeDeclaration);
		assertEquals(1, expressions.size());
		Integer minusOne = ExpressionToNumericToken.expressionToInteger(expressions.get(0))
			.orElse(null);
		assertNotNull(minusOne);
		assertEquals(-1, minusOne.intValue());
	}

	// TODO: extend to parameterized test
	@Test
	void test_expressionToInteger_shouldBeWithoutSign() throws Exception {
		String original = "int x =  +1;";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		List<Expression> expressions = collectExpressions(typeDeclaration);
		assertEquals(1, expressions.size());
		Integer oneUnsigned = ExpressionToNumericToken.expressionToInteger(expressions.get(0))
			.orElse(null);
		assertNotNull(oneUnsigned);
		assertEquals(1, oneUnsigned.intValue());
	}

	// TODO: extend to parameterized test
	// with literals for !true, -'A', ~'A'
	@Test
	void test_expressionToInteger_shouldNotFindIntegerValue() throws Exception {
		String original = "Object o = !true;";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		List<Expression> expressions = collectExpressions(typeDeclaration);
		assertEquals(1, expressions.size());
		Optional<Integer> optionalInteger = ExpressionToNumericToken.expressionToInteger(expressions.get(0));
		assertFalse(optionalInteger.isPresent());
	}

	// TODO: extend to parameterized test
	@Test
	void test_expressionToInteger_shouldThrowNumberFormatExeption() throws Exception {
		String original = "Object o = 1.1;";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		List<NumberLiteral> expressions = collectNumberLiterals(typeDeclaration);
		assertEquals(1, expressions.size());
		Expression firstExpression = expressions.get(0);
		assertThrows(NumberFormatException.class, () -> {
			ExpressionToNumericToken.expressionToInteger(firstExpression);
		});
	}

}
