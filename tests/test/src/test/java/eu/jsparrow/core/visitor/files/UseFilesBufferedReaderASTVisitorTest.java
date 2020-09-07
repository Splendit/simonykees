package eu.jsparrow.core.visitor.files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseFilesBufferedReaderASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseFilesBufferedReaderASTVisitor());
		fixture.addImport(java.io.File.class.getName());
		fixture.addImport(java.io.FileReader.class.getName());
		fixture.addImport(java.io.BufferedReader.class.getName());
		fixture.addImport(java.io.IOException.class.getName());
	}
	
	@Test 
	public void visit_baseCase_shouldTransform() throws Exception {
		String original = "" +
				"try (FileReader reader = new FileReader(new File(\"path/to/file\"));\n" + 
				"        BufferedReader br = new BufferedReader(reader)) {\n" + 
				"\n" + 
				"} catch (IOException e) {\n" + 
				"   e.printStackTrace();\n" + 
				"}";
		String expected = "" +
				"try (BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n" + 
				"\n" + 
				"} catch (IOException e) {\n" + 
				"   e.printStackTrace();\n" + 
				"}";
		assertChange(original, expected);
	}
	
	@Test 
	public void visit_initializingWithNewFileReaderNewFile_shouldTransform() throws Exception {
		String original = "" +
				"try (BufferedReader br = new BufferedReader(new FileReader(new File(\"path/to/file\")))) {\n" + 
				"} catch (IOException e) {}";
		String expected = "" +
				"try (BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n" + 
				"} catch (IOException e) {}";
		assertChange(original, expected);
	}
	
	@Test 
	public void visit_initializingWithNewFileReaderNewFileMultipleArgs_shouldTransform() throws Exception {
		String original = "" +
				"try (BufferedReader br = new BufferedReader(new FileReader(new File(\"path/to/parent\", \"path/to/child\")))) {\n" + 
				"} catch (IOException e) {}";
		String expected = "" +
				"try (BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\", \"path/to/child\"), Charset.defaultCharset())) {\n" + 
				"} catch (IOException e) {}";
		assertChange(original, expected);
	}

	@Test 
	public void visit_initializingWithNewFileReaderString_shouldTransform() throws Exception {
		String original = "" +
				"try (BufferedReader br = new BufferedReader(new FileReader(\"path/to/file\"))) {\n" + 
				"} catch (IOException e) {}";
		String expected = "" +
				"try (BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n" + 
				"} catch (IOException e) {}";
		assertChange(original, expected);
	}

}
