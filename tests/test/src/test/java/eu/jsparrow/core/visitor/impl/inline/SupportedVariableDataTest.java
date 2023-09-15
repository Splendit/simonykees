package eu.jsparrow.core.visitor.impl.inline;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

class SupportedVariableDataTest extends UsesJDTUnitFixture {

	private static VariableDeclarationStatement createVariableDeclarationStatement(String code)
			throws JdtUnitException {
		Block block = ASTNodeBuilder.createBlockFromString(code);
		Object firstSatement = block.statements()
			.get(0);
		return (VariableDeclarationStatement) firstSatement;
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_ReferenceAsReturnValue_shouldBeSupported() throws Exception {
		VariableDeclarationStatement variableDeclarationStatement = createVariableDeclarationStatement("int x = 1;");
		SupportedVariableData variableData = SupportedVariableData.extractVariableData(variableDeclarationStatement, "x")
			.orElse(null);
		assertNotNull(variableData);

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"int x;",
			"int y = 1;",
			"int x = 1, y = 1;"
	})
	void visit_returnExpressionEnclosingReference_shouldNotBeSupported(String code) throws Exception {
		VariableDeclarationStatement variableDeclarationStatement = createVariableDeclarationStatement(code);
		SupportedVariableData variableData = SupportedVariableData.extractVariableData(variableDeclarationStatement, "x")
				.orElse(null);
		
		assertNull(variableData);
	}
}
