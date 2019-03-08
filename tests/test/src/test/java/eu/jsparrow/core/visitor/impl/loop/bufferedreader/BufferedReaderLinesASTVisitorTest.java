package eu.jsparrow.core.visitor.impl.loop.bufferedreader;

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
	
	@Test
	public void visit_multipleDeclarationFragment_shouldTransform() throws Exception {
		
		String original = ""
				+ "		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {			\n" + 
				"			String line, line2;\n" + 
				"			while((line = bufferedReader.readLine()) != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		String expected = ""
				+ "		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {	\n" + 
				"			String line2;\n" + 
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
	
	@Test
	public void visit_missingLinesDeclaration_shouldNotTransform() throws Exception {
		String block = "" +
				"		String line;\n" + 
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			while((line = bufferedReader.readLine()) != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingBufferDeclaration_shouldNotTransform() throws Exception {
		String block = "" +
				"		BufferedReader bufferedReader;\n" + 
				"		try {\n" + 
				"			bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"));\n" + 
				"			String line;\n"+
				"			while((line = bufferedReader.readLine()) != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_referencedLineVariable_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line;\n" + 
				"			line = \"\";\n" + 
				"			while((line = bufferedReader.readLine()) != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_referencedBufferedReader_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line;\n" + 
				"			bufferedReader.readLine();\n" + 
				"			while((line = bufferedReader.readLine()) != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_accessingNonFinalVariablesInLoop_shouldNotTransform() throws Exception {
		String block = "" +
				"		String nonFinal = \"\";\n" + 
				"		nonFinal = \"\";\n" + 
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line;\n" + 
				"			while((line = bufferedReader.readLine()) != null) {\n" + 
				"				System.out.println(line + nonFinal);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingLineAssignment_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line = \"\";\n" + 
				"			while(line != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_wrongInfixOperator_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line = \"\";\n" + 
				"			while((line = bufferedReader.readLine()) == null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingInfixExpression_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line = \"\";\n" + 
				"			while(false) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingNullLiteral_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line = \"\";\n" + 
				"			while((line = bufferedReader.readLine()) != \"\") {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingAssignment_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line = \"\";\n" + 
				"			while((bufferedReader.readLine()) != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingRHSmethodInvocation_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line = \"\";\n" + 
				"			while((line = null) != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingRHSreadLineInvocation_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line;\n" + 
				"			while((line = bufferedReader.readLine().toString()) != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingLHSsimpleName_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
				"			String line=\"\";\n" + 
				"			class Pair {public String value;}\n" + 
				"			Pair p = new Pair();\n" + 
				"			while((p.value = bufferedReader.readLine()) != null) {\n" + 
				"				System.out.println(line);\n" + 
				"			}\n" + 
				"		} catch (Exception e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_loopWithBreakStateemnt_shouldNotTransform() throws Exception {
		String block = "" +
				"		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(\"file.name.txt\"))) {\n" + 
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
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(block), fixture.getMethodBlock());
	}
	
	public void presample() {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader("file.name.txt"))) {
			String line;
			while((line = bufferedReader.readLine().toString()) != null) {
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
