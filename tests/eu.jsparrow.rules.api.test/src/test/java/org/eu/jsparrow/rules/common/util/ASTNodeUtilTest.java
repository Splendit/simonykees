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

	private static <T extends Statement> T getStatementAt(Block block, int index, Class<T> type) {
		return type.cast(block.statements()
			.get(index));
	}

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
	void visit_shouldFindPreviousVariableDeclaration() throws Exception {
		String code = ""
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);

		IfStatement ifStatement = getStatementAt(block, 1, IfStatement.class);
		assertNotNull(ASTNodeUtil
			.findPreviousStatementInBlock(ifStatement, VariableDeclarationStatement.class)
			.orElse(null));
	}

	@Test
	void visit_shouldNotFindPreviousIf() throws Exception {
		String code = ""
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);

		IfStatement ifStatement = getStatementAt(block, 1, IfStatement.class);
		assertNull(ASTNodeUtil
			.findPreviousStatementInBlock(ifStatement, IfStatement.class)
			.orElse(null));
	}

	@Test
	void visit_shouldNotFindAnyPreviousStatement() throws Exception {
		String code = ""
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);

		IfStatement ifStatement = getStatementAt(block, 0, IfStatement.class);
		assertNull(ASTNodeUtil.findPreviousStatementInBlock(ifStatement, Statement.class)
			.orElse(null));
	}

	@Test
	void visit_shouldNotFindParentForPreviousStatement() throws Exception {
		String code = "" +
				"		if (condition) x = 1;";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);

		IfStatement ifStatement = getStatementAt(block, 0, IfStatement.class);
		Statement thenStatement = ifStatement.getThenStatement();
		assertNull(ASTNodeUtil.findPreviousStatementInBlock(thenStatement, Statement.class)
			.orElse(null));
	}

	@Test
	void visit_shouldFindSubsequentIf() throws Exception {
		String code = ""
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);

		VariableDeclarationStatement declarationStatement = getStatementAt(block, 0,
				VariableDeclarationStatement.class);
		assertNotNull(ASTNodeUtil.findSubsequentStatementInBlock(declarationStatement, IfStatement.class)
			.orElse(null));
	}

	@Test
	void visit_shouldNotFindSubsequentVariableDeclaration() throws Exception {
		String code = ""
				+ "		boolean condition = true;\n"
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);

		VariableDeclarationStatement declarationStatement = getStatementAt(block, 0,
				VariableDeclarationStatement.class);
		assertNull(ASTNodeUtil
			.findSubsequentStatementInBlock(declarationStatement, VariableDeclarationStatement.class)
			.orElse(null));

	}

	@Test
	void visit_shouldNotFindAnySubsequentStatement() throws Exception {
		String code = ""
				+ "		if (condition) {\n"
				+ "		}";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);

		IfStatement ifStatement = getStatementAt(block, 0, IfStatement.class);
		assertNull(ASTNodeUtil.findSubsequentStatementInBlock(ifStatement, Statement.class)
			.orElse(null));
	}

	@Test
	void visit_shouldNotFindParentForSubsequentStatement() throws Exception {
		String code = "" +
				"		if (condition) x = 1;";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);

		IfStatement ifStatement = getStatementAt(block, 0, IfStatement.class);
		Statement thenStatement = ifStatement.getThenStatement();
		assertNull(ASTNodeUtil.findSubsequentStatementInBlock(thenStatement, Statement.class)
			.orElse(null));
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
