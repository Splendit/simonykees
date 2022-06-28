package eu.jsparrow.core.visitor.impl.optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.optional.OptionalIfPresentOrElseASTVisitor;

class OptionalIfPresentOrElseExceptionASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		setDefaultVisitor(new OptionalIfPresentOrElseASTVisitor());
		defaultFixture.addImport("java.util.Optional");
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_ifPresentExceptionInCatchClause_shouldNotTransform() throws Exception {
		String original = "" +
				"	public void ifPresentExceptionInCatchClause(Optional<String> optional) throws Exception {\n"
				+ "		if (optional.isPresent()) {\n"
				+ "			final String value = optional.get();\n"
				+ "			try {\n"
				+ "				useStringWithException(value);\n"
				+ "			} catch (Exception exception) {\n"
				+ "				useStringWithException(\"HelloWorld\");\n"
				+ "			}\n"
				+ "		} else {\n"
				+ "			useString(\"HelloWorld\");\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	void useString(String string) {\n"
				+ "\n"
				+ "	}\n"
				+ "\n"
				+ "	void useStringWithException(String string) throws Exception {\n"
				+ "		throw new Exception();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_ifNotPresentExceptionInCatchClause_shouldNotTransform() throws Exception {

		String original = "" +
				"	public void ifNotPresentExceptionInCatchClause(Optional<String> optional) throws Exception {\n"
				+ "		if (optional.isPresent()) {\n"
				+ "			final String value = optional.get();\n"
				+ "			useString(value);\n"
				+ "		} else {\n"
				+ "			final String value = \"Hello World!\";\n"
				+ "			try {\n"
				+ "				useStringWithException(value);\n"
				+ "			} catch (Exception exception) {\n"
				+ "				useStringWithException(\"Hello Other World!\");\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	void useString(String string) {\n"
				+ "\n"
				+ "	}\n"
				+ "\n"
				+ "	void useStringWithException(String string) throws Exception {\n"
				+ "		throw new Exception();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_ifPresentUnhandledCloseException_shouldNotTransform() throws Exception {

		defaultFixture.addImport("java.io.BufferedReader");
		defaultFixture.addImport("java.io.FileReader");

		String original = "" +
				"	public void ifPresentUnhandledCloseException(Optional<FileReader> optional) throws Exception {\n"
				+ "		if (optional.isPresent()) {\n"
				+ "			final FileReader fileReader = optional.get();\n"
				+ "			try (BufferedReader br = new BufferedReader(fileReader)) {\n"
				+ "\n"
				+ "			}\n"
				+ "		} else {\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_ifPresentHandledThrowStatement_shouldTransform() throws Exception {

		String original = "" +
				"	public void ifPresentHandledThrowStatement(Optional<String> optional) {\n"
				+ "		if (optional.isPresent()) {\n"
				+ "			final String value = optional.get();\n"
				+ "			try {\n"
				+ "				if (value.isBlank()) {\n"
				+ "					throw new Exception();\n"
				+ "				}\n"
				+ "			} catch (Exception exc) {\n"
				+ "			}\n"
				+ "		} else {\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void ifPresentHandledThrowStatement(Optional<String> optional) {\n"
				+ "		optional.ifPresentOrElse(value -> {\n"
				+ "			try {\n"
				+ "				if (value.isBlank()) {\n"
				+ "					throw new Exception();\n"
				+ "				}\n"
				+ "			} catch (Exception exc) {\n"
				+ "			}\n"
				+ "		},() -> {\n"
				+ "		});"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_ifNotPresentThrowRuntimeException_shouldTransform() throws Exception {
		String original = "" +
				"	public void ifNotPresentThrowRuntimeException(Optional<String> optional) {\n"
				+ "		if (optional.isPresent()) {\n"
				+ "			final String value = optional.get();\n"
				+ "			System.out.println(value);\n"
				+ "		} else {\n"
				+ "			throw new RuntimeException();\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void ifNotPresentThrowRuntimeException(Optional<String> optional) {\n"
				+ "		optional.ifPresentOrElse(value -> System.out.println(value),() -> {\n"
				+ "			throw new RuntimeException();\n"
				+ "		});"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_ifNotPresentThrowException_shouldNotTransform() throws Exception {
		String original = "" +
				"	public void ifNotPresentThrowRuntimeException(Optional<String> optional) throws Exception {\n"
				+ "		if (optional.isPresent()) {\n"
				+ "			final String value = optional.get();\n"
				+ "			System.out.println(value);\n"
				+ "		} else {\n"
				+ "			throw new Exception();\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_ifPresentThenTryWithHandledExceptionWithoutBraces_shouldTransform() throws Exception {
		String original = "" +
				"	public void ifPresentThenTryWithHandledEException(Optional<String> optional) {\n"
				+ "		if (optional.isPresent())\n"
				+ "			try {\n"
				+ "				final String value = optional.get();\n"
				+ "				if (value.isEmpty())\n"
				+ "					throw new Exception();\n"
				+ "				System.out.println(value);\n"
				+ "			} catch (Exception exc) {\n"
				+ "\n"
				+ "			}\n"
				+ "		else {\n"
				+ "			System.out.println(\"default\");\n"
				+ "		}\n"
				+ "	}";
		String expected = "" +
				"	public void ifPresentThenTryWithHandledEException(Optional<String> optional) {\n"
				+ "		optional.ifPresentOrElse(value -> {\n"
				+ "			try {\n"
				+ "				if (value.isEmpty())\n"
				+ "					throw new Exception();\n"
				+ "				System.out.println(value);\n"
				+ "			} catch (Exception exc) {\n"
				+ "\n"
				+ "			}\n"
				+ "		}, () -> System.out.println(\"default\"));\n"
				+ "	}";

		assertChange(original, expected);

	}

	/**
	 * This test may fail in the future if braces for the else clause may not be
	 * necessary for transformation any more.
	 */
	@Test
	void visit_ifNotPresentThenTryWithHandledExceptionWithoutBraces_shouldNotTransform() throws Exception {

		String original = "" +
				"	public void ifPresentThenTryWithHandledEException(Optional<String> optional) {\n"
				+ "		if (optional.isPresent()) {\n"
				+ "			final String value = optional.get();\n"
				+ "			System.out.println(value);\n"
				+ "		} else \n"
				+ "			try {\n"
				+ "				throw new Exception();\n"
				+ "			} catch (Exception exc) {\n"
				+ "\n"
				+ "			}\n"
				+ "	}";

		assertNoChange(original);
	}

}
