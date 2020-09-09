package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class CreateTempFilesUsingJavaNIONegativeASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new CreateTempFilesUsingJavaNIOASTVisitor());
		defaultFixture.addImport(java.io.File.class.getName());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_DirectoryConstructorWithTwoArguments_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", new File(new File(\"test/\"), \"/tmp/\"));\n"
				+
				"		} catch (Exception e) {\n" +
				"		}" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_DirectoryIsFieldByThisAccess_shouldNotTransform() throws Exception {

		String original = "" +
				"	File directory = new File(\"/tmp/test/\");\n" +
				"	void test() {\n" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", this.directory);\n" +
				"		} catch (Exception e) {\n" +
				"		}" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_DirectoryIsFieldBySimpleName_shouldNotTransform() throws Exception {

		String original = "" +
				"	File directory = new File(\"/tmp/test/\");\n" +
				"	void test() {\n" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", directory);\n" +
				"		} catch (Exception e) {\n" +
				"		}" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_DirectoryIsParameter_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test(File directory) {\n" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", directory);\n" +
				"		} catch (Exception e) {\n" +
				"		}" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_DirectoryVariableDeclaredWithoutInitializer_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		try {\n" +
				"			File directory;\n" +
				"			directory = new File(\"/tmp/test/\");\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", directory);\n" +
				"		} catch (Exception e) {\n" +
				"		}" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_DirectoryVariableInitializedWithParameter_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test(File testDirectory) {\n" +
				"		try {\n" +
				"			File directory = testDirectory;\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", directory);\n" +
				"		} catch (Exception e) {\n" +
				"		}" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_DirectoryVariableReAssignedBeforeUsage_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		try {\n" +
				"			File directory = new File(\"/tmp/test/\");\n" +
				"			directory = new File(\"/tmp/test1/\");\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", directory);\n" +
				"		} catch (Exception e) {\n" +
				"		}" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_TempFileAlreadyCreatedWithFiles_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		try {\n" +
				"			File file = Files.createTempFile(\"prefix\", \"suffix\").toFile();\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}";
		
		defaultFixture.addImport(java.nio.file.Files.class.getName());
		assertNoChange(original);
	}

}
