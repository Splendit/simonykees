package eu.jsparrow.core.visitor.impl.loop.stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;

class EnhancedForLoopToStreamForEachExceptionsASTVisitorTest extends UsesJDTUnitFixture {

	private static final String SUBCLASSES_OF_BUFFERED_READER = ""
			+ "	static class SubclassOfSubclassOfBufferedReader extends SubClassOfBufferedReader {\n"
			+ "\n"
			+ "		public SubclassOfSubclassOfBufferedReader(FileReader in) {\n"
			+ "			super(in);\n"
			+ "		}\n"
			+ "	}\n"
			+ "\n"
			+ "	static class SubClassOfBufferedReader extends BufferedReader {\n"
			+ "\n"
			+ "		public SubClassOfBufferedReader(FileReader in) {\n"
			+ "			super(in);\n"
			+ "		}\n"
			+ "	}";

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new EnhancedForLoopToStreamForEachASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"BufferedReader",
			"SubClassOfBufferedReader",
			"SubclassOfSubclassOfBufferedReader" })
	void visit_TryWithResourcesInEnhancedForLoop_shouldTransform(String resource) throws Exception {
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());

		String tryWithCatch = String.format(""
				+ "			try (%s br = new %s(fileReader)) {\n"
				+ "			}\n"
				+ "			catch (IOException exc) {\n"
				+ "			}\n",
				resource, resource);

		String original = "" +
				"	void forEachTryWithResource(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		for (FileReader fileReader : fileReaderList) {\n"
				+ tryWithCatch
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ SUBCLASSES_OF_BUFFERED_READER;

		String expected = "" +
				"	void forEachTryWithResource(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		fileReaderList.forEach(fileReader -> {\n"
				+ tryWithCatch
				+ "		});\n"
				+ "	}\n"
				+ "\n"
				+ SUBCLASSES_OF_BUFFERED_READER;

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"BufferedReader",
			"SubClassOfBufferedReader",
			"SubclassOfSubclassOfBufferedReader" })
	void visit_TryWithResourcesInEnhancedForLoop_shouldNotTransform(String resource) throws Exception {
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());

		String tryWithoutCatch = String.format(""
				+ "			try (%s br = new %s(fileReader)) {\n"
				+ "			}\n",
				resource, resource);

		String original = "" +
				"	void forEachTryWithResource(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		for (FileReader fileReader : fileReaderList) {\n"
				+ tryWithoutCatch
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ SUBCLASSES_OF_BUFFERED_READER;

		assertNoChange(original);
	}

	@Test
	void visit_CloseWithParameter_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());

		String original = "" +
				"	static class BufferedReaderSubClass extends BufferedReader {\n"
				+ "\n"
				+ "		public BufferedReaderSubClass(FileReader in) {\n"
				+ "			super(in);\n"
				+ "		}\n"
				+ "\n"
				+ "		public void close(String message) {\n"
				+ "\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	void forEachTryWithResource(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		for (FileReader fileReader : fileReaderList) {\n"
				+ "			try (BufferedReaderSubClass br = new BufferedReaderSubClass(fileReader)) {\n"
				+ "\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_CatchClauseWithUnionType_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());

		String declarations = "\n"
				+ "	void useString1(String s) throws Exception1 {\n"
				+ "		throw new Exception1();\n"
				+ "	}\n"
				+ "\n"
				+ "	void useString2(String s) throws Exception2 {\n"
				+ "		throw new Exception2();\n"
				+ "	}\n"
				+ "\n"
				+ "	static class Exception1 extends Exception {\n"
				+ "	}\n"
				+ "\n"
				+ "	static class Exception2 extends Exception {\n"
				+ "	}";

		String original = "" +
				"	void tryUseStringMethods(List<String> strings) {\n"
				+ "\n"
				+ "		for (String s : strings) {\n"
				+ "			try {\n"
				+ "				useString1(s);\n"
				+ "				useString2(s);\n"
				+ "			} catch (Exception1 | Exception2 e) {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ declarations;

		String expected = "" +
				"	void tryUseStringMethods(List<String> strings) {\n"
				+ "\n"
				+ "		strings.forEach(s -> {\n"
				+ "			try {\n"
				+ "				useString1(s);\n"
				+ "				useString2(s);\n"
				+ "			} catch (Exception1 | Exception2 e) {\n"
				+ "			}\n"
				+ "		});\n"
				+ "	}\n"
				+ declarations;

		assertChange(original, expected);

	}

	@Test
	void visit_CatchClauseException_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.List.class.getName());

		String declarations = "\n"
				+ "	void useString(String s) throws Exception1 {\n"
				+ "		throw new Exception1();\n"
				+ "	}\n"
				+ "\n"
				+ "	static class Exception1 extends Exception {\n"
				+ "	}";

		String original = "" +
				"	void tryUseStringMethods(List<String> strings) {\n"
				+ "\n"
				+ "		for (String s : strings) {\n"
				+ "			try {\n"
				+ "				useString(s);\n"
				+ "			} catch (Exception e) {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ declarations;

		String expected = "" +
				"	void tryUseStringMethods(List<String> strings) {\n"
				+ "\n"
				+ "		strings.forEach(s -> {\n"
				+ "			try {\n"
				+ "				useString(s);\n"
				+ "			} catch (Exception e) {\n"
				+ "			}\n"
				+ "		});\n"
				+ "	}\n"
				+ declarations;

		assertChange(original, expected);
	}

}
