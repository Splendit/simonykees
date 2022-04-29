package eu.jsparrow.core.visitor.impl.trycatch.close;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings({ "nls" })
class RemoveRedundantCloseASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setVisitor(new RemoveRedundantCloseASTVisitor());
	}

	@Test
	void visit_redundantClose_shouldBeRemoved() throws Exception {
		fixture.addImport(java.io.BufferedReader.class.getName());
		fixture.addImport(java.io.FileReader.class.getName());
		fixture.addImport(java.io.IOException.class.getName());

		String original = ""
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "			br.close();\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "		}";
		String expected = ""
				+ "		try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n"
				+ "			System.out.println(\"First line: \" + br.readLine());\n"
				+ "		} catch (IOException e) {\n"
				+ "			e.printStackTrace();\n"
				+ "	}";

		assertChange(original, expected);
	}
}
