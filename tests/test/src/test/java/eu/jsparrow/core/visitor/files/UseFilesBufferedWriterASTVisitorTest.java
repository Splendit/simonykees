package eu.jsparrow.core.visitor.files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseFilesBufferedWriterASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseFilesBufferedWriterASTVisitor());
		fixture.addImport(java.io.File.class.getName());
		fixture.addImport(java.io.FileWriter.class.getName());
		fixture.addImport(java.io.BufferedWriter.class.getName());
		fixture.addImport(java.io.IOException.class.getName());
	}

	@Test
	public void visit_baseCase_shouldTransform() throws Exception {
		String original = "" +
				"			try (FileWriter writer = new FileWriter(new File(\"path/to/file\"));\n" +
				"					BufferedWriter bw = new BufferedWriter(writer)) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		String expected = "" +
				"			try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		assertChange(original, expected);
	}

	@Test
	public void visit_fileWriterInitializedWithString_shouldTransform() throws Exception {
		String original = "" +
				"			try (FileWriter writer = new FileWriter(\"path/to/file\");\n" +
				"					BufferedWriter bw = new BufferedWriter(writer)) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		String expected = "" +
				"			try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		assertChange(original, expected);
	}

	@Test
	public void visit_initializingWithNewFileWriterNewFile_shouldTransform() throws Exception {
		String original = "" +
				"			try {\n" +
				"				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(\"path/to/file\")));\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		String expected = "" +
				"			try {\n" +
				"				BufferedWriter bw = Files.newBufferedWriter(Paths.get(\"path/to/file\"), Charset.defaultCharset());\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		assertChange(original, expected);
	}

	@Test
	public void visit_initializingWithNewFileWriterNewFileMultipleArgs_shouldTransform() throws Exception {
		String original = "" +
				"			try (BufferedWriter bw = new BufferedWriter(\n" +
				"					new FileWriter(new File(\"path/to/parent\", \"path/to/child\")))) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		String expected = "" +
				"			try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(\"path/to/parent\", \"path/to/child\"),\n"
				+
				"					Charset.defaultCharset())) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		assertChange(original, expected);
	}

	@Test
	public void visit_initializingWithNewFileWriterString_shouldTransform() throws Exception {
		String original = "" +
				"			try (BufferedWriter bw = new BufferedWriter(new FileWriter(\"path/to/file\"))) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		String expected = "" +
				"			try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		assertChange(original, expected);
	}
}