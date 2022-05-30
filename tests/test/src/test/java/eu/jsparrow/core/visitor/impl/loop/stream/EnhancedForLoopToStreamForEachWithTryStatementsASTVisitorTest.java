package eu.jsparrow.core.visitor.impl.loop.stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;

@SuppressWarnings("nls")
class EnhancedForLoopToStreamForEachWithTryStatementsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new EnhancedForLoopToStreamForEachASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_TryWithResourcesWithBufferedReader_shouldTransform() throws Exception {
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());

		String original = "" +
				"	void loopWithBufferedReaderAsResource(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		for (FileReader fileReader : fileReaderList) {\n"
				+ "			try (BufferedReader br = new BufferedReader(fileReader)) {\n"
				+ "				\n"
				+ "			}\n"
				+ "			catch(IOException exc) {\n"
				+ "				\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	void loopWithBufferedReaderAsResource(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		fileReaderList.forEach(fileReader -> {\n"
				+ "			try (BufferedReader br = new BufferedReader(fileReader)) {\n"
				+ "				\n"
				+ "			}\n"
				+ "			catch(IOException exc) {\n"
				+ "				\n"
				+ "			}\n"
				+ "		});\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_TryWithResourcesWithBufferedReader_shouldNotTransform() throws Exception {

		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());

		String original = "" +
				"	void loopWithBufferedReaderAsResource(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		for (FileReader fileReader : fileReaderList) {\n"
				+ "			try (BufferedReader br = new BufferedReader(fileReader)) {\n"
				+ "				\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_TryWithSublcassOfBufferedReader_shouldNotTransform() throws Exception {
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
				+ "	}\n"
				+ "\n"
				+ "	void test(List<FileReader> fileReaderList) throws IOException {\n"
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
	void visit_TryWithSublcassOfBufferedReader_shouldTransform() throws Exception {
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
				+ "	}\n"
				+ "\n"
				+ "	void test(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		for (FileReader fileReader : fileReaderList) {\n"
				+ "			try (BufferedReaderSubClass br = new BufferedReaderSubClass(fileReader)) {\n"
				+ "\n"
				+ "			}\n"
				+ "			catch(IOException exc) {\n"
				+ "				\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	static class BufferedReaderSubClass extends BufferedReader {\n"
				+ "\n"
				+ "		public BufferedReaderSubClass(FileReader in) {\n"
				+ "			super(in);\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	void test(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		fileReaderList.forEach(fileReader -> {\n"
				+ "			try (BufferedReaderSubClass br = new BufferedReaderSubClass(fileReader)) {\n"
				+ "\n"
				+ "			}\n"
				+ "			catch(IOException exc) {\n"
				+ "				\n"
				+ "			}\n"
				+ "		});\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_TryWithSublcassOfSublcassOfBufferedReader_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());

		String original = "" +
				"	static class SubclassOfSubclassOfBufferedReader extends BufferedReaderSubClass {\n"
				+ "\n"
				+ "		public SubclassOfSubclassOfBufferedReader(FileReader in) {\n"
				+ "			super(in);\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	static class BufferedReaderSubClass extends BufferedReader {\n"
				+ "\n"
				+ "		public BufferedReaderSubClass(FileReader in) {\n"
				+ "			super(in);\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	void testUseBufferedReaderWithoutException(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		for (FileReader fileReader : fileReaderList) {\n"
				+ "			try (SubclassOfSubclassOfBufferedReader br = new SubclassOfSubclassOfBufferedReader(fileReader)) {\n"
				+ "\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

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
				+ "	void test(List<FileReader> fileReaderList) throws IOException {\n"
				+ "\n"
				+ "		for (FileReader fileReader : fileReaderList) {\n"
				+ "			try (BufferedReaderSubClass br = new BufferedReaderSubClass(fileReader)) {\n"
				+ "\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}
}
