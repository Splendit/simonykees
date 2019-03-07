package eu.jsparrow.core.visitor.impl.loop.bufferedreader;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import java.io.BufferedReader;
import java.io.FileReader;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.bufferedreader.BufferedReaderLinesASTVisitor;

/**
 * 
 * @since 3.3.0
 *
 */
@SuppressWarnings("nls")
public class BufferedReaderLinesASTVisitorTest  extends UsesJDTUnitFixture {
	
	private BufferedReaderLinesASTVisitor visitor;
	

	@BeforeEach
	public void setUp() throws Exception {
		visitor = new BufferedReaderLinesASTVisitor();
		fixture.addImport("java.io.BufferedReader");
		fixture.addImport("java.io.FileReader");
	}
	
	@Test
	public void visit_usingBufferedReader_shouldTransform() throws Exception {
		
		String original = ""
				+ "		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {			\n" + 
				"			String line;\n" + 
				"			while((line = bufferedReader.readLine()) != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		String expected = ""
				+ "		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {	\n" + 
				"			bufferedReader.lines().forEach(line -> {\n" + 
				"				System.out.println(line);\n" + 
				"			});\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = createBlock(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
		
	}
	
	public void presample() {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader("file.name.txt"))) {			
			String line;
			while((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void postsample() throws Exception {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader("file.name.txt"))) {	
			bufferedReader.lines().forEach(line -> {
				System.out.println(line);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
