package eu.jsparrow.core.visitor.sub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

class ExceptionHandlingAnalyzerTest extends UsesJDTUnitFixture {

	private static MethodDeclaration findUniqueMethodDeclaration(TypeDeclaration typeDeclaration, String methodName) {
		List<MethodDeclaration> methodDeclarations = Arrays.stream(typeDeclaration.getMethods())
			.filter(declaration -> declaration.getName()
				.getIdentifier()
				.equals(methodName))
			.collect(Collectors.toList());
		assertEquals(1, methodDeclarations.size());
		return methodDeclarations.get(0);
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/**
	 * Throwable should not be tolerated but at the moment it is tolerated and
	 * the result of this is a transformation to invalid code. Therefore this
	 * test is expected to fail for the @ValueSource entry "Throwable" as soon
	 * as the corresponding bug has been fixed.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"RuntimeException",
			"IllegalArgumentException",
			"Error",
			"AssertionError",
			"Throwable", // for this entry this test will fail as soon as the
							// corresponding bug has been fixed
	})
	void analyze_throwStatement_shouldReturnTrue(String errorName) throws Exception {
		String methodWithThrowStatement = String.format("" +
				"		void methodWithThrowStatement() {\n"
				+ "			%s e = new %s();\n"
				+ "			throw e;\n"
				+ "		}", errorName, errorName);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithThrowStatement);
		MethodDeclaration methodDeclaration = findUniqueMethodDeclaration(defaultFixture.getTypeDeclaration(),
				"methodWithThrowStatement");
		Block methodBody = methodDeclaration.getBody();
		ThrowStatement throwStatement = ASTNodeUtil.convertToTypedList(methodBody.statements(), ThrowStatement.class)
			.get(0);
		assertTrue(ExceptionHandlingAnalyzer.checkThrowStatement(methodBody, throwStatement));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Exception",
			"java.io.IOException",
			"UndefinedExcetion",
	// "Throwable",
	// at the moment this test would fail for "Throwable" but it will pass as
	// soon as as the corresponding bug has been fixed.
	})
	void analyze_throwStatement_shouldReturnFalse(String errorName) throws Exception {
		String methodWithThrowStatement = String.format("" +
				"		void methodWithThrowStatement() throws %s {\n"
				+ "			%s e = new %s();\n"
				+ "			throw e;\n"
				+ "		}", errorName, errorName, errorName);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithThrowStatement);
		MethodDeclaration methodDeclaration = findUniqueMethodDeclaration(defaultFixture.getTypeDeclaration(),
				"methodWithThrowStatement");
		Block methodBody = methodDeclaration.getBody();
		ThrowStatement throwStatement = ASTNodeUtil.convertToTypedList(methodBody.statements(), ThrowStatement.class)
			.get(0);
		assertFalse(ExceptionHandlingAnalyzer.checkThrowStatement(methodBody, throwStatement));
	}
}
