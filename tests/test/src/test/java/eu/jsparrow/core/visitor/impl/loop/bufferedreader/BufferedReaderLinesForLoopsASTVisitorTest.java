package eu.jsparrow.core.visitor.impl.loop.bufferedreader;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.bufferedreader.BufferedReaderLinesASTVisitor;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class BufferedReaderLinesForLoopsASTVisitorTest extends UsesSimpleJDTUnitFixture  {
	
	private BufferedReaderLinesASTVisitor visitor;

	@BeforeEach
	public void setUp() throws Exception {
		visitor = new BufferedReaderLinesASTVisitor();
		fixture.addImport("java.io.BufferedReader");
		fixture.addImport("java.io.FileReader");
	}
	
	@Test
	public void visit_forLoop_shouldTransform() throws Exception {
		String original = "" + 
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n" + 
				"			for(String line; (line = bufferedReader.readLine())  != null;) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		String expected = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n" + 
				"			bufferedReader.lines().forEach(line -> {\n" + 
				"				System.out.println(line);				\n" + 
				"			});\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_emptyInitializer_shouldTransform() throws Exception {
		String original = "" + 
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n" + 
				"			String line;\n" + 
				"			for(; (line = bufferedReader.readLine()) != null;) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		String expected = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n" + 
				"			bufferedReader.lines().forEach(line -> {\n" + 
				"				System.out.println(line);				\n" + 
				"			});\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_nonEmptyInitializer_shouldNotTransform() throws Exception {
		String original = "" + 
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n" + 
				"			String line;\n" + 
				"			for(String line2; (line = bufferedReader.readLine()) != null;) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(original);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_multipleInitializers_shouldNotTransform() throws Exception {
		String original = "" + 
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n" + 
				"			for(String line, line2; (line = bufferedReader.readLine()) != null;) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(original);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_nonEmptyUpdater_shouldNotTransform() throws Exception {
		String original = "" + 
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n" + 
				"			String line;\n" + 
				"			for(; (line = bufferedReader.readLine()) != null; line = \"\") {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(original);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_noExpression_shouldNotTransform() throws Exception {
		String original = "" + 
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {		\n" + 
				"			String line;\n" + 
				"			for(; ;) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(original);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
}
