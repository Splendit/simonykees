package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class CreateTempFilesUsingJavaNioASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new CreateTempFilesUsingJavaNioASTVisitor());
		fixture.addImport(java.io.File.class.getName());
	}

	@Test
	public void visit_CreateTempFileWithoutDirectory_shouldTransform() throws Exception {

		String original = "" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\");\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		String expected = "" +
				"		try {\n" +
				"			File file = Files.createTempFile(\"prefix\", \"suffix\").toFile();\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	public void visit_CreateTempFileWithNullAsDirectory_shouldTransform() throws Exception {

		String original = "" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", null);\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		String expected = "" +
				"		try {\n" +
				"			File file = Files.createTempFile(\"prefix\", \"suffix\").toFile();\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	public void visit_PathsCannotBeImported_shouldTransform() throws Exception {
		String original = "" +
				"		class Paths {}\n" + 
				"		try {\n" + 
				"			File file = File.createTempFile(\"prefix\", \"suffix\", new File(\"/tmp/test/\"));\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		String expected = "" +
				"		class Paths {}\n" + 
				"		try {\n" + 
				"			File file = Files.createTempFile(java.nio.file.Paths.get(\"/tmp/test/\"), \"prefix\", \"suffix\").toFile();\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_FilesCannotBeImported_shouldTransform() throws Exception {
		String original = "" +
				"		class Files {}\n" + 
				"		try {\n" + 
				"			File file = File.createTempFile(\"prefix\", \"suffix\");\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		String expected = "" +
				"		class Files {}\n" + 
				"		try {\n" + 
				"			File file = java.nio.file.Files.createTempFile(\"prefix\", \"suffix\").toFile();\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_FilesAndPathsImportedOnDemand_shouldTransform() throws Exception {
		fixture.addImport(java.nio.file.Files.class.getPackage().getName(), false, true);		
		String original = "" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", new File(\"/tmp/test/\"));\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		String expected = "" +
				"		try {\n" +
				"			File file = Files.createTempFile(Paths.get(\"/tmp/test/\"), \"prefix\", \"suffix\").toFile();\n"
				+
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	public void visit_CreateTempFileWithNewFileAsDirectory_shouldTransform() throws Exception {

		String original = "" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", new File(\"/tmp/test/\"));\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		String expected = "" +
				"		try {\n" +
				"			File file = Files.createTempFile(Paths.get(\"/tmp/test/\"), \"prefix\", \"suffix\").toFile();\n"
				+
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	public void visit_CreateTempFileWithNewFileAsDirectory_shouldNotTransform() throws Exception {

		String original = "" +
				"		try {\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", new File(new File(\"test/\"), \"/tmp/\"));\n"
				+
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_CreateTempFileWithVariableAsDirectory_shouldNotTransform() throws Exception {

		String original = "" +
				"		try {\n" +
				"			File directory = new File(\"/tmp/test/\");\n" +
				"			File file = File.createTempFile(\"prefix\", \"suffix\", directory);\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		assertNoChange(original);
	}

}
