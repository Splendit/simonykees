package eu.jsparrow.core.visitor.sub;

import static eu.jsparrow.core.visitor.sub.VisitorSubTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

class UnhandledExceptionVisitorTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void analyzeExceptionHandling_throwException_shouldReturnFalse() throws Exception {
		String typeContent = "" +
				"	void throwException() throws Exception {\n"
				+ "		Exception exception = new Exception();\n"
				+ "		throw exception;\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ThrowStatement throwStatement = findUniqueThrowStatement(typeDeclaration);
		assertFalse(UnhandledExceptionVisitor.analyzeExceptionHandling(throwStatement, typeDeclaration));
	}

	@Test
	void analyzeExceptionHandling_throwRuntimeException_shouldReturnTrue() throws Exception {
		String typeContent = "" +
				"	void throwRuntimeException() throws Exception {\n"
				+ "		RuntimeException runtimeException = new RuntimeException();\n"
				+ "		throw runtimeException;\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ThrowStatement throwStatement = findUniqueThrowStatement(typeDeclaration);
		assertTrue(UnhandledExceptionVisitor.analyzeExceptionHandling(throwStatement, typeDeclaration));
	}

	@Test
	void analyzeExceptionHandling_closeInputStream_shouldReturnFalse() throws Exception {
		defaultFixture.addImport(java.io.FileInputStream.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		String typeContent = "" +
				"	void closeInputStream(FileInputStream inputStream) throws IOException {\n"
				+ "		inputStream.close();\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		MethodInvocation methodInvocation = findUniqueMethodInvocation(typeDeclaration);
		assertFalse(UnhandledExceptionVisitor.analyzeExceptionHandling(methodInvocation, typeDeclaration));
	}

	@Test
	void analyzeExceptionHandling_getObjectHashCode_shouldReturnTrue() throws Exception {
		String typeContent = "" +
				"	int getObjectHashCode(Object o) {\n"
				+ "		return o.hashCode();\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		MethodInvocation methodInvocation = findUniqueMethodInvocation(typeDeclaration);
		assertTrue(UnhandledExceptionVisitor.analyzeExceptionHandling(methodInvocation, typeDeclaration));
	}

	@Test
	void analyzeExceptionHandling_createFileInputStream_shouldReturnFalse() throws Exception {
		defaultFixture.addImport(java.io.File.class.getName());
		defaultFixture.addImport(java.io.FileInputStream.class.getName());
		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());

		String typeContent = "" +
				"	FileInputStream createFileInputStream(File file) throws FileNotFoundException  {\n"
				+ "		return new FileInputStream(file);\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ClassInstanceCreation classinstanceCreation = findUniqueClassInstanceCreation(typeDeclaration);
		assertFalse(UnhandledExceptionVisitor.analyzeExceptionHandling(classinstanceCreation, typeDeclaration));
	}

	@Test
	void analyzeExceptionHandling_createObject_shouldReturnTrue() throws Exception {
		String typeContent = "" +
				"	Object createObject() {\n"
				+ "		return new Object();\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ClassInstanceCreation classinstanceCreation = findUniqueClassInstanceCreation(typeDeclaration);
		assertTrue(UnhandledExceptionVisitor.analyzeExceptionHandling(classinstanceCreation, typeDeclaration));
	}

	@Test
	void analyzeExceptionHandling_unhandledCloseExceptionInTWRStatement_shouldReturnFalse() throws Exception {
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());

		String typeContent = "" +
				"	void unhandledCloseExceptionInTWRStatement(FileReader fileReader) throws IOException {\n"
				+ "		try (BufferedReader br = new BufferedReader(fileReader)) {\n"
				+ "\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TryStatement tryStatement = findUniqueTryStatement(typeDeclaration);
		assertFalse(UnhandledExceptionVisitor.analyzeExceptionHandling(tryStatement, typeDeclaration));
	}

	@Test
	void analyzeExceptionHandling_handledCloseExceptionInTWRStatement_shouldReturnTrue() throws Exception {
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());

		String typeContent = "" +
				"	void handledCloseExceptionInTWRStatement(FileReader fileReader) {\n"
				+ "		try (BufferedReader br = new BufferedReader(fileReader)) {\n"
				+ "\n"
				+ "		} catch (IOException e) {\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TryStatement tryStatement = findUniqueTryStatement(typeDeclaration);
		assertTrue(UnhandledExceptionVisitor.analyzeExceptionHandling(tryStatement, typeDeclaration));
	}

	@Test
	public void analyzeExceptionHandling_ResourceInstanceCreationWithUnHandledException_shouldReturnFalse()
			throws Exception {
		defaultFixture.addImport(java.io.File.class.getName());
		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());

		String typeContent = "" +
				"	void newResourceCreationWithException(File file) throws FileNotFoundException {\n"
				+ "		try (FileReaderWithCloseNotThrowingException reader = new FileReaderWithCloseNotThrowingException(file)) {\n"
				+ "\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	class FileReaderWithCloseNotThrowingException extends FileReader {\n"
				+ "\n"
				+ "		public FileReaderWithCloseNotThrowingException(File file) throws FileNotFoundException {\n"
				+ "			super(file);\n"
				+ "		}\n"
				+ "\n"
				+ "		@Override\n"
				+ "		public void close() {\n"
				+ "			try {\n"
				+ "				super.close();\n"
				+ "			} catch (IOException e) {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		MethodDeclaration newResourceCreationWithException = findUniqueMethodDeclaration(typeDeclaration,
				"newResourceCreationWithException");
				TryStatement tryStatement = findUniqueTryStatement(newResourceCreationWithException);
		assertFalse(UnhandledExceptionVisitor.analyzeExceptionHandling(tryStatement, newResourceCreationWithException));
	}
}
