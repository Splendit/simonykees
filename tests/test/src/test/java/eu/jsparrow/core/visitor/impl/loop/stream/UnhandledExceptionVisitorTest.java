package eu.jsparrow.core.visitor.impl.loop.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.sub.UnhandledExceptionVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

class UnhandledExceptionVisitorTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private static MethodDeclaration findUniqueMethodDeclaration(TypeDeclaration typeDeclaration, String methodName) {
		List<MethodDeclaration> methodDeclarations = Arrays.stream(typeDeclaration.getMethods())
			.filter(declaration -> declaration.getName()
				.getIdentifier()
				.equals(methodName))
			.collect(Collectors.toList());
		assertEquals(1, methodDeclarations.size());
		return methodDeclarations.get(0);
	}

	@Test
	public void visit_throwException_shouldContainUnhandledException() throws Exception {
		String typeContent = "" +
				"	void throwException() throws Exception {\n"
				+ "		Exception exception = new Exception();\n"
				+ "		throw exception;\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		MethodDeclaration methodDeclaration = findUniqueMethodDeclaration(defaultFixture.getTypeDeclaration(),
				"throwException");

		assertNotNull(methodDeclaration.resolveBinding());

		Block methodBody = methodDeclaration.getBody();
		ThrowStatement throwStatement = ASTNodeUtil.convertToTypedList(methodBody
			.statements(), ThrowStatement.class)
			.stream()
			.findFirst()
			.get();
		assertFalse(UnhandledExceptionVisitor.analyzeExceptionHandling(throwStatement, methodBody));
	}

	@Test
	public void visit_throwRuntimeException_shouldNotContainUnhandledException() throws Exception {
		String typeContent = "" +
				"	void throwRuntimeException() throws Exception {\n"
				+ "		RuntimeException runtimeException = new RuntimeException();\n"
				+ "		throw runtimeException;\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);

		MethodDeclaration methodDeclaration = findUniqueMethodDeclaration(defaultFixture.getTypeDeclaration(),
				"throwRuntimeException");
		
		Block methodBody = methodDeclaration.getBody();
		ThrowStatement throwStatement = ASTNodeUtil.convertToTypedList(methodBody
			.statements(), ThrowStatement.class)
			.stream()
			.findFirst()
			.get();
		assertTrue(UnhandledExceptionVisitor.analyzeExceptionHandling(throwStatement, methodBody));
	}
}
