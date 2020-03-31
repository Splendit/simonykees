package eu.jsparrow.core.visitor.impl.loop.bufferedreader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.bufferedreader.BufferedReaderLinesASTVisitor;

@SuppressWarnings("nls")
public class BufferedReaderLinesForLoopsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new BufferedReaderLinesASTVisitor());
		fixture.addImport("java.io.BufferedReader");
		fixture.addImport("java.io.FileReader");
	}

	@Test
	public void visit_forLoop_shouldTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n"	+
				"			for(String line; (line = bufferedReader.readLine())  != null;) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		String expected = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n"	+
				"			bufferedReader.lines().forEach(line -> {\n" +
				"				System.out.println(line);				\n" +
				"			});\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	public void visit_emptyInitializer_shouldTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n"	+
				"			String line;\n" +
				"			for(; (line = bufferedReader.readLine()) != null;) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		String expected = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n"	+
				"			bufferedReader.lines().forEach(line -> {\n" +
				"				System.out.println(line);				\n" +
				"			});\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertChange(original, expected);
	}

	@Test
	public void visit_nonEmptyInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n"	+
				"			String line;\n" +
				"			for(String line2; (line = bufferedReader.readLine()) != null;) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_multipleInitializers_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n"	+
				"			for(String line, line2; (line = bufferedReader.readLine()) != null;) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_nonEmptyUpdater_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n"	+
				"			String line;\n" +
				"			for(; (line = bufferedReader.readLine()) != null; line = \"\") {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_noExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n"	+
				"			String line;\n" +
				"			for(; ;) {\n" +
				"				System.out.println(line);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		assertNoChange(original);
	}
}
