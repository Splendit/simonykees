package eu.jsparrow.core.visitor.sub;

import static eu.jsparrow.core.visitor.sub.VisitorSubTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ExceptionHandlingAnalyzerTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"RuntimeException",
			"IllegalArgumentException",
			"Error",
			"AssertionError",
	})
	void analyze_throwStatement_shouldReturnTrue(String errorName) throws Exception {
		String methodWithThrowStatement = String.format("" +
				"		void methodWithThrowStatement() {\n"
				+ "			%s e = new %s();\n"
				+ "			throw e;\n"
				+ "		}", errorName, errorName);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithThrowStatement);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ThrowStatement throwStatement = findUniqueThrowStatement(typeDeclaration);
		assertTrue(ExceptionHandlingAnalyzer.checkThrowStatement(typeDeclaration, throwStatement));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Exception",
			"java.io.IOException",
			"UndefinedExcetion",
			"Throwable",
	})
	void analyze_throwStatement_shouldReturnFalse(String errorName) throws Exception {
		String methodWithThrowStatement = String.format("" +
				"		void methodWithThrowStatement() throws %s {\n"
				+ "			%s e = new %s();\n"
				+ "			throw e;\n"
				+ "		}", errorName, errorName, errorName);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithThrowStatement);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ThrowStatement throwStatement = findUniqueThrowStatement(typeDeclaration);
		assertFalse(ExceptionHandlingAnalyzer.checkThrowStatement(typeDeclaration, throwStatement));
	}

	@Test
	void analyze_ThrowExceptionInCatchClause_shouldReturnFalse() throws Exception {
		String methodWithThrowStatement = "" +
				"	void throwExceptionInCatchClause() throws Exception {\n"
				+ "		try {\n"
				+ "			throw new Exception();\n"
				+ "		} catch (Exception e) {\n"
				+ "			throw new Exception(e);\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithThrowStatement);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ThrowStatement throwStatement = findUniqueThrowStatement(
				typeDeclaration, throwStm -> {
					assertEquals(Block.STATEMENTS_PROPERTY, throwStm.getLocationInParent());
					Block block = (Block) throwStm.getParent();
					if (block.getLocationInParent() != CatchClause.BODY_PROPERTY) {
						return false;
					}
					return true;
				});
		assertFalse(ExceptionHandlingAnalyzer.checkThrowStatement(typeDeclaration, throwStatement));
	}
	
	
	@Test
	void analyze_ThrowExceptionInFinallyClause_shouldReturnFalse() throws Exception {
		String methodWithThrowStatement = "" +
				"	void throwExceptionInFinallyClause() throws Exception {\n"
				+ "		try {\n"
				+ "			throw new Exception();\n"
				+ "		} finally {\n"
				+ "			throw new Exception();\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithThrowStatement);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ThrowStatement throwStatement = findUniqueThrowStatement(
				typeDeclaration, throwStm -> {
					assertEquals(Block.STATEMENTS_PROPERTY, throwStm.getLocationInParent());
					Block block = (Block) throwStm.getParent();
					if (block.getLocationInParent() != TryStatement.FINALLY_PROPERTY) {
						return false;
					}
					return true;
				});
		assertFalse(ExceptionHandlingAnalyzer.checkThrowStatement(typeDeclaration, throwStatement));
	}

	@Test
	void analyze_throwStatementInTryCatchBlock_shouldReturnTrue() throws Exception {
		String methodWithThrowStatement = ""
				+ "	Exception exception = new Exception();"
				+ ""
				+ "	void throwStatementInTryCatch() {\n"
				+ "		try {\n"
				+ "			throw exception;\n"
				+ "		} catch (Exception e) {\n"
				+ "\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithThrowStatement);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ThrowStatement throwStatement = findUniqueThrowStatement(typeDeclaration);
		assertTrue(ExceptionHandlingAnalyzer.checkThrowStatement(typeDeclaration, throwStatement));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "		try {\n"
					+ "			throw new Exception();\n"
					+ "		} catch (UnknownException e) {\n"
					+ "		}",

			""
					+ "		try {\n"
					+ "			try {\n"
					+ "				throw new Exception();\n"
					+ "			} catch (UnknownException e) {\n"
					+ "			}\n"
					+ "		} catch (Exception e) {\n"
					+ "		}",
	})
	void analyze_tryStatementCatchingUnknownException_shouldReturnFalse(String code) throws Exception {
		String methodWithThrowStatement = ""
				+ "	void tryStatementCatchingUnknownException() throws Exception {\n"
				+ code + "\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithThrowStatement);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ThrowStatement throwStatement = findUniqueThrowStatement(typeDeclaration);
		assertFalse(ExceptionHandlingAnalyzer.checkThrowStatement(typeDeclaration, throwStatement));
	}

	@Test
	void analyze_TryCatchBlockIsExcludedAncestor_shouldReturnFalse() throws Exception {
		String methodWithThrowStatement = ""
				+ "	Exception exception = new Exception();"
				+ ""
				+ "	void throwStatementInTryCatch() {\n"
				+ "		try {\n"
				+ "			throw exception;\n"
				+ "		} catch (Exception e) {\n"
				+ "\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, methodWithThrowStatement);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ThrowStatement throwStatement = findUniqueThrowStatement(typeDeclaration);
		assertEquals(Block.STATEMENTS_PROPERTY, throwStatement.getLocationInParent());
		Block block = (Block) throwStatement.getParent();
		assertEquals(TryStatement.BODY_PROPERTY, block.getLocationInParent());
		TryStatement tryStatementAsExcludedAncestor = (TryStatement) block.getParent();
		assertFalse(ExceptionHandlingAnalyzer.checkThrowStatement(tryStatementAsExcludedAncestor, throwStatement));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"BufferedReader",
			"SubclassOfBufferedReader",
			"SubclassDeclaringCloseWithParameter",
	})
	void analyzeExceptionHandling_unhandledCloseExceptionInTWRStatement_shouldReturnFalse(String resourceClass)
			throws Exception {
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());

		String typeContent = String.format("" +
				"	void unhandledCloseExceptionInTWRStatement(FileReader fileReader) throws IOException {\n"
				+ "		try (%s br = new %s(fileReader)) {\n"
				+ "\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubclassOfBufferedReader extends BufferedReader {\n"
				+ "\n"
				+ "		public SubclassOfBufferedReader(Reader in) {\n"
				+ "			super(in);\n"
				+ "		}\n"
				+ "	}\n"
				+ "	class SubclassDeclaringCloseWithParameter extends BufferedReader {\n"
				+ "\n"
				+ "		public SubclassDeclaringCloseWithParameter(Reader in) {\n"
				+ "			super(in);\n"
				+ "		}\n"
				+ "		\n"
				+ "		public void close(boolean doit) {\n"
				+ "			if (!doit) {\n"
				+ "				return;\n"
				+ "			}\n"
				+ "			try {\n"
				+ "				super.close();\n"
				+ "			} catch (IOException e) {\n"
				+ "				e.printStackTrace();\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}", resourceClass, resourceClass);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		MethodDeclaration unhandledCloseExceptionInTWRStatement = findUniqueMethodDeclaration(typeDeclaration,
				"unhandledCloseExceptionInTWRStatement");
		TryStatement tryStatement = findUniqueTryStatement(unhandledCloseExceptionInTWRStatement);
		assertFalse(ExceptionHandlingAnalyzer.checkResourcesForAutoCloseException(typeDeclaration, tryStatement));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"SubclassOverridingCloseWithoutException",
			"SubclassOfSubclass",
	})
	void analyzeExceptionHandling_ImplicitCloseWithoutException_shouldReturnTrue(String resourceClass)
			throws Exception {
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());

		String typeContent = String.format("" +
				"	void implicitCloseWithoutException(FileReader fileReader) {\n"
				+ "		try (%s br = new %s(fileReader)) {\n"
				+ "\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubclassOverridingCloseWithoutException extends BufferedReader {\n"
				+ "\n"
				+ "		public SubclassOverridingCloseWithoutException(Reader in) {\n"
				+ "			super(in);\n"
				+ "\n"
				+ "		}\n"
				+ "\n"
				+ "		@Override\n"
				+ "		public void close() {\n"
				+ "			try {\n"
				+ "				super.close();\n"
				+ "			} catch (IOException e) {\n"
				+ "				e.printStackTrace();\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubclassOfSubclass extends SubclassOverridingCloseWithoutException {\n"
				+ "\n"
				+ "		public SubclassOfSubclass(Reader in) {\n"
				+ "			super(in);\n"
				+ "\n"
				+ "		}\n"
				+ "	}\n"
				+ "", resourceClass, resourceClass);

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		MethodDeclaration implicitCloseWithoutException = findUniqueMethodDeclaration(typeDeclaration,
				"implicitCloseWithoutException");
		TryStatement tryStatement = findUniqueTryStatement(implicitCloseWithoutException);
		assertTrue(ExceptionHandlingAnalyzer.checkResourcesForAutoCloseException(typeDeclaration, tryStatement));
	}

	@Test
	void analyzeExceptionHandling_tryStatementCatchingUnionType_shouldReturnTrue()
			throws Exception {

		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		defaultFixture.addImport(java.io.NotSerializableException.class.getName());
		String typeContent = "" +
				"	void tryStatementCatchingUnionType(FileReader fileReader) {\n"
				+ "\n"
				+ "		try {\n"
				+ "			throwFileNotFoundOrNotSerializableException(null);\n"
				+ "		} catch (NotSerializableException | FileNotFoundException e) {\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	void throwFileNotFoundOrNotSerializableException(Exception exc)\n"
				+ "			throws FileNotFoundException, NotSerializableException {\n"
				+ "		if (exc instanceof FileNotFoundException) {\n"
				+ "			throw (FileNotFoundException) exc;\n"
				+ "		}\n"
				+ "\n"
				+ "		if (exc instanceof NotSerializableException) {\n"
				+ "			throw (NotSerializableException) exc;\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		MethodDeclaration tryStatementCatchingUnionType = findUniqueMethodDeclaration(typeDeclaration,
				"tryStatementCatchingUnionType");
		MethodInvocation methodInvocationThrowingTwoExceptions = findUniqueMethodInvocation(
				tryStatementCatchingUnionType);
		assertTrue(ExceptionHandlingAnalyzer.checkMethodInvocation(tryStatementCatchingUnionType,
				methodInvocationThrowingTwoExceptions));
	}

	@Test
	void analyze_NestedTryStatements_shouldReturnTrue() throws Exception {

		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		defaultFixture.addImport(java.io.NotSerializableException.class.getName());
		String typeContent = "" +
				"	void nestedTryCatch() {\n"
				+ "		try {\n"
				+ "			try {\n"
				+ "				throwFileNotFoundOrNotSerializableException(null);\n"
				+ "			} catch (FileNotFoundException e) {\n"
				+ "			}\n"
				+ "		} catch (NotSerializableException e) {\n"
				+ "\n"
				+ "		}\n"
				+ "	}\n"
				+ "	\n"
				+ "	void throwFileNotFoundOrNotSerializableException(Exception exc)\n"
				+ "			throws FileNotFoundException, NotSerializableException {\n"
				+ "		if (exc instanceof FileNotFoundException) {\n"
				+ "			throw (FileNotFoundException) exc;\n"
				+ "		}\n"
				+ "\n"
				+ "		if (exc instanceof NotSerializableException) {\n"
				+ "			throw (NotSerializableException) exc;\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		MethodDeclaration nestedTryStatements = findUniqueMethodDeclaration(typeDeclaration,
				"nestedTryCatch");
		MethodInvocation methodInvocationThrowingTwoExceptions = findUniqueMethodInvocation(
				nestedTryStatements);
		assertTrue(ExceptionHandlingAnalyzer.checkMethodInvocation(typeDeclaration,
				methodInvocationThrowingTwoExceptions));
	}

	@Test
	void analyzeExceptionHandling_tryStatementCatchingException_shouldReturnTrue()
			throws Exception {

		defaultFixture.addImport(java.io.FileNotFoundException.class.getName());
		defaultFixture.addImport(java.io.NotSerializableException.class.getName());
		String typeContent = "" +
				"	void tryStatementCatchingUnionType(FileReader fileReader) {\n"
				+ "\n"
				+ "		try {\n"
				+ "			throwFileNotFoundOrNotSerializableException(null);\n"
				+ "		} catch (Exception e) {\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	void throwFileNotFoundOrNotSerializableException(Exception exc)\n"
				+ "			throws FileNotFoundException, NotSerializableException {\n"
				+ "		if (exc instanceof FileNotFoundException) {\n"
				+ "			throw (FileNotFoundException) exc;\n"
				+ "		}\n"
				+ "\n"
				+ "		if (exc instanceof NotSerializableException) {\n"
				+ "			throw (NotSerializableException) exc;\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		MethodDeclaration tryStatementCatchingUnionType = findUniqueMethodDeclaration(typeDeclaration,
				"tryStatementCatchingUnionType");
		MethodInvocation methodInvocationThrowingTwoExceptions = findUniqueMethodInvocation(
				tryStatementCatchingUnionType);
		assertTrue(ExceptionHandlingAnalyzer.checkMethodInvocation(tryStatementCatchingUnionType,
				methodInvocationThrowingTwoExceptions));
	}

	@Test
	void analyze_callMethodThrowingRuntimeException_shouldReturnTrue()
			throws Exception {

		String typeContent = "" +
				"	void callMethodThrowingRuntimeException() {\n"
				+ "		methodThrowingRuntimeException();\n"
				+ "	}\n"
				+ "\n"
				+ "	void methodThrowingRuntimeException() throws RuntimeException {\n"
				+ "		throw new RuntimeException();\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		MethodDeclaration tryStatementCatchingUnionType = findUniqueMethodDeclaration(typeDeclaration,
				"callMethodThrowingRuntimeException");
		MethodInvocation methodInvocation = findUniqueMethodInvocation(
				tryStatementCatchingUnionType);
		assertTrue(ExceptionHandlingAnalyzer.checkMethodInvocation(tryStatementCatchingUnionType,
				methodInvocation));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Throwable",
			"Exception",
			"NotDefinedException",
	})
	void analyze_callMethodWithNotToleratedThrowsClause_shouldReturnFalse(String notToleratedException)
			throws Exception {

		String methodDeclaration = String.format(""
				+ "	void notToleratedMethod() throws %s {\n"
				+ "		throw new %s();\n"
				+ "	}", notToleratedException, notToleratedException);
		String typeContent = "" +
				"	void callNotToleratedMethod() {\n"
				+ "		notToleratedMethod();\n"
				+ "	}\n"
				+ "\n"
				+ methodDeclaration;

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		MethodDeclaration callNotToleratedMethod = findUniqueMethodDeclaration(typeDeclaration,
				"callNotToleratedMethod");
		MethodInvocation methodInvocation = findUniqueMethodInvocation(
				callNotToleratedMethod);
		assertFalse(ExceptionHandlingAnalyzer.checkMethodInvocation(callNotToleratedMethod,
				methodInvocation));
	}

	@Test
	void analyze_newInstanceOfUnknownClass_shouldReturnFalse() throws Exception {
		String typeContent = "" +
				"	Object createUnknownClass() {\n"
				+ "		return new UnknownClass();\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, typeContent);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		ClassInstanceCreation classinstanceCreation = findUniqueClassInstanceCreation(typeDeclaration);
		assertFalse(ExceptionHandlingAnalyzer.checkClassInstanceCreation(typeDeclaration, classinstanceCreation));
	}

}
