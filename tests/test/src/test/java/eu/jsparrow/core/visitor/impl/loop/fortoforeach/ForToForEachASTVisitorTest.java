package eu.jsparrow.core.visitor.impl.loop.fortoforeach;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;

@SuppressWarnings("nls")
public class ForToForEachASTVisitorTest extends UsesSimpleJDTUnitFixture {

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
		
		assertChange(original, expected);
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
		
		fixture.addImport("java.util.Map");
		fixture.addImport("java.util.HashMap");
		
		assertChange(original, expected);
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
		
		fixture.addImport("java.util.Map");
		fixture.addImport("java.util.HashMap");
		
		assertChange(original, expected);
	}
}
