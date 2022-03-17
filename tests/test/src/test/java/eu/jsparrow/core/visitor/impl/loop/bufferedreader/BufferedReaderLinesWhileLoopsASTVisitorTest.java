package eu.jsparrow.core.visitor.impl.loop.bufferedreader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.bufferedreader.BufferedReaderLinesASTVisitor;

/**
 * Visitor tests for {@link BufferedReaderLinesASTVisitor}.
 * 
 * @since 3.3.0
 *
 */
class BufferedReaderLinesWhileLoopsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new BufferedReaderLinesASTVisitor());
		fixture.addImport("java.io.BufferedReader");
		fixture.addImport("java.io.FileReader");
	}

	@Test
	void visit_usingBufferedReader_shouldTransform() throws Exception {
		String original = "" + 
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" +
				"			String line;\n" +
				"			while((line = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		String expected = ""
				+ "		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			bufferedReader.lines().forEach(line -> {\n" +
				"				System.out.println(line);\n" +
				"			});\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	void visit_identifyingLineDeclaration_shouldTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			if(true) {\n" +
				"				String line = \"\";\n" +
				"			}\n" +
				"			String line;\n" +
				"			while((line = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		String expected = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {	\n"
				+
				"			if(true) {\n" +
				"				String line = \"\";\n" +
				"			}\n" +
				"			bufferedReader.lines().forEach(line -> {\n" +
				"				System.out.println(line);\n" +
				"			});\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	void visit_multipleDeclarationFragment_shouldTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {			\n"
				+
				"			String line, line2;\n" +
				"			while((line = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		String expected = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {	\n"
				+
				"			String line2;\n" +
				"			bufferedReader.lines().forEach(line -> {\n" +
				"				System.out.println(line);\n" +
				"			});\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	void visit_missingLinesDeclaration_shouldNotTransform() throws Exception {
		String original = "" +
				"		String line;\n" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			while((line = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_missingBufferDeclaration_shouldNotTransform() throws Exception {
		String original = "" +
				"		BufferedReader bufferedReader;\n" +
				"		try {\n" +
				"			bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"));\n" +
				"			String line;\n" +
				"			while((line = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_referencedLineVariable_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line;\n" +
				"			while((line = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"			line = \"\";\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_referencedBufferedReader_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line;\n" +
				"			bufferedReader.readLine();\n" +
				"			while((line = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_accessingNonFinalVariablesInLoop_shouldNotTransform() throws Exception {
		String original = "" +
				"		String nonFinal = \"\";\n" +
				"		nonFinal = \"\";\n" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line;\n" +
				"			while((line = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line + nonFinal);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_missingLineAssignment_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line = \"\";\n" +
				"			while(line != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_wrongInfixOperator_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line = \"\";\n" +
				"			while((line = bufferedReader.readLine()) == null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_missingInfixExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line = \"\";\n" +
				"			while(false) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_missingNullLiteral_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line = \"\";\n" +
				"			while((line = bufferedReader.readLine()) != \"\") {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_missingAssignment_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line = \"\";\n" +
				"			while((bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_missingRHSMethodInvocation_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line = \"\";\n" +
				"			while((line = null) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_missingRHSReadLineInvocation_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line;\n" +
				"			while((line = bufferedReader.readLine().toString()) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_missingLHSSimpleName_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line=\"\";\n" +
				"			class Pair {public String value;}\n" +
				"			Pair p = new Pair();\n" +
				"			while((p.value = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_loopWithBreakStatement_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n"
				+
				"			String line = \"\";\n" +
				"			while((line = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"				if(line.isEmpty()) {\n" +
				"					break;\n" +
				"				}\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	void visit_usingBufferedReaderInLoopBody_shouldNotTransform() throws Exception {
		String original = ""
				+ "		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {			\n"
				+
				"			String line;\n" +
				"			while((line = bufferedReader.readLine()) != null) {\n" +
				"				System.out.println(line);\n" +
				"				System.out.println(bufferedReader.readLine());\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}
}
