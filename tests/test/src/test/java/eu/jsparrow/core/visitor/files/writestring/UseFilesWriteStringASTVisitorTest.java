package eu.jsparrow.core.visitor.files.writestring;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

class UseFilesWriteStringASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		setVisitor(new UseFilesWriteStringASTVisitor());
		setJavaVersion(JavaCore.VERSION_11);
	}

	@Test
	void visit_TWRUsingNewFileWriter_shouldTransform() throws Exception {
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
	void visit_TWRUsingQualifiedClassNames_shouldTransform() throws Exception {
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
	void visit_TWRUsingFileWriterVariable_shouldTransform() throws Exception {
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
	void visit_TWRUsingFilesNewBufferedWriterAndPathVariable_shouldTransform() throws Exception {
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
	void visit_TWRUsingNewFileWriterNotAllResourcesRemoved_shouldTransform() throws Exception {
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
	void visit_TWRUsingFileWriterVariableNotAllResourcesRemoved_shouldTransform() throws Exception {
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
	void visit_TWRUsingNewFileWriterWithoutCharset_shouldTransform() throws Exception {
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
	void visit_TWRUsingFileWriterVariableWithoutCharset_shouldTransform() throws Exception {
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
	void visit_TWRUsingFilesNewBufferedWriterOnlyWithPath_shouldTransform() throws Exception {
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
	void visit_TWRUsingFilesNewBufferedWriterWithStandardCharsetUTF8_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.charset.StandardCharsets.class,
				java.nio.file.Files.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class);

		String original = "" +
				"		String value = \"Hello World!\";\n" +
				"		Path path = Paths.get(\"/home/test/testpath\");\n" +
				"		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {\n"
				+
				"			bufferedWriter.write(value);\n" +
				"		} catch (Exception exception) {\n" +
				"		}";
		String expected = "" +
				"		String value = \"Hello World!\";\n" +
				"		Path path = Paths.get(\"/home/test/testpath\");\n" +
				"		try {\n" +
				"			Files.writeString(path, value, StandardCharsets.UTF_8);\n" +
				"		} catch (Exception exception) {\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	void visit_TWRUsingFilesNewBufferedWriterWithCharsetAndOpenOptions_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class,
				java.nio.file.Files.class,
				java.nio.file.OpenOption.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class,
				java.nio.file.StandardOpenOption.class);

		String original = "" +
				"		OpenOption openOption1 = StandardOpenOption.CREATE;\n" +
				"		OpenOption openOption2 = StandardOpenOption.APPEND;\n" +
				"		String value = \"Hello World!\";\n" +
				"		Charset cs = StandardCharsets.UTF_8;\n" +
				"		Path path = Paths.get(\"/home/test/testpath\");\n" +
				"		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, cs, openOption1, openOption2)) {\n"
				+
				"			bufferedWriter.write(value);\n" +
				"		} catch (Exception exception) {\n" +
				"		}";
		String expected = "" +
				"		OpenOption openOption1 = StandardOpenOption.CREATE;\n" +
				"		OpenOption openOption2 = StandardOpenOption.APPEND;\n" +
				"		String value = \"Hello World!\";\n" +
				"		Charset cs = StandardCharsets.UTF_8;\n" +
				"		Path path = Paths.get(\"/home/test/testpath\");\n" +
				"		try {\n" +
				"			Files.writeString(path, value, cs, openOption1, openOption2);\n" +
				"		} catch (Exception exception) {\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	void visit_TWRUsingFilesNewBufferedWriterWithoutCharsetWithOpenOptions_shouldTransform() throws Exception {
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
	void visit_TWRUsingFilesNewBufferedWriterWithOpenOptionCreate_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.file.Files.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class,
				java.nio.file.StandardOpenOption.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		String expected = ""
				+ "		String value = \"Hello World!\";\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try {\n"
				+ "			Files.writeString(path, value, StandardOpenOption.CREATE);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_TWRUsingFilesNewBufferedWriterWithArrayOfOpenOptions_shouldTransform()
			throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class,
				java.nio.file.Files.class,
				java.nio.file.OpenOption.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class,
				java.nio.file.StandardOpenOption.class);

		String original = "" +
				"		OpenOption[] openOptions = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND };\n"
				+ "		String value = \"Hello World!\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, cs, openOptions)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		String expected = ""
				+ "		OpenOption[] openOptions = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND };\n"
				+ "		String value = \"Hello World!\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try {\n"
				+ "			Files.writeString(path, value, cs, openOptions);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_TWRUsingFilesNewBufferedWriterRemovingNotAllResources_shouldTransform()
			throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.file.Files.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path);\n"
				+ "				BufferedWriter bufferedWriter2 = Files.newBufferedWriter(path)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		String expected = "" +
				"		String value = \"Hello World!\";\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try (BufferedWriter bufferedWriter2 = Files.newBufferedWriter(path)) {\n"
				+ "			Files.writeString(path, value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_DeclareWriterAsResource_shouldTransform() throws Exception {
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

	@Test
	void visit_FileWriterResourceFromNewFileWithOneStringArgument_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.File.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try (FileWriter fileWriter = new FileWriter(new File(pathString), cs);\n"
				+ "				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		String expected = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try {\n"
				+ "			Files.writeString(Paths.get(pathString), value, cs);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_FileWriterResourceFromNewFileWithTwoStringArguments_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.File.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathStringParent = \"/home/test/\";\n"
				+ "		String pathStringChild = \"testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try (FileWriter fileWriter = new FileWriter(new File(pathStringParent, pathStringChild), cs);\n"
				+ "				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		String expected = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathStringParent = \"/home/test/\";\n"
				+ "		String pathStringChild = \"testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try {\n"
				+ "			Files.writeString(Paths.get(pathStringParent, pathStringChild), value, cs);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_newFileWriterFromNewFileWithOneStringArgument_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.File.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(pathString), cs))) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		String expected = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try {\n"
				+ "			Files.writeString(Paths.get(pathString), value, cs);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_AdditionalNonWriterWriteMethodInvocation_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.io.IOException.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		class LocalClass {\n"
				+ "			void write(String s) throws IOException {\n"
				+ "			}\n"
				+ "		}\n"
				+ "		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString))) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "			new LocalClass().write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		String expected = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		class LocalClass {\n"
				+ "			void write(String s) throws IOException {\n"
				+ "			}\n"
				+ "		}\n"
				+ "		try {\n"
				+ "			Files.writeString(Paths.get(pathString), value, Charset.defaultCharset());\n"
				+ "			new LocalClass().write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		assertChange(original, expected);
	}

	@Test
	void visit_TwoBufferedWritersWriting_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.file.Files.class,
				java.nio.file.Paths.class);

		String original = "" +
				"			String value = \"Hello World!\";\n"
				+ "			try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(\"/home/test/testpath\"));\n"
				+ "					BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(\"/home/test/testpath-2\"))) {\n"
				+ "				bufferedWriter.write(value);\n"
				+ "				bufferedWriter2.write(value);\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}";

		String expected = "" +
				"			String value = \"Hello World!\";\n"
				+ "			try {\n"
				+ "				Files.writeString(Paths.get(\"/home/test/testpath\"), value);\n"
				+ "				Files.writeString(Paths.get(\"/home/test/testpath-2\"), value, Charset.defaultCharset());\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}";

		assertChange(original, expected);
	}

	/**
	 * Bug fix SIM-1918
	 */
	@Test
	void visit_TWRUsingBufferedWriterConstructorNoCatch_shouldTransform() throws Exception {
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
				"}";

		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"Files.writeString(Paths.get(pathString), value, cs);";
		assertChange(original, expected);
	}

	/**
	 * Bug fix SIM-1918
	 */
	@Test
	void visit_TWRUsingTwoBufferedWritersNoCatch_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.file.Files.class,
				java.nio.file.Paths.class);

		String original = "" +
				"			String value = \"Hello World!\";\n"
				+ "			try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(\"/home/test/testpath\"));\n"
				+ "					BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(\"/home/test/testpath-2\"))) {\n"
				+ "				bufferedWriter.write(value);\n"
				+ "				bufferedWriter2.write(value);\n"
				+ "			}";

		String expected = "" +
				"			String value = \"Hello World!\";\n"
				+ "			Files.writeString(Paths.get(\"/home/test/testpath\"), value);\n"
				+ "			Files.writeString(Paths.get(\"/home/test/testpath-2\"), value, Charset.defaultCharset());\n";

		assertChange(original, expected);
	}

	/**
	 * Bug fix SIM-1918
	 */
	@Test
	void visit_TWRUsingFilesNewBufferedWriterNoCatch_shouldTransform() throws Exception {
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
				"}";
		String expected = "" +
				"String value = \"Hello World!\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"Path path = Paths.get(\"/home/test/testpath\");\n" +
				"Files.writeString(path, value, cs);";

		assertChange(original, expected);
	}

	/**
	 * Bug fix SIM-1918
	 */
	@Test
	void visit_NoCatchNoTryStatementParentBlock_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class,
				java.nio.file.Files.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class);

		String original = "" +
				"	String value = \"Hello World!\";\n" +
				"	Charset cs = StandardCharsets.UTF_8;\n" +
				"	Path path = Paths.get(\"/home/test/testpath\");\n" +
				"	if(true) try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, cs)) {\n" +
				"		bufferedWriter.write(value);\n" +
				"	}";
		String expected = "" +
				"	String value = \"Hello World!\";\n" +
				"	Charset cs = StandardCharsets.UTF_8;\n" +
				"	Path path = Paths.get(\"/home/test/testpath\");\n" +
				"	if(true) Files.writeString(path, value, cs);";

		assertChange(original, expected);
	}

	@Test
	void visit_NoCatchNoTryStatementParentBlockTwoBufferedWriters_shouldTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class,
				java.nio.file.Files.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class);

		String original = "" +
				"	String value = \"Hello World!\";\n" +
				"	Charset cs = StandardCharsets.UTF_8;\n" +
				"	Path path = Paths.get(\"/home/test/testpath\");\n" +
				"	if(true) try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, cs);\n" +
				"			BufferedWriter bufferedWriter2 = Files.newBufferedWriter(path, cs)) {\n" +
				"		bufferedWriter.write(value);\n" +
				"		bufferedWriter2.write(value);\n" +
				"	}";
		String expected = "" +
				"	String value=\"Hello World!\";\n" +
				"	Charset cs=StandardCharsets.UTF_8;\n" +
				"	Path path=Paths.get(\"/home/test/testpath\");\n" +
				"	if (true) {\n" +
				"		Files.writeString(path,value,cs);\n" +
				"		Files.writeString(path,value,cs);\n" +
				"	}";

		assertChange(original, expected);
	}

	/**
	 * Bug fix SIM-1918
	 */
	@Test
	void visit_TWRUsingFilesNewBufferedWriterNoCatchButFinally_shouldTransform() throws Exception {

		addImports(java.io.Writer.class,
				java.nio.charset.StandardCharsets.class,
				java.nio.file.Files.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class);

		String original = "" +
				"		String text = \"Hello World!\";\n" +
				"		Path path = Paths.get(\"/home/test/testpath\");\n" +
				"		try (Writer out = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {\n" +
				"			out.write(text);\n" +
				"		} finally {\n" +
				"		}";
		String expected = "" +
				"		String text = \"Hello World!\";\n" +
				"		Path path = Paths.get(\"/home/test/testpath\");\n" +
				"		try {\n" +
				"			Files.writeString(path, text, StandardCharsets.UTF_8);\n" +
				"		} finally {\n" +
				"		}";

		assertChange(original, expected);
	}
}
