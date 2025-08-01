package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

class CreateTempFilesUsingJavaNIOASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		setVisitor(new CreateTempFilesUsingJavaNIOASTVisitor());
		fixture.addImport(java.io.File.class.getName());
	}

	@Test
	void visit_CreateTempFileWithoutDirectory_shouldTransform() throws Exception {

		String original = "" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\");\n" +
				"		} catch (Exception e) {\n" +
				"		}";

		String expected = "" +
				"		try {\n" +
				"			File file = Files.createTempFile(\"prefix\", \"suffix\").toFile();\n" +
				"		} catch (Exception e) {\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	void visit_CreateTempFileWithNullAsDirectory_shouldTransform() throws Exception {

		String original = "" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", null);\n" +
				"		} catch (Exception e) {\n" +
				"		}";
		String expected = "" +
				"		try {\n" +
				"			File file = Files.createTempFile(\"prefix\", \"suffix\").toFile();\n" +
				"		} catch (Exception e) {\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	void visit_PathsCannotBeImported_shouldTransform() throws Exception {
		String original = "" +
				"		class Paths {}\n" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", new File(\"/tmp/test/\"));\n" +
				"		} catch (Exception e) {\n" +
				"		}";
		String expected = "" +
				"		class Paths {}\n" +
				"		try {\n" +
				"			File file = Files.createTempFile(java.nio.file.Paths.get(\"/tmp/test/\"), \"prefix\", \"suffix\").toFile();\n"
				+
				"		} catch (Exception e) {\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	void visit_FilesCannotBeImported_shouldTransform() throws Exception {
		String original = "" +
				"		class Files {}\n" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\");\n" +
				"		} catch (Exception e) {\n" +
				"		}";
		String expected = "" +
				"		class Files {}\n" +
				"		try {\n" +
				"			File file = java.nio.file.Files.createTempFile(\"prefix\", \"suffix\").toFile();\n" +
				"		} catch (Exception e) {\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	void visit_FilesAndPathsImportedOnDemand_shouldTransform() throws Exception {
		fixture.addImport("java.nio.file", false, true);
		String original = "" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", new File(\"/tmp/test/\"));\n" +
				"		} catch (Exception e) {\n" +
				"		}";
		String expected = "" +
				"		try {\n" +
				"			File file = Files.createTempFile(Paths.get(\"/tmp/test/\"), \"prefix\", \"suffix\").toFile();\n"
				+
				"		} catch (Exception e) {\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	void visit_CreateTempFileWithNewFileAsDirectory_shouldTransform() throws Exception {

		String original = "" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", new File(\"/tmp/test/\"));\n" +
				"		} catch (Exception e) {\n" +
				"		}";
		String expected = "" +
				"		try {\n" +
				"			File file = Files.createTempFile(Paths.get(\"/tmp/test/\"), \"prefix\", \"suffix\").toFile();\n"
				+
				"		} catch (Exception e) {\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	void visit_CreateTempFileWithNotNullVariableAsDirectory_shouldTransform() throws Exception {

		String original = "" +
				"		try {\n" +
				"			File directory = new File(\"/tmp/test/\");\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", directory);\n" +
				"		} catch (Exception e) {\n" +
				"		}";

		String expected = "" +
				"		try {\n" +
				"			File directory = new File(\"/tmp/test/\");\n" +
				"			File file=Files.createTempFile(directory.toPath(),\"prefix\",\"suffix\").toFile();\n" +
				"		} catch (Exception e) {\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	void visit_DirectoryAssignedToNullAfterUsage_shouldTransform() throws Exception {
		String original = "" +
				"		try {\n" +
				"			File directory = new File(\"/tmp/test/\");\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", directory);\n" +
				"			directory = null;\n" +
				"		} catch (Exception e) {\n" +
				"		}";

		String expected = "" +
				"		try {\n" +
				"			File directory = new File(\"/tmp/test/\");\n" +
				"			File file=Files.createTempFile(directory.toPath(),\"prefix\",\"suffix\").toFile();\n" +
				"			directory = null;\n" +
				"		} catch (Exception e) {\n" +
				"		}";

		assertChange(original, expected);
	}
}
