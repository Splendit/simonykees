package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseFilesWriteStringASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseFilesWriteStringASTVisitor());
		setJavaVersion(JavaCore.VERSION_11);
	}

	private void addImports(Class<?>... classes) throws Exception {
		for (Class<?> c : classes) {
			fixture.addImport(c.getName());
		}
	}

	@Test
	public void visit_TWRUsingNewFileWriter_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try {\n" +
				"	Files.writeString(Paths.get(pathString), value, cs);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_TWRUsingQualifiedClassNames_shouldTransform() throws Exception {
		addImports(java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (java.io.BufferedWriter bufferedWriter = new java.io.BufferedWriter(\n" +
				"		new java.io.FileWriter(pathString, cs))) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try {\n" +
				"	Files.writeString(Paths.get(pathString), value, cs);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_TWRUsingFileWriterVariable_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (FileWriter fileWriter = new FileWriter(pathString, cs);\n" +
				"		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try {\n" +
				"	Files.writeString(Paths.get(pathString), value, cs);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_TWRUsingFilesNewBufferedWriterAndPathVariable_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class,
				java.nio.file.Files.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class);

		String original = "" +
				"String value = \"Hello World!\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"Path path = Paths.get(\"/home/test/testpath\");\n" +
				"try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, cs)) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"Path path = Paths.get(\"/home/test/testpath\");\n" +
				"try {\n" +
				"	Files.writeString(path, value, cs);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_TWRUsingNewFileWriterNotAllResourcesRemoved_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs));\n" +
				"		BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	Files.writeString(Paths.get(pathString), value, cs);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_TWRUsingFileWriterVariableNotAllResourcesRemoved_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (FileWriter fileWriter = new FileWriter(pathString, cs);\n" +
				"		FileWriter fileWriter2 = new FileWriter(pathString, cs);\n" +
				"		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);\n" +
				"		BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2)) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (FileWriter fileWriter2 = new FileWriter(pathString, cs);\n" +
				"		BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2)) {\n" +
				"	Files.writeString(Paths.get(pathString), value, cs);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_TWRUsingNewFileWriterWithoutCharset_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class);

		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString))) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"try {\n" +
				"	Files.writeString(Paths.get(pathString), value, Charset.defaultCharset());\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_TWRUsingFileWriterVariableWithoutCharset_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class);

		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"try (FileWriter fileWriter = new FileWriter(pathString);\n" +
				"		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"try {\n" +
				"	Files.writeString(Paths.get(pathString), value, Charset.defaultCharset());\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_TWRUsingFilesNewBufferedWriterOnlyWithPath_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.file.Files.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class);

		String original = "" +
				"String value = \"Hello World!\";\n" +
				"Path path = Paths.get(\"/home/test/testpath\");\n" +
				"try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"Path path = Paths.get(\"/home/test/testpath\");\n" +
				"try {\n" +
				"	Files.writeString(path, value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_TWRUsingFilesNewBufferedWriterWithCharsetAndOpenOptions_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class,
				java.nio.file.Files.class,
				java.nio.file.OpenOption.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class,
				java.nio.file.StandardOpenOption.class);

		String original = "" +
				"		OpenOption openOption1 = StandardOpenOption.CREATE;\n"
				+ "		OpenOption openOption2 = StandardOpenOption.APPEND;\n"
				+ "		String value = \"Hello World!\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, cs, openOption1, openOption2)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		String expected = "" +
				"		OpenOption openOption1 = StandardOpenOption.CREATE;\n"
				+ "		OpenOption openOption2 = StandardOpenOption.APPEND;\n"
				+ "		String value = \"Hello World!\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try {\n"
				+ "			Files.writeString(path, value, cs, openOption1, openOption2);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		assertChange(original, expected);
	}

	@Test
	public void visit_TWRUsingFilesNewBufferedWriterWithoutCharsetWithOpenOptions_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.file.Files.class,
				java.nio.file.OpenOption.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class,
				java.nio.file.StandardOpenOption.class);

		String original = "" +
				"		OpenOption openOption1 = StandardOpenOption.CREATE;\n"
				+ "		OpenOption openOption2 = StandardOpenOption.APPEND;\n"
				+ "		String value = \"Hello World!\";\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, openOption1, openOption2)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		String expected = "" +
				"		OpenOption openOption1 = StandardOpenOption.CREATE;\n"
				+ "		OpenOption openOption2 = StandardOpenOption.APPEND;\n"
				+ "		String value = \"Hello World!\";\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try {\n"
				+ "			Files.writeString(path, value, openOption1, openOption2);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		assertChange(original, expected);
	}

	@Test
	public void visit_DeclareWriterAsResource_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.io.Writer.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"			String value = \"Hello World!\";\n"
				+ "			String pathString = \"/home/test/testpath\";\n"
				+ "			Charset cs = StandardCharsets.UTF_8;\n"
				+ "			try (Writer writer = new BufferedWriter(new FileWriter(pathString, cs))) {\n"
				+ "				writer.write(value);\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}\n"
				+ "";
		String expected = "" +
				"			String value = \"Hello World!\";\n"
				+ "			String pathString = \"/home/test/testpath\";\n"
				+ "			Charset cs = StandardCharsets.UTF_8;\n"
				+ "			try {\n"
				+ "				Files.writeString(Paths.get(pathString), value, cs);\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}";
		assertChange(original, expected);
	}
}
