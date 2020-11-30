package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseFilesWriteStringASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseFilesWriteStringASTVisitor());
		setJavaVersion(JavaCore.VERSION_11);
	}

	@Disabled
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
}
