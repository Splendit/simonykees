package eu.jsparrow.core.visitor.impl.entryset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.junit.jupiter.api.Test;

class KeyVariableDeclarationDataTest {

	@SuppressWarnings("deprecation")
	@Test
	void test_shouldThrowUnsupportedDimensionException() throws Exception {
		AST ast = AST.newAST(4, false);
		SingleVariableDeclaration newSingleVariableDeclaration = ast.newSingleVariableDeclaration();
		newSingleVariableDeclaration.setExtraDimensions(2);

		assertEquals(2, newSingleVariableDeclaration.getExtraDimensions());
		assertThrows(UnsupportedDimensionException.class,
				() -> KeyVariableDeclarationData.extractKeyVariableDeclarationData(newSingleVariableDeclaration));
	}

	@SuppressWarnings("unchecked")
	@Test
	void test_shouldHaveTwoDimensions() throws Exception {
		AST ast = AST.newAST(8, false);
		SingleVariableDeclaration newSingleVariableDeclaration = ast.newSingleVariableDeclaration();
		@SuppressWarnings("rawtypes")
		List extraDimensions = newSingleVariableDeclaration.extraDimensions();
		extraDimensions.add(ast.newDimension());
		extraDimensions.add(ast.newDimension());

		assertEquals(2, newSingleVariableDeclaration.getExtraDimensions());
		KeyVariableDeclarationData data = KeyVariableDeclarationData
			.extractKeyVariableDeclarationData(newSingleVariableDeclaration);
		assertEquals(2, data.getKeyExtraDimensions()
			.get()
			.size());
	}
	
	
	@Test
	void test_shouldHaveNoDimension() throws Exception {
		AST ast = AST.newAST(8, false);
		SingleVariableDeclaration newSingleVariableDeclaration = ast.newSingleVariableDeclaration();		
		assertEquals(0, newSingleVariableDeclaration.getExtraDimensions());
		KeyVariableDeclarationData data = KeyVariableDeclarationData
			.extractKeyVariableDeclarationData(newSingleVariableDeclaration);
		
		assertNull(data.getKeyExtraDimensions().orElse(null));
	}
}
