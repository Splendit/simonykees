package eu.jsparrow.core.visitor.impl.loop.stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamTakeWhileASTVisitor;

class EnhancedForLoopToStreamTakeWhileExceptionsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new EnhancedForLoopToStreamTakeWhileASTVisitor());
		defaultFixture.addImport("java.util.List");
		defaultFixture.addImport("java.util.ArrayList");
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/**
	 * Not transformed but should be transformed.<br>
	 * This test is expected to fail as soon as the corresponding bug has been
	 * fixed.
	 */
	@Test
	void visit_HandledThrowExceptionAfterFirstIf_shouldTransform() throws Exception {
		String original = "" +
				"	void loopHandledThrowExceptionAfterFirstIf() throws Exception {\n"
				+ "\n"
				+ "		final List<String> strings = new ArrayList<>();\n"
				+ "		for (String string : strings) {\n"
				+ "			if (!checkString(string)) {\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			try {\n"
				+ "				throw new Exception();\n"
				+ "			} catch (Exception exc) {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkString(String s) {\n"
				+ "		return s != null;\n"
				+ "	}";

		assertNoChange(original);
	}

	/**
	 * Transformed to invalid code.<br>
	 * This test is expected to fail as soon as the corresponding bug has been
	 * fixed.
	 */
	@Test
	void visit_IfConditionWithException_shouldNotTransform() throws Exception {
		String original = "" +
				"	void loppIfConditionWithException() throws Exception {\n"
				+ "\n"
				+ "		final List<String> strings = new ArrayList<>();\n"
				+ "		for (String string : strings) {\n"
				+ "			if (!checkStringWithException(string)) {\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			useString(string);\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkStringWithException(String s) throws Exception {\n"
				+ "		throw new Exception();\n"
				+ "	}\n"
				+ "\n"
				+ "	void useString(String s) {\n"
				+ "\n"
				+ "	}";

		String expected = "" +
				"	void loppIfConditionWithException() throws Exception {\n"
				+ "\n"
				+ "		final List<String> strings = new ArrayList<>();\n"
				+ "		strings.stream().takeWhile(string -> checkStringWithException(string)).forEach(string -> {\n"
				+ "			useString(string);\n"
				+ "		});\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkStringWithException(String s) throws Exception {\n"
				+ "		throw new Exception();\n"
				+ "	}\n"
				+ "\n"
				+ "	void useString(String s) {\n"
				+ "\n"
				+ "	}";

		assertChange(original, expected);
	}

	/**
	 * Transformed to invalid code.<br>
	 * This test is expected to fail as soon as the corresponding bug has been
	 * fixed.
	 */
	@Test
	void visit_StatementWithExceptionAfterFirstIf_shouldNotTransform() throws Exception {
		String original = "" +
				"	void loopStatementWithExceptionAfterFirstIf() throws Exception {\n"
				+ "\n"
				+ "		final List<String> strings = new ArrayList<>();\n"
				+ "		for (String string : strings) {\n"
				+ "			if (!checkString(string)) {\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			useStringWithException(string);\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkString(String s) {\n"
				+ "		return true;\n"
				+ "	}\n"
				+ "\n"
				+ "	void useStringWithException(String s) throws Exception {\n"
				+ "		throw new Exception();\n"
				+ "	}";
		String expected = "" +
				"	void loopStatementWithExceptionAfterFirstIf() throws Exception {\n"
				+ "\n"
				+ "		final List<String> strings = new ArrayList<>();\n"
				+ "		strings.stream().takeWhile(string -> checkString(string)).forEach(string -> {\n"
				+ "			useStringWithException(string);\n"
				+ "		});\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkString(String s) {\n"
				+ "		return true;\n"
				+ "	}\n"
				+ "\n"
				+ "	void useStringWithException(String s) throws Exception {\n"
				+ "		throw new Exception();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_ThrowExceptionAfterFirstIf_shouldNotTransform() throws Exception {
		String original = "" +
				"	void loopStatementThrowExceptionAfterFirstIf() throws Exception {\n"
				+ "\n"
				+ "		final List<String> strings = new ArrayList<>();\n"
				+ "		for (String string : strings) {\n"
				+ "			if (!checkString(string)) {\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			useString(string);\n"
				+ "			throw new Exception();\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkString(String s) {\n"
				+ "		return true;\n"
				+ "	}\n"
				+ "	\n"
				+ "	void useString(String s) {\n"
				+ "	}";

		assertNoChange(original);
	}

	/**
	 * Not Transformed but should be transformed.<br>
	 * This test is expected to fail as soon as the corresponding bug has been
	 * fixed.
	 */
	@Test
	void visit_ThrowRuntimeExceptionAfterFirstIf_shouldTransform() throws Exception {
		String original = "" +
				"	void loopStatementThrowRuntimeExceptionAfterFirstIf() {\n"
				+ "\n"
				+ "		final List<String> strings = new ArrayList<>();\n"
				+ "		for (String string : strings) {\n"
				+ "			if (!checkString(string)) {\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			useString(string);\n"
				+ "			throw new RuntimeException();\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkString(String s) {\n"
				+ "		return true;\n"
				+ "	}\n"
				+ "\n"
				+ "	void useString(String s) {\n"
				+ "\n"
				+ "	}";

		assertNoChange(original);
	}

	/**
	 * Transformed to invalid code.<br>
	 * This test is expected to fail as soon as the corresponding bug has been
	 * fixed.
	 */
	@Test
	void visit_UnhandledCloseExceptionAfterFirstIf_shouldNotTransform() throws Exception {
		defaultFixture.addImport("java.io.BufferedReader");
		defaultFixture.addImport("java.io.FileReader");
		defaultFixture.addImport("java.io.IOException");

		String original = "" +
				"	void loopStatementThrowRuntimeExceptionAfterFirstIf(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		for (FileReader fileReader : fileReaderList) {\n"
				+ "			if (!checkFileReader(fileReader)) {\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			try (BufferedReader br = new BufferedReader(fileReader)) {\n"
				+ "\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkFileReader(FileReader fileReader) {\n"
				+ "		return true;\n"
				+ "	}";
		
		String expected = "" +
				"	void loopStatementThrowRuntimeExceptionAfterFirstIf(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		fileReaderList.stream().takeWhile(fileReader -> checkFileReader(fileReader)).forEach(fileReader -> {\n"
				+ "			try (BufferedReader br = new BufferedReader(fileReader)) {\n"
				+ "\n"
				+ "			}\n"
				+ "		});\n"
				+ "	}\n"
				+ "\n"
				+ "	boolean checkFileReader(FileReader fileReader) {\n"
				+ "		return true;\n"
				+ "	}";

		assertChange(original, expected);
	}

}
