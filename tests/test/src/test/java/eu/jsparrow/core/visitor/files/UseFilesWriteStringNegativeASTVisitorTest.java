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

	private void addImports(Class<?>... classes) throws Exception {
		for (Class<?> c : classes) {
			fixture.addImport(c.getName());
		}
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

	// TestLocalClassMethod

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
				+ "				bufferedWriter.write(value);\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}";
		assertNoChange(original);
	}

	@Test
	public void visit_VariableDeclarationExpressionNotResource_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"			String value = \"Hello World!\";\n"
				+ "			String pathString = \"/home/test/testpath\";\n"
				+ "			Charset cs = StandardCharsets.UTF_8;\n"
				+ "			try {\n"
				+ "				for (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString, cs));;) {\n"
				+ "					bufferedWriter.write(value);\n"
				+ "				}\n"
				+ "			} catch (Exception exception) {\n"
				+ "			}";
		assertNoChange(original);
	}

	@Test
	public void visit_VariableDeclarationExpressionsWithTwoFragments_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class,
				java.io.FileWriter.class,
				java.nio.charset.Charset.class,
				java.nio.charset.StandardCharsets.class);

		String original = "" +
				"			String value = \"Hello World!\";\n"
				+ "			String pathString = \"/home/test/testpath\";\n"
				+ "			Charset cs = StandardCharsets.UTF_8;\n"
				+ "			try {\n"
				+ "				for (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString,\n"
				+ "						cs)), bufferedWriter2 = new BufferedWriter(new FileWriter(pathString, cs));;) {\n"
				+ "					bufferedWriter.write(value);\n"
				+ "				}\n"
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
	public void visit_InitializerIsNull_shouldNotTransform() throws Exception {
		addImports(java.io.BufferedWriter.class);
		String original = "" +
				"			class LocalClass {\n"
				+ "				BufferedWriter bufferedWriter;\n"
				+ "				void test() {\n"
				+ "					String value = \"Hello World!\";\n"
				+ "					try {\n"
				+ "						bufferedWriter.write(value);\n"
				+ "					} catch (Exception exception) {\n"
				+ "					}\n"
				+ "				}\n"
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

	// @Test
	// public void visit__shouldNotTransform() throws Exception {
	// addImports();
	//
	// String original = "" +
	// "";
	// assertNoChange(original);
	// }
}
