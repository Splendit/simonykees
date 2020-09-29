package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseFilesBufferedReaderASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseFilesBufferedReaderASTVisitor());
		setJavaVersion(JavaCore.VERSION_11);
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
				"try (BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n"
				+
				"\n" +
				"} catch (IOException e) {\n" +
				"   e.printStackTrace();\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_fileReaderInitializedWithString_shouldTransform() throws Exception {
		String original = "" +
				"try (FileReader reader = new FileReader(\"path/to/file\");\n" +
				"        BufferedReader br = new BufferedReader(reader)) {\n" +
				"} catch (IOException e) {\n" +
				"   e.printStackTrace();\n" +
				"}";
		String expected = "" +
				"try (BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n"
				+
				"} catch (IOException e) {\n" +
				"   e.printStackTrace();\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_initializingWithNewFileReaderNewFile_shouldTransform() throws Exception {
		String original = "" +
				"try {\n" +
				"	BufferedReader br = new BufferedReader(new FileReader(new File(\"path/to/file\")));\n" +
				"} catch (IOException e) {}";
		String expected = "" +
				"try {\n" +
				"    BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\"), Charset.defaultCharset());\n"
				+
				"} catch (IOException e) {}";
		assertChange(original, expected);
	}

	@Test
	public void visit_initializingWithNewFileReaderNewFileMultipleArgs_shouldTransform() throws Exception {
		String original = "" +
				"try (BufferedReader br = new BufferedReader(new FileReader(new File(\"path/to/parent\", \"path/to/child\")))) {\n"
				+
				"} catch (IOException e) {}";
		String expected = "" +
				"try (BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/parent\", \"path/to/child\"), Charset.defaultCharset())) {\n"
				+
				"} catch (IOException e) {}";
		assertChange(original, expected);
	}

	@Test
	public void visit_initializingWithNewFileReaderString_shouldTransform() throws Exception {
		String original = "" +
				"try (BufferedReader br = new BufferedReader(new FileReader(\"path/to/file\"))) {\n" +
				"} catch (IOException e) {}";
		String expected = "" +
				"try (BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\"), Charset.defaultCharset())) {\n"
				+
				"} catch (IOException e) {}";
		assertChange(original, expected);
	}

	@Test
	public void visit_missingInitializer_shouldNotTransform() throws Exception {
		String original = "BufferedReader br;";
		assertNoChange(original);
	}

	@Test
	public void visit_methodInvocationInitializer_shouldNotTransform() throws Exception {
		fixture.addImport(java.nio.file.Files.class.getName());
		fixture.addImport(java.nio.file.Paths.class.getName());
		String original = "" +
				"try {\n" +
				"	BufferedReader br = Files.newBufferedReader(Paths.get(\"path/to/file\"));\n" +
				"} catch (IOException e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_multipleArguments_shouldNotTransform() throws Exception {
		String original = "" +
				"try {\n" +
				"	BufferedReader br = new BufferedReader(new FileReader(\"path/to/file\"), 100);\n" +
				"} catch (IOException e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_missingFileReader_shouldNotTransform() throws Exception {
		fixture.addImport(java.io.InputStreamReader.class.getName());
		String original = "" +
				"BufferedReader br = new BufferedReader(new InputStreamReader(null));";
		assertNoChange(original);
	}

	@Test
	public void visit_fileReaderDeclaredBeforeTWR_shouldNotTransform() throws Exception {
		String original = "" +
				"try {\n" +
				"	FileReader fileReader = new FileReader(\"path/to/file\");\n" +
				"	try(BufferedReader br = new BufferedReader(fileReader)) {\n" +
				"	} catch (IOException e) {}\n" +
				"} catch (FileNotFoundException e1) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_missingFileReaderInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"try(FileReader fileReader = null;\n" +
				"	BufferedReader br = new BufferedReader(fileReader)) {\n" +
				"} catch (IOException e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_reuseFileReaderInTryBlock_shouldNotTransform() throws Exception {
		String original = "" +
				"try(FileReader fileReader = new FileReader(new File(\"path/to/file\"));\n" +
				"		BufferedReader br = new BufferedReader(fileReader)) {\n" +
				"	System.out.println(fileReader.getEncoding());\n" +
				"} catch (IOException e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_nullFileReaderArgument_shouldNotTransform() throws Exception {
		String original = "" +
				"try (BufferedReader br = new BufferedReader(new FileReader(null))) {\n" +
				"\n" +
				"} catch (IOException e) {\n" +
				"   e.printStackTrace();\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_nullFileReaderArgumentInTWR_shouldNotTransform() throws Exception {
		String original = "" +
				"try (FileReader reader = new FileReader(null);\n" +
				"        BufferedReader br = new BufferedReader(reader)) {\n" +
				"\n" +
				"} catch (IOException e) {\n" +
				"   e.printStackTrace();\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_usingInputStreamReader_shouldNotTransform() throws Exception {
		String original = "" +
				"try (InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(\"file\")));\n" +
				"	BufferedReader br = new BufferedReader(reader)) {\n" +
				"\n" +
				"} catch (IOException e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_subTypeInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"try (BufferedReader br = new LineNumberReader(null)) {\n" +
				"\n" +
				"} catch (IOException e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_tempFileInFileReader_shouldNotTransform() throws Exception {
		String original = "" +
				"try (FileReader reader = new FileReader(File.createTempFile(\"prefix\", \"suffix\"));\n" +
				"		BufferedReader br = new BufferedReader(reader)) {\n" +
				"\n" +
				"} catch (IOException e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_anonymousSubClassOfBufferedReader_shouldNotTransform() throws Exception {
		String newAnonymousSubclassOfBufferedReader = "new BufferedReader(new FileReader(\"path\")) {}";
		String original = "" +
				"try {\n" +
				"	BufferedReader br = " + newAnonymousSubclassOfBufferedReader + ";\n" +
				"	System.out.println(br.readLine());\n" +
				"} catch (IOException e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_anonymousSubclassOfFileReader_shouldNotTransform() throws Exception {
		String newAnonymousSubclassOfFileReader = "new FileReader(new File(\"path\")){}";
		String original = "" +
				"		try {\n" +
				"			BufferedReader br = new BufferedReader(" +
				newAnonymousSubclassOfFileReader +
				");\n" +
				"			System.out.println(br.readLine());\n" +
				"		} catch (Exception e) {\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_anonymousSubclassOfFile_shouldNotTransform() throws Exception {
		String newAnonymousSubclassOfFile = "new File(\"path\") {}";
		String original = "" +
				"		try {\n" +
				"			BufferedReader br = new BufferedReader(new FileReader(" +
				newAnonymousSubclassOfFile +
				"));\n" +
				"			System.out.println(br.readLine());\n" +
				"		} catch (Exception e) {\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_NewFileReaderWithCharSet_shouldTransform() throws Exception {
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());
		String original = "" +
				"var path = \"pathToFile\";" +
				"try {\n" +
				"	BufferedReader bufferedReader = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8));\n"
				+
				"} catch (IOException e) {}";
		String expected = "" +
				"var path = \"pathToFile\";" +
				"try {\n" +
				"	BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8);\n"
				+
				"} catch (IOException e) {}";
		assertChange(original, expected);
	}

	@Test
	public void visit_FileReaderVariableWithCharSet_shouldTransform() throws Exception {
		fixture.addImport(java.nio.charset.StandardCharsets.class.getName());
		String original = "" +
				"			var path = \"pathToFile\";\n"
				+ "			try (FileReader FileReader = new FileReader(path, StandardCharsets.UTF_8);\n"
				+ "					BufferedReader bw = new BufferedReader(FileReader);) {\n"
				+ "			} catch (IOException e) {\n"
				+ "			}";
		String expected = "" +
				"			var path = \"pathToFile\";\n"
				+ "			try (BufferedReader bw = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8);) {\n"
				+ "			} catch (IOException e) {\n"
				+ "			}";
		assertChange(original, expected);
	}
}
