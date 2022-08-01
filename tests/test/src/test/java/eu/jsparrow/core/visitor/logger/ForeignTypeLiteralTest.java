package eu.jsparrow.core.visitor.logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.exception.visitor.UnresolvedTypeBindingException;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

class ForeignTypeLiteralTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setupTest() {
		setDefaultVisitor(new ReplaceWrongClassForLoggerASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Class<?> clazz = Object.class;",
			"Class<?> clazz = " + DEFAULT_TYPE_DECLARATION_NAME + "[].class;",
			""
					+ "	static class NestedClass {\n"
					+ "		Class<?> clazz = " + DEFAULT_TYPE_DECLARATION_NAME + ".class;\n"
					+ "	}",
			"" +
					"	Class<?> clazz = ExampleSubClass.class;\n" +
					"	static class ExampleSubClass extends " + DEFAULT_TYPE_DECLARATION_NAME + " {\n" +
					"	}"
	})
	void analyze_isForeignTypeLiteral_shouldReturnTrue(String codeContainingClassLiteral)
			throws Exception {
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, codeContainingClassLiteral);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		AbstractTypeDeclaration enclosingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(typeLiteral,
				AbstractTypeDeclaration.class);
		assertTrue(ForeignTypeLiteral.isForeignTypeLiteral(typeLiteral, enclosingTypeDeclaration,
				defaultFixture.getRootNode()));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Class<?> clazz = " + DEFAULT_TYPE_DECLARATION_NAME + ".class;",
			""
					+ "	static class NestedClass {\n"
					+ "		Class<?> clazz = NestedClass.class;\n"
					+ "	}",
			""
					+ "	static class NestedClass<T> {\n"
					+ "		Class<?> clazz = NestedClass.class;\n"
					+ "	}	"
	})
	void analyze_isForeignTypeLiteral_shouldReturnFalse(String fieldDeclaration)
			throws Exception {
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, fieldDeclaration);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		AbstractTypeDeclaration enclosingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(typeLiteral,
				AbstractTypeDeclaration.class);
		assertFalse(ForeignTypeLiteral.isForeignTypeLiteral(typeLiteral, enclosingTypeDeclaration,
				defaultFixture.getRootNode()));
	}

	@Test
	void analyze_isForeignTypeLiteral_shouldThrowException()
			throws Exception {
		String codeContainingClassLiteral = "Class<?> clazz = UnknownClass.class;";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, codeContainingClassLiteral);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		AbstractTypeDeclaration enclosingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(typeLiteral,
				AbstractTypeDeclaration.class);
		assertThrows(UnresolvedTypeBindingException.class,
				() -> ForeignTypeLiteral.isForeignTypeLiteral(typeLiteral, enclosingTypeDeclaration,
						defaultFixture.getRootNode()));
	}
}
