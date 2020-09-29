package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseFilesBufferedWriterASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseFilesBufferedWriterASTVisitor());
		setJavaVersion(JavaCore.VERSION_11);
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

	@Test
	public void visit_MissingInitializer_shouldNotTransform() throws Exception {
		String original = "BufferedWriter bw;";
		assertNoChange(original);
	}

	@Test
	public void visit_MethodInvocationAsInitializer_shouldNotTransform() throws Exception {
		fixture.addImport(java.nio.file.Files.class.getName());
		fixture.addImport(java.nio.file.Paths.class.getName());
		String original = "" +
				"			try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(\"path/to/file\"));) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_BufferedWriterWithIntAs2ndArgument_shouldNotTransform() throws Exception {
		String original = "" +
				"			try (BufferedWriter bw = new BufferedWriter(new FileWriter(\"path/to/file\"), 100);) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_BufferedWriterWithOutputStreamWriterAsArgument_shouldNotTransform() throws Exception {
		fixture.addImport(java.io.OutputStreamWriter.class.getName());
		String original = "" +
				"			try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(null));) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_FileWriterDeclaredBeforeTWR_shouldNotTransform() throws Exception {
		String original = "" +
				"			try {\n" +
				"				FileWriter fileWriter = new FileWriter(\"path/to/file\");\n" +
				"				try (BufferedWriter bw = new BufferedWriter(fileWriter)) {\n" +
				"				} catch (IOException e) {\n" +
				"				}\n" +
				"			} catch (IOException e1) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_FileWriterInitializedWithNull_shouldNotTransform() throws Exception {
		String original = "" +
				"			try (FileWriter fileWriter = null; BufferedWriter bw = new BufferedWriter(fileWriter)) {\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_ReuseFileWriterInTryBlock_shouldNotTransform() throws Exception {
		String original = "" +
				"			try (FileWriter fileWriter = new FileWriter(new File(\"path/to/file\"));\n" +
				"					BufferedWriter bw = new BufferedWriter(fileWriter)) {\n" +
				"				System.out.println(fileWriter.getEncoding());\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_NullAsFileWriterArgument_shouldNotTransform() throws Exception {
		String original = "" +
				"			try (BufferedWriter bw = new BufferedWriter(new FileWriter((File) null))) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_NullAsFileWriterArgumentInTWR_shouldNotTransform() throws Exception {
		String original = "" +
				"			try (FileWriter writer = new FileWriter((File) null); BufferedWriter bw = new BufferedWriter(writer)) {\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_UsingOutputStreamWriter_shouldNotTransform() throws Exception {
		fixture.addImport(java.io.OutputStreamWriter.class.getName());
		fixture.addImport(java.io.FileOutputStream.class.getName());
		String original = "" +
				"			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(\"file\")));\n"
				+
				"					BufferedWriter bw = new BufferedWriter(writer)) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_InitializingBufferedWriterWithSubclass_shouldNotTransform() throws Exception {
		String original = "" +
				"			class BufferedWriterSubclass extends BufferedWriter {\n" +
				"				public BufferedWriterSubclass(Writer out) {\n" +
				"					super(out);\n" +
				"				}\n" +
				"			}\n" +
				"			try (BufferedWriter bw = new BufferedWriterSubclass(new FileWriter(\"path/to/file\"))) {\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_FileWriterWithTempFileAsArgument_shouldNotTransform() throws Exception {
		String original = "" +
				"			try (FileWriter writer = new FileWriter(File.createTempFile(\"prefix\", \"suffix\"));\n" +
				"					BufferedWriter bw = new BufferedWriter(writer)) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_AnonymousSubclassOfBufferedWriter_shouldNotTransform() throws Exception {
		String newAnonymousSubclassOfBufferedWriter = "new BufferedWriter(new FileWriter(\"path\")) {}";
		String original = "" +
				"			try {\n" +
				"				BufferedWriter bw = " + newAnonymousSubclassOfBufferedWriter + ";\n" +
				"			} catch (IOException e) {\n" +
				"			}\n" +
				"";
		assertNoChange(original);
	}

	@Test
	public void visit_AnonymousSubclassOfFileWriter_shouldNotTransform() throws Exception {
		String newAnonymousSubclassOfFileWriter = "new FileWriter(new File(\"path\")) {}";
		String original = "" +
				"			try {\n" +
				"				BufferedWriter bw = new BufferedWriter(" + newAnonymousSubclassOfFileWriter + ");\n" +
				"			} catch (Exception e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_AnonymousSubclassOfFile_shouldNotTransform() throws Exception {
		String newAnonymousSubclassOfFile = "new File(\"path\") {}";
		String original = "" +
				"			try {\n" +
				"				BufferedWriter br = new BufferedWriter(new FileWriter(" + newAnonymousSubclassOfFile
				+ "));\n" +
				"			} catch (Exception e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_NewFileWriterWithCharSet_shouldTransform() throws Exception {
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());
		String original = "" +
				"			var path = \"pathToFile\";\n" +
				"			try {\n" +
				"				BufferedWriter bw = new BufferedWriter(new FileWriter(path, StandardCharsets.UTF_8));\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		String expected = "" +
				"			var path = \"pathToFile\";\n" +
				"			try {\n" +
				"				BufferedWriter bw = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8);\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		assertChange(original, expected);
	}

	@Test
	public void visit_FileWriterVariableWithCharSet_shouldTransform() throws Exception {
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());
		String original = "" +
				"			var path = \"pathToFile\";\n" +
				"			try (FileWriter fileWriter = new FileWriter(path, StandardCharsets.UTF_8);\n" +
				"					BufferedWriter bw = new BufferedWriter(fileWriter);) {\n" +
				"			} catch (IOException e) {\n" +
				"			}";
		String expected = "" +
				"			var path = \"pathToFile\";\n" +
				"			try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8);) {\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		assertChange(original, expected);
	}

	@Test
	public void visit_NewFileWriterWithCharSetAndBoolean_shouldNotTransform() throws Exception {
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());
		String original = "" +
				"			var path = \"pathToFile\";\n" +
				"			try {\n" +
				"				BufferedWriter bw = new BufferedWriter(new FileWriter(path, StandardCharsets.UTF_8, true));\n"
				+
				"			} catch (IOException e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_NewFileWriterWithBoolean_shouldNotTransform() throws Exception {
		String original = "" +
				"			var path = \"pathToFile\";\n"
				+ "			try {\n"
				+ "				BufferedWriter bw = new BufferedWriter(new FileWriter(path, true));\n"
				+ "			} catch (IOException e) {\n"
				+ "			}";
		assertNoChange(original);
	}
}
