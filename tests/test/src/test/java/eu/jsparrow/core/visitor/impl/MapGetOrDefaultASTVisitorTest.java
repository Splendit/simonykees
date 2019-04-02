package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class MapGetOrDefaultASTVisitorTest extends UsesJDTUnitFixture {

	private MapGetOrDefaultASTVisitor visitor;
	
	@BeforeEach
	public void setUp() throws Exception {
		visitor = new MapGetOrDefaultASTVisitor();
		fixture.addImport("java.util.Map");
		fixture.addImport("java.util.HashMap");
	}

	@Test
	public void visit_declarationFollowedByNullCheck_shouldTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		String expected = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.getOrDefault(\"key\", \"default\");";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_multipleDeclarationsFollowedByNullCheck_shouldTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\"), value2;\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		String expected = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.getOrDefault(\"key\", \"default\"), value2;";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_inversedNullCheck_shouldTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(null == value) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		String expected = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.getOrDefault(\"key\", \"default\");";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	
	@Test
	public void visit_assignmentFollowedByNullCheck_shouldTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value;\n" + 
				"		value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		String expected = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value;\n" + 
				"		value = map.getOrDefault(\"key\", \"default\");";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_singleBodyIfStatement_shouldTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value;\n" + 
				"		value = map.get(\"key\");\n" + 
				"		if(value == null) \n" + 
				"			value = \"default\";\n";
		String expected = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value;\n" + 
				"		value = map.getOrDefault(\"key\", \"default\");";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	/*
	 * Negative test cases
	 */
	
	@Test
	public void visit_listGet_shouldNotTransform() throws Exception {
		String original = ""
				+ "		List<String> map = new ArrayList<>();\n" + 
				"		String value = map.get(0);\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";

		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingAssignment_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = \"\";\n" + 
				"		map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingFollowingIfStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		String t = value;" +
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_usedBeforeAssigningDefault_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\"), t = value;\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingFollowingStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\");\n";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingNullCheck_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value.isEmpty()) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_compoundCondition_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null || value.isEmpty()) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingDefaultValueAssignment_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			map = new HashMap<>();\n" + 
				"		}";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_multipleIfBodyStatements_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"\";\n" + 
				"			System.out.println(value);\n" + 
				"		}";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_elseIfStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Map<String, String> map = new HashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"\";\n" + 
				"		} else {\n" + 
				"			\n" + 
				"		}";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
}
