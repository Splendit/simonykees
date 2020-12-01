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

	@Test
	public void visit_TWRUsingNewFileWriter_shouldTransform() throws Exception {
		fixture.addImport(java.io.BufferedWriter.class.getName());
		fixture.addImport(java.io.FileWriter.class.getName());
		fixture.addImport(java.nio.charset.Charset.class.getName());
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());

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
		fixture.addImport(java.nio.charset.Charset.class.getName());
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());

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
		fixture.addImport(java.io.BufferedWriter.class.getName());
		fixture.addImport(java.io.FileWriter.class.getName());
		fixture.addImport(java.nio.charset.Charset.class.getName());
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());

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
		fixture.addImport(java.io.BufferedWriter.class.getName());
		fixture.addImport(java.nio.charset.Charset.class.getName());
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());
		fixture.addImport(java.nio.file.Files.class.getName());
		fixture.addImport(java.nio.file.Path.class.getName());
		fixture.addImport(java.nio.file.Paths.class.getName());
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
		fixture.addImport(java.io.BufferedWriter.class.getName());
		fixture.addImport(java.io.FileWriter.class.getName());
		fixture.addImport(java.nio.charset.Charset.class.getName());
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());
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
		fixture.addImport(java.io.BufferedWriter.class.getName());
		fixture.addImport(java.io.FileWriter.class.getName());
		fixture.addImport(java.nio.charset.Charset.class.getName());
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());
		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try (FileWriter fileWriter = new FileWriter(pathString, cs);\n"
				+ "				FileWriter fileWriter2 = new FileWriter(pathString, cs);\n"
				+ "				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);\n"
				+ "				BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		String expected = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try (FileWriter fileWriter2 = new FileWriter(pathString, cs);\n"
				+ "				BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2)) {\n"
				+ "			Files.writeString(Paths.get(pathString), value, cs);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		assertChange(original, expected);
	}
}
