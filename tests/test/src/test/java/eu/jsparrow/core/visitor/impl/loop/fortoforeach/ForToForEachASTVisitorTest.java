package eu.jsparrow.core.visitor.impl.loop.fortoforeach;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class ForToForEachASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private ForToForEachASTVisitor visitor;

	@BeforeEach
	public void beforeEach() {
		visitor = new ForToForEachASTVisitor();
	}
	
	@Test
	public void visit_loopOverArray_shouldTransform() throws Exception {
		String original = "" +
				"		String[] ms = {};\n" + 
				"		StringBuilder sb = new StringBuilder();\n" + 
				"		for (int i = 0; i < ms.length; i++) {\n" + 
				"			String s = ms[i];\n" + 
				"			sb.append(s);\n" + 
				"		}";
		String expected ="" +
				"		String[] ms = {};\n" + 
				"		StringBuilder sb = new StringBuilder();\n" + 
				"		for (String s : ms) {\n" + 
				"			sb.append(s);\n" + 
				"		}";
		
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);
		assertMatch(ASTNodeBuilder.createBlockFromString(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_variableDeclarationExpression_shouldTransform() throws Exception {
		String original = "" +
				"		Map<String, String>[] table = (Map<String, String>[])new HashMap[10];\n" + 
				"		for (int i = 0; i<table.length; i++) {\n" + 
				"			int bucketLength = 0;\n" + 
				"			for(Map<String, String> map = table[i]; map != null; ) {\n" + 
				"				bucketLength++;\n" + 
				"			}\n" + 
				"		}";
		String expected ="" +
				"		Map<String, String>[] table = (Map<String, String>[])new HashMap[10];\n" + 
				"		for (Map<String, String> map : table) {\n" + 
				"			int bucketLength = 0;\n" + 
				"			for(; map != null; ) {\n" + 
				"				bucketLength++;\n" + 
				"			}\n" + 
				"		}";
		
		fixture.addMethodBlock(original);
		fixture.addImport("java.util.Map");
		fixture.addImport("java.util.HashMap");
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);
		assertMatch(ASTNodeBuilder.createBlockFromString(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_multipleVariableDeclarationExpressions_shouldTransform() throws Exception {
		String original = "" +
				"		Map<String, String>[] table = (Map<String, String>[])new HashMap[10];\n" + 
				"		for (int i = 0; i<table.length; i++) {\n" + 
				"			int bucketLength = 0;\n" + 
				"			for(Map<String, String> map = table[i], map2 = new HashMap<>(); map != null; ) {\n" + 
				"				bucketLength++;\n" + 
				"			}\n" + 
				"		}";
		String expected ="" +
				"		Map<String, String>[] table = (Map<String, String>[])new HashMap[10];\n" + 
				"		for (Map<String, String> map : table) {\n" + 
				"			int bucketLength = 0;\n" + 
				"			for(Map<String, String> map2 = new HashMap<>(); map != null; ) {\n" + 
				"				bucketLength++;\n" + 
				"			}\n" + 
				"		}";
		
		fixture.addMethodBlock(original);
		fixture.addImport("java.util.Map");
		fixture.addImport("java.util.HashMap");
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);
		assertMatch(ASTNodeBuilder.createBlockFromString(expected), fixture.getMethodBlock());
	}

}
