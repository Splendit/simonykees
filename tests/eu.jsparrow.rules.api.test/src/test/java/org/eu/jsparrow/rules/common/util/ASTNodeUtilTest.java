package org.eu.jsparrow.rules.common.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Block;
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
	void findSingletonListElement_shouldNotFindVariableDeclarationStatement() throws Exception {
		String code = ""
				+ "		int i = 0;\n"
				+ "		int j = 0;\n";

		Block block = ASTNodeBuilder.createBlockFromString(code);
		assertNotNull(block);
		Optional<VariableDeclarationStatement> optionalDeclaration = ASTNodeUtil
			.findSingletonListElement(block.statements(), VariableDeclarationStatement.class);
		assertTrue(optionalDeclaration.isEmpty());
	}

}
