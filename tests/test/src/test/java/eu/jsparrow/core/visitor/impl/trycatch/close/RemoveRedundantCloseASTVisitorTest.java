package eu.jsparrow.core.visitor.impl.trycatch.close;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings({ "nls" })
class RemoveRedundantCloseASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new RemoveRedundantCloseASTVisitor());
		defaultFixture.addImport(java.io.BufferedReader.class.getName());
		defaultFixture.addImport(java.io.FileReader.class.getName());
		defaultFixture.addImport(java.io.IOException.class.getName());

	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_RedundantClose_shouldTransform() throws Exception {

		String original = ""
				+ "	void readFirstLineFromFile(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "			br.close();\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";
		String expected = ""
				+ "	void readFirstLineFromFile(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"			BufferedReader br2;",
			"" +
					"			br: while (true) {\n" +
					"				break br;\n" +
					"			}",
			"" +
					"			class LocalClass {\n" +
					"				BufferedReader br;\n" +
					"			}",
			"" +
					"",

	})
	void visit_ResourceNotReferencedInTWRBlock_shouldNotTransform(String code) throws Exception {

		String original = ""
				+ "	void resourceNotReferencedInTWRBlock(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ code + "\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_ResourceUsedAfterClose_shouldNotTransform() throws Exception {

		String original = ""
				+ "	void resourceUsedAfterClose(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			 br.close();\n"
				+ "			 useBufferedReader(br);\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	void useBufferedReader(BufferedReader br) {\n"
				+ "	}";

		assertNoChange(original);
	}
	
	@Test
	void visit_ResourceClosedWithinIfStatement_shouldNotTransform() throws Exception {

		String original = ""
				+ "	void closeStatementInsideIfStatement(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			if (true) {\n"
				+ "				br.close();\n"
				+ "			}\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}
	
	@Test
	void visit_ResourceClosedWithinLambdaBody_shouldNotTransform() throws Exception {

		String original = ""
				+ "	static interface InterfaceClosingResource {\n"
				+ "		void closeResource() throws IOException;\n"
				+ "	}\n"
				+ "\n"
				+ "	void closeResourceInsideLambdaBody(String path) {\n"
				+ "\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			InterfaceClosingResource lambdaClosingResource = () -> br.close();\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}
	
	@Test
	void visit_CallClosewithArgument_shouldNotTransform() throws Exception {

		String original = ""
				+ "	void useCloseWithArgument(String path) {\n"
				+ "\n"
				+ "		class LocalBufferedReader extends BufferedReader {\n"
				+ "\n"
				+ "			public LocalBufferedReader(Reader in) {\n"
				+ "				super(in);\n"
				+ "			}\n"
				+ "\n"
				+ "			public void close(boolean arg) {\n"
				+ "\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "		try (LocalBufferedReader br = new LocalBufferedReader(new FileReader(path))) {\n"
				+ "			br.close(false);\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	void visit_CallSkipMethod_shouldNotTransform() throws Exception {

		String original = ""
				+ "	void skipMethodInvocationOnResource(String path) {\n"
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			br.skip(10);\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}
}
