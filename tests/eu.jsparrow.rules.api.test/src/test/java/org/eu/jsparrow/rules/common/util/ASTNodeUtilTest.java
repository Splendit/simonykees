package org.eu.jsparrow.rules.common.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

@SuppressWarnings("nls")
class ASTNodeUtilTest {

	@Test
	void findSingletonListElement_shouldFindVariableDeclarationStatement() throws Exception {
		String code = "int i = 0;\n";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);
		Optional<VariableDeclarationStatement> optionalDeclaration = ASTNodeUtil
			.findSingletonListElement(block.statements(), VariableDeclarationStatement.class);
		assertTrue(optionalDeclaration.isPresent());
	}

	@Test
	void findSingletonListElement_shouldNotFindIfStatement() throws Exception {
		String code = "int i = 0;\n";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);
		Optional<IfStatement> optionalDeclaration = ASTNodeUtil
			.findSingletonListElement(block.statements(), IfStatement.class);
		assertFalse(optionalDeclaration.isPresent());
	}

	@Test
	void findSingletonListElement_shouldNotFindAnyStatement() throws Exception {
		String code = ""
				+ "		int i = 0;\n"
				+ "		int j = 0;\n";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);
		Optional<Statement> optionalDeclaration = ASTNodeUtil.findSingletonListElement(block.statements(),
				Statement.class);
		assertFalse(optionalDeclaration.isPresent());
	}

	@Test
	void findListElementBefore_shouldFindVariableDeclarationStatement() throws Exception {
		String code = ""
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);
		Optional<VariableDeclarationStatement> optionalDeclaration = ASTNodeUtil
			.findListElementBefore(block.statements(), (IfStatement) block.statements()
				.get(1), VariableDeclarationStatement.class);
		assertTrue(optionalDeclaration.isPresent());
	}

	@Test
	void findListElementBefore_shouldNotFindIfStatement() throws Exception {
		String code = ""
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);
		Optional<IfStatement> optionalDeclaration = ASTNodeUtil
			.findListElementBefore(block.statements(), (IfStatement) block.statements()
				.get(1), IfStatement.class);
		assertFalse(optionalDeclaration.isPresent());
	}

	@Test
	void findListElementBefore_shouldNotFindAnyStatement() throws Exception {
		String code = ""
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);
		Optional<Statement> optionalDeclaration = ASTNodeUtil.findListElementBefore(block.statements(),
				(IfStatement) block.statements()
					.get(0),
				Statement.class);
		assertFalse(optionalDeclaration.isPresent());
	}

	@Test
	void findListElementAfter_shouldFindIfStatement() throws Exception {
		String code = ""
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);
		Optional<IfStatement> optionalDeclaration = ASTNodeUtil
			.findListElementAfter(block.statements(), (VariableDeclarationStatement) block.statements()
				.get(0), IfStatement.class);
		assertTrue(optionalDeclaration.isPresent());
	}

	@Test
	void findListElementAfter_shouldNotFindVariableDeclarationStatement() throws Exception {
		String code = ""
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);
		Optional<VariableDeclarationStatement> optionalDeclaration = ASTNodeUtil
			.findListElementAfter(block.statements(), (VariableDeclarationStatement) block.statements()
				.get(0), VariableDeclarationStatement.class);
		assertFalse(optionalDeclaration.isPresent());
	}

	@Test
	void findListElementAfter_shouldNotFindAnyStatement() throws Exception {
		String code = ""
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);
		Optional<Statement> optionalDeclaration = ASTNodeUtil.findListElementAfter(block.statements(),
				(IfStatement) block.statements()
					.get(0),
				Statement.class);
		assertFalse(optionalDeclaration.isPresent());
	}

	@Test
	void findSingleInvocationArgument_shouldFindExpression() throws Exception {
		String code = "useNumber(0)";
		MethodInvocation methodInvocation = (MethodInvocation) ASTNodeBuilder.createExpressionFromString(code);
		Optional<Expression> expression = ASTNodeUtil.findSingleInvocationArgument(methodInvocation);
		assertTrue(expression.isPresent());
	}

	@Test
	void findSingleInvocationArgument_shouldFindNumberLiteral() throws Exception {
		String code = "useNumber(0)";
		MethodInvocation methodInvocation = (MethodInvocation) ASTNodeBuilder.createExpressionFromString(code);
		Optional<NumberLiteral> numberLiteral = ASTNodeUtil.findSingleInvocationArgument(methodInvocation,
				NumberLiteral.class);
		assertTrue(numberLiteral.isPresent());
	}
	
	@Test
	void findSingleInvocationArgument_shouldNotFindSingleArgument() throws Exception {
		String code = "useNumber(0, 1)";
		MethodInvocation methodInvocation = (MethodInvocation) ASTNodeBuilder.createExpressionFromString(code);
		Optional<Expression> expression = ASTNodeUtil.findSingleInvocationArgument(methodInvocation);
		assertFalse(expression.isPresent());
	}

	@Test
	void findSingleBlockStatement_shouldFindStatement() throws Exception {
		String code = "useNumber(0);";
		Block block = (Block) ASTNodeBuilder.createBlockFromString(code);
		Optional<Statement> expression = ASTNodeUtil.findSingleBlockStatement(block);
		assertTrue(expression.isPresent());
	}

	@Test
	void findSingleBlockStatement_shouldFindExpressionStatement() throws Exception {
		String code = "useNumber(0);";
		Block block = (Block) ASTNodeBuilder.createBlockFromString(code);
		Optional<ExpressionStatement> expression = ASTNodeUtil.findSingleBlockStatement(block,
				ExpressionStatement.class);
		assertTrue(expression.isPresent());
	}

	@Test
	void findSingleBlockStatement_shouldNotFindSingleStatement() throws Exception {
		String code = ""
				+ "useNumber(0);\n"
				+ "useNumber(1);";
		Block block = (Block) ASTNodeBuilder.createBlockFromString(code);
		Optional<Statement> expression = ASTNodeUtil.findSingleBlockStatement(block);
		assertFalse(expression.isPresent());
	}

}
