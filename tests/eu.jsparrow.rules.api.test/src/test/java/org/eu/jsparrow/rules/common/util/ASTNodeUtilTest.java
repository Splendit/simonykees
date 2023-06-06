package org.eu.jsparrow.rules.common.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
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
	void findSingletonListElement_shouldFindIfStatement() throws Exception {
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
	void findListElement_shouldFindVariableDeclarationStatementBefore() throws Exception {
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
	void findListElement_shouldNotFindIfStatementBefore() throws Exception {
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
	void findListElement_shouldNotFindAnyStatementBefore() throws Exception {
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
}
