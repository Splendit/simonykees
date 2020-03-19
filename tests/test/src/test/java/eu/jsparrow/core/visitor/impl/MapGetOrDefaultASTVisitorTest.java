package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class MapGetOrDefaultASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new MapGetOrDefaultASTVisitor());
		fixture.addImport("java.util.concurrent.ConcurrentHashMap");
		
	}

	@Test
	public void visit_declarationFollowedByNullCheck_shouldTransform() throws Exception {
		String original = "" +
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		String expected = "" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.getOrDefault(\"key\", \"default\");";
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_multipleDeclarationsFollowedByNullCheck_shouldTransform() throws Exception {
		String original = "" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\"), value2;\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		String expected = "" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.getOrDefault(\"key\", \"default\"), value2;";
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_inversedNullCheck_shouldTransform() throws Exception {
		String original = "" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(null == value) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		String expected = "" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.getOrDefault(\"key\", \"default\");";
		
		assertChange(original, expected);		
	}
	
	
	@Test
	public void visit_assignmentFollowedByNullCheck_shouldTransform() throws Exception {
		String original = "" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value;\n" + 
				"		value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}";
		String expected = "" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value;\n" + 
				"		value = map.getOrDefault(\"key\", \"default\");";
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_singleBodyIfStatement_shouldTransform() throws Exception {
		String original = "" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value;\n" + 
				"		value = map.get(\"key\");\n" + 
				"		if(value == null) \n" + 
				"			value = \"default\";\n";
		String expected = "" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value;\n" + 
				"		value = map.getOrDefault(\"key\", \"default\");";
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_mapAllowingNulValues_shouldNotTransform() throws Exception {
		fixture.addImport("java.util.HashMap");
		
		assertNoChange("" + 
				"		HashMap<String, String> map = new HashMap<>();\n" + 
				"		String value;\n" + 
				"		value = map.get(\"key\");\n" + 
				"		if(value == null) \n" + 
				"			value = \"default\";\n");
	}
	
	/*
	 * Negative test cases
	 */
	
	@Test
	public void visit_listGet_shouldNotTransform() throws Exception {
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");
		
		assertNoChange("" + 
				"		List<String> map = new ArrayList<>();\n" + 
				"		String value = map.get(0);\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}");
	}
	
	@Test
	public void visit_missingAssignment_shouldNotTransform() throws Exception {
		assertNoChange("" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = \"\";\n" + 
				"		map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}");
	}
	
	@Test
	public void visit_missingFollowingIfStatement_shouldNotTransform() throws Exception {
		assertNoChange("" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		String t = value;" +
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}");
	}
	
	@Test
	public void visit_usedBeforeAssigningDefault_shouldNotTransform() throws Exception {
		assertNoChange("" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\"), t = value;\n" + 
				"		if(value == null) {\n" + 
				"			value = \"default\";\n" + 
				"		}");
	}
	
	@Test
	public void visit_missingFollowingStatement_shouldNotTransform() throws Exception {
		assertNoChange("" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n");
	}
	
	@Test
	public void visit_missingNullCheck_shouldNotTransform() throws Exception {
		assertNoChange("" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value.isEmpty()) {\n" + 
				"			value = \"default\";\n" + 
				"		}");
	}
	
	@Test
	public void visit_compoundCondition_shouldNotTransform() throws Exception {
		assertNoChange("" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null || value.isEmpty()) {\n" + 
				"			value = \"default\";\n" + 
				"		}");
	}
	
	@Test
	public void visit_missingDefaultValueAssignment_shouldNotTransform() throws Exception {
		assertNoChange("" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			map = new HashMap<>();\n" + 
				"		}");
	}
	
	@Test
	public void visit_multipleIfBodyStatements_shouldNotTransform() throws Exception {
		assertNoChange("" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"\";\n" + 
				"			System.out.println(value);\n" + 
				"		}");
	}
	
	@Test
	public void visit_elseIfStatement_shouldNotTransform() throws Exception {
		assertNoChange("" + 
				"		ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();\n" + 
				"		String value = map.get(\"key\");\n" + 
				"		if(value == null) {\n" + 
				"			value = \"\";\n" + 
				"		} else {\n" + 
				"			\n" + 
				"		}");
	}
	
	@Test
	public void visit_incompatibleDefaultValueType_shouldNotTransform() throws Exception {		
		fixture.addImport("java.util.Collection");
		fixture.addImport("java.util.Collections");
		fixture.addImport("java.util.List");
		
		assertNoChange("" + 
				"		ConcurrentHashMap<String, List<String>> map = new ConcurrentHashMap<>();\n" + 
				"		Collection<String> defaultValue = Collections.emptyList();\n" + 
				"		Collection<String> result = map.get(\"key\");\n" + 
				"		if(result == null) {\n" + 
				"			result = defaultValue;\n" + 
				"		}");
	}
}
