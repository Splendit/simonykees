package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseFilesWriteStringNegativeASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseFilesWriteStringASTVisitor());
		setJavaVersion(JavaCore.VERSION_11);
	}

	@Test
	public void visit_WriteIntToFile_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"int value = 'A';\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_WriteIfTrue_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"String value = \"Hello World!\";\n" +
				"String pathString = \"/home/test/testpath\";\n" +
				"Charset cs = StandardCharsets.UTF_8;\n" +
				"try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs))) {\n" +
				"	if(true) bufferedWriter.write(value);\n" +
				"} catch (Exception exception) {\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_WriteWithinNestedBlock_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"			String value = \"Hello World!\";\n"
				+ "			String pathString = \"/home/test/testpath\";\n"
				+ "			Charset cs = StandardCharsets.UTF_8;\n"
				+ "			try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs))) {\n"
				+ "				{\n"
				+ "					bufferedWriter.write(value);\n"
				+ "				}\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}";
		assertNoChange(original);
	}

	@Test
	public void visit_WriteStringWithinNestedTryStatement_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"			String value = \"Hello World!\";\n"
				+ "			String pathString = \"/home/test/testpath\";\n"
				+ "			Charset cs = StandardCharsets.UTF_8;\n"
				+ "			try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs))) {\n"
				+ "				try {\n"
				+ "					bufferedWriter.write(value);\n"
				+ "				} catch (Exception exception) {\n"
				+ "				}\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}";
		assertNoChange(original);
	}

	@Test
	public void visit_MethodInvocationWithoutExpression_shouldNotTransform() throws Exception {
		fixture.addMethod("exampleMethod");
		String original = "exampleMethod();";
		assertNoChange(original);
	}

	@Test
	public void visit_ThisMethodInvocation_shouldNotTransform() throws Exception {
		fixture.addMethod("exampleMethod");
		String original = "this.exampleMethod();";
		assertNoChange(original);
	}

	@Test
	public void visit_VarMethodInvocationWithSimpleNameExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"		var o = new Object() {\n"
				+ "			void exampeMethod() {}\n"
				+ "		};\n"
				+ "		o.exampeMethod();";
		assertNoChange(original);
	}

	@Test
	public void visit_MethodInvocationNotInExpressionStatement_shouldNotTransform() throws Exception {
		String original = "" +
				"		String s = \"Hello World!\";\n"
				+ "		int i = s.length();";
		assertNoChange(original);
	}

	@Test
	public void visit_BufferedWriterUsedTwice_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"			String value = \"Hello World!\";\n"
				+ "			String pathString = \"/home/test/testpath\";\n"
				+ "			Charset cs = StandardCharsets.UTF_8;\n"
				+ "			try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs))) {\n"
				+ "				bufferedWriter.write(value);\n"
				+ "				String s = bufferedWriter.toString();\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}";
		assertNoChange(original);
	}

	@Test
	public void visit_NoVariableDeclarationExpressionFound_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"			String value = \"Hello World!\";\n"
				+ "			String pathString = \"/home/test/testpath\";\n"
				+ "			Charset cs = StandardCharsets.UTF_8;			\n"
				+ "			try {\n"
				+ "				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs));\n"
				+ "				bufferedWriter.write(value);\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}";
		assertNoChange(original);
	}

	@Test
	public void visit_BufferedWriterWithTwoArguments_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"			String value = \"Hello World!\";\n"
				+ "			String pathString = \"/home/test/testpath\";\n"
				+ "			Charset cs = StandardCharsets.UTF_8;\n"
				+ "			try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs), 1000)) {\n"
				+ "				bufferedWriter.write(value);\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}";
		assertNoChange(original);
	}

	@Test
	public void visit_TWRUsingMethodNotOfFilesClass_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class,
				java.nio.file.OpenOption.class,
				java.nio.file.Path.class,
				java.nio.file.Paths.class,
				java.nio.file.StandardOpenOption.class);

		String original = "" +
				"		class LocalClass {\n"
				+ "			BufferedWriter newBufferedWriter(Path path, Charset cs, OpenOption... openOptions) {\n"
				+ "				return null;\n"
				+ "			}\n"
				+ "		}\n"
				+ "		OpenOption openOption1 = StandardOpenOption.CREATE;\n"
				+ "		OpenOption openOption2 = StandardOpenOption.APPEND;\n"
				+ "		String value = \"Hello World!\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		Path path = Paths.get(\"/home/test/testpath\");\n"
				+ "		try (BufferedWriter bufferedWriter = new LocalClass().newBufferedWriter(path, cs, openOption1, openOption2)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	public void visit_TWRUsingFileWriterVariableNotResource_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try {\n"
				+ "			FileWriter fileWriter = new FileWriter(pathString, cs);\n"
				+ "			try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {\n"
				+ "				bufferedWriter.write(value);\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}\n"
				+ "		} catch (Exception exception1) {\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	public void visit_TWRUsingFileWriterResourceInitializedWithVariable_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try {\n"
				+ "			FileWriter fileWriter0 = new FileWriter(pathString, cs);\n"
				+ "			try (FileWriter fileWriter = fileWriter0; BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {\n"
				+ "				bufferedWriter.write(value);\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}\n"
				+ "		} catch (Exception exception1) {\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	public void visit_TWRFileWriterResourceToString_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try {\n"
				+ "			try (FileWriter fileWriter = new FileWriter(pathString, cs); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {\n"
				+ "				bufferedWriter.write(value);\n"
				+ "				String s = fileWriter.toString();\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}\n"
				+ "		} catch (Exception exception1) {\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	public void visit_FileWriterResourceFromFileVariable_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.File.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		File file = new File(pathString);\n"
				+ "		try (FileWriter fileWriter = new FileWriter(file, cs);\n"
				+ "				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	public void visit_NewFileWriterFromFileVariable_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.File.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		File file = new File(pathString);\n"
				+ "		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, cs))) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	public void visit_FileWriterResourceConstructedWithCharsetAndBoolean_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.File.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try (FileWriter fileWriter = new FileWriter(new File(pathString), cs, true);\n"
				+ "				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	public void visit_FileWriterResourceConstructedWithoutCharsetWithBoolean_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.File.class,
				java.io.FileWriter.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		try (FileWriter fileWriter = new FileWriter(new File(pathString), true);\n"
				+ "				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	public void visit_NewFileWriterWithCharsetAndBoolean_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.File.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		Charset cs = StandardCharsets.UTF_8;\n"
				+ "		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(pathString), cs, true))) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	public void visit_NewFileWriterWithoutCharsetWithBoolean_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.File.class,
				java.io.FileWriter.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(pathString), true))) {\n"
				+ "			bufferedWriter.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";
		assertNoChange(original);
	}

	@Test
	public void visit_FileWriterNotSimpleName_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.File.class,
				java.io.FileWriter.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		try {\n"
				+ "			var wrapper = new Object() {\n"
				+ "				FileWriter fileWriter = new FileWriter(new File(pathString));\n"
				+ "			};\n"
				+ "			try (BufferedWriter bufferedWriter = new BufferedWriter(wrapper.fileWriter)) {\n"
				+ "				bufferedWriter.write(value);\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		assertNoChange(original);
	}

	@Test
	public void visit_WriteInvocationOfAnonymousClass_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.io.IOException.class);

		String original = "" +
				"		String value = \"Hello World!\";\n"
				+ "		String pathString = \"/home/test/testpath\";\n"
				+ "		try (BufferedWriter bufferedWriterAnonymous = new BufferedWriter(new FileWriter(pathString)) {\n"
				+ "			@Override\n"
				+ "			public void write(String s) throws IOException {\n"
				+ "				super.write(s);\n"
				+ "			}\n"
				+ "		}) {\n"
				+ "			bufferedWriterAnonymous.write(value);\n"
				+ "		} catch (Exception exception) {\n"
				+ "		}";

		assertNoChange(original);
	}

	@Test
	public void visit_WriteWithoutExpression_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.Writer.class);

		String original = "" +
				"			class LocalClass extends BufferedWriter {\n"
				+ "				public LocalClass(Writer out) {super(out);}\n"
				+ "				void test(String value) {\n"
				+ "					try {\n"
				+ "						write(value);\n"
				+ "					} catch (Exception exception) {\n"
				+ "					}\n"
				+ "				}\n"
				+ "			}";

		assertNoChange(original);
	}

	@Test
	public void visit_WriteWithThisQualifier_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.Writer.class);

		String original = "" +
				"			class LocalClass extends BufferedWriter {\n"
				+ "				public LocalClass(Writer out) {super(out);}\n"
				+ "				void test(String value) {\n"
				+ "					try {\n"
				+ "						this.write(value);\n"
				+ "					} catch (Exception exception) {\n"
				+ "					}\n"
				+ "				}\n"
				+ "			}";

		assertNoChange(original);
	}

	@Test
	public void visit_WriteWithSuperQualifier_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.Writer.class);

		String original = "" +
				"			class LocalClass extends BufferedWriter {\n"
				+ "				public LocalClass(Writer out) {super(out);}\n"
				+ "				@Override\n"
				+ "				public void write(String value) {\n"
				+ "					try {\n"
				+ "						super.write(value);\n"
				+ "					} catch (Exception exception) {\n"
				+ "					}\n"
				+ "				}\n"
				+ "			}";

		assertNoChange(original);
	}
}
