package eu.jsparrow.core.visitor.impl.loop.bufferedreader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.bufferedreader.BufferedReaderLinesASTVisitor;

class BufferedReaderLinesExceptionASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new BufferedReaderLinesASTVisitor());

		defaultFixture.addImport("java.io.BufferedReader");
		defaultFixture.addImport("java.io.IOException");
		defaultFixture.addImport("java.nio.file.Files");
		defaultFixture.addImport("java.nio.file.Path");
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/**
	 * Transformation to invalid code.<br>
	 * This test is expected to fail as soon as the corresponding bug has been
	 * fixed.
	 */
	@Test
	void visit_InvocationThrowingExceptionInFinallyBlock_shouldNotTransform() throws Exception {
		String original = "" +
				"	void useLineThrowingExceptionInFinallyBlock(Path path) {\n"
				+ "\n"
				+ "		try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {\n"
				+ "			String line;\n"
				+ "			while ((line = bufferedReader.readLine()) != null) {\n"
				+ "				try {\n"
				+ "					useLineThrowingException(line);\n"
				+ "				} catch (Exception exc) {\n"
				+ "\n"
				+ "				} finally {\n"
				+ "					useLineThrowingException(\"\");\n"
				+ "				}\n"
				+ "			}\n"
				+ "		} catch (Exception e) {\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	void useLineThrowingException(String line) throws Exception {\n"
				+ "		throw new Exception();\n"
				+ "	}";

		String expected = ""
				+ "	void useLineThrowingExceptionInFinallyBlock(Path path) {\n"
				+ "\n"
				+ "		try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {\n"
				+ "			bufferedReader.lines().forEach(line -> {\n"
				+ "				try {\n"
				+ "					useLineThrowingException(line);\n"
				+ "				} catch (Exception exc) {\n"
				+ "\n"
				+ "				} finally {\n"
				+ "					useLineThrowingException(\"\");\n"
				+ "				}\n"
				+ "			});\n"
				+ "		} catch (Exception e) {\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	void useLineThrowingException(String line) throws Exception {\n"
				+ "		throw new Exception();\n"
				+ "	}";

		assertChange(original, expected);
	}

	/**
	 * Code is not transformed although it should be transformed.<br>
	 * This test is expected to fail as soon as the corresponding bug has been
	 * fixed.
	 */
	@Test
	void visit_WhileLoopWithHandledThrowStatement_shouldTransform() throws Exception {
		String original = "" +
				"	void whileLoopWithHandledThrowStatement(Path path) {\n"
				+ "		try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {\n"
				+ "			String line;\n"
				+ "			while ((line = bufferedReader.readLine()) != null) {\n"
				+ "				try {\n"
				+ "					if (line.isBlank()) {\n"
				+ "						throw new Exception();\n"
				+ "					}\n"
				+ "				} catch (Exception exc) {\n"
				+ "\n"
				+ "				}\n"
				+ "				System.out.println(line);\n"
				+ "			}\n"
				+ "		} catch (IOException e) {\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}
	
	
	/**
	 * Code is not transformed although it should be transformed.<br>
	 * This test is expected to fail as soon as the corresponding bug has been
	 * fixed.
	 */
	@Test
	void visit_ForLoopWithHandledThrowStatement_shouldTransform() throws Exception {
		String original = "" +
				"	void forLoopWithHandledThrowStatement(Path path) {\n"
				+ "		try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {\n"
				+ "			for (String line; (line = bufferedReader.readLine()) != null;) {\n"
				+ "				try {\n"
				+ "					if (line.isBlank()) {\n"
				+ "						throw new Exception();\n"
				+ "					}\n"
				+ "				} catch (Exception exc) {\n"
				+ "\n"
				+ "				}\n"
				+ "				System.out.println(line);\n"
				+ "			}\n"
				+ "		} catch (IOException e) {\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}
}
