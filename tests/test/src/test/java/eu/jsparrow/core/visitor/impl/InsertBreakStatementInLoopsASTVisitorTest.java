package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class InsertBreakStatementInLoopsASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new InsertBreakStatementInLoopsASTVisitor());
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.Arrays");
		fixture.addImport("java.util.Collections");
	}
	
	@Test
	public void visit_hasEmptyValue_shouldTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) {\n" + 
				"            if (value.isEmpty()) {\n" + 
				"                contains = false;\n" + 
				"            }\n" + 
				"        }";
		String expected = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) {\n" + 
				"            if (value.isEmpty()) {\n" + 
				"                contains = false;\n" + 
				"                break;\n" + 
				"            }\n" + 
				"        }";
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_compoundCondition_shouldTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) {\n" + 
				"            if (!value.isEmpty() || (value.equals(\"-\")) || value.equalsIgnoreCase(\"another String\")) {\n" +
				"                contains = false;\n" + 
				"            }\n" + 
				"        }";
		String expected = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) {\n" + 
				"            if (!value.isEmpty() || (value.equals(\"-\")) || value.equalsIgnoreCase(\"another String\")) {\n" + 
				"                contains = false;\n" + 
				"                break;\n" + 
				"            }\n" + 
				"        }";
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_listOfObjects_shouldTransform() throws Exception {
		String original = "" +
				"        boolean contains = false;\n" + 
				"        List<Object> values = Collections.emptyList();\n" + 
				"        for (Object value : values) {\n" + 
				"            if (value.hashCode() == 37) {\n" + 
				"                contains = true;\n" + 
				"            }\n" + 
				"        }";
		String expected = "" +
				"        boolean contains = false;\n" + 
				"        List<Object> values = Collections.emptyList();\n" + 
				"        for (Object value : values) {\n" + 
				"            if (value.hashCode() == 37) {\n" + 
				"                contains = true;\n" +
				"                break;\n" + 
				"            }\n" + 
				"        }";
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_stringEndsWith_shouldTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) {\n" + 
				"            if (value.endsWith(\"t\")) {\n" + 
				"                contains = false;\n" + 
				"            }\n" + 
				"        }";
		String expected = "" +
				"         boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) {\n" + 
				"            if (value.endsWith(\"t\")) {\n" + 
				"                contains = false;\n" +
				"                break;\n" + 
				"            }\n" + 
				"        }";
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_collectionContains_shouldTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : Collections.emptyList()) {\n" + 
				"            if (values.contains(value)) {\n" + 
				"                contains = false;\n" + 
				"            }\n" + 
				"        }";
		String expected = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : Collections.emptyList()) {\n" + 
				"            if (values.contains(value)) {\n" + 
				"                contains = false;\n" + 
				"                break;\n" + 
				"            }\n" + 
				"        }";
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_missingCurlyBraces_shouldTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) \n" + 
				"            if (value.isEmpty()) {\n" + 
				"                contains = false;\n" + 
				"            }\n" + 
				"        ";
		String expected = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) \n" + 
				"            if (value.isEmpty()) {\n" + 
				"                contains = false;\n" +
				"                break;\n" + 
				"            }\n" + 
				"        ";
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_missingBracesInIf_shouldTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) \n" + 
				"            if (value.isEmpty()) \n" + 
				"                contains = false;\n";
		String expected = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) \n" + 
				"            if (value.isEmpty()) {\n" + 
				"                contains = false;\n" + 
				"                break;\n" +
				"            }\n"; 
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_iteratingOverCollection_shouldTransform() throws Exception {
		String original = "" +
				"        boolean contains = false;\n" + 
				"        Collection<String> collection = Collections.singletonList(\"value\");\n" + 
				"        for (String value : collection) {\n" + 
				"            if (collection.contains(value)) {\n" + 
				"                contains = true;\n" + 
				"            }\n" + 
				"        }";
		String expected = "" +
				"        boolean contains = false;\n" + 
				"        Collection<String> collection = Collections.singletonList(\"value\");\n" + 
				"        for (String value : collection) {\n" + 
				"            if (collection.contains(value)) {\n" + 
				"                contains = true;\n" +
				"                break;\n" + 
				"            }\n" + 
				"        }";
		
		fixture.addImport("java.util.Collection");
		
		assertChange(original, expected);
	}
	
	/*
	 * Negative test cases
	 */
	
	@Test
	public void visit_missingLiteralAssignment_shouldNotTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Collections.emptyList();\n" + 
				"        for (String value : values) {\n" + 
				"            if (value.endsWith(\"t\")) {\n" + 
				"                contains = value.length() == 3;\n" + 
				"            }\n" + 
				"        }";
		
		assertNoChange(original);
	}
	
	@Test
	public void visit_elseStatement_shouldNotTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Collections.emptyList();\n" + 
				"        for (String value : values) {\n" + 
				"            if (value.endsWith(\"t\")) {\n" + 
				"                contains = true;\n" + 
				"            } else {\n" + 
				"            }\n" + 
				"        }";
		
		assertNoChange(original);
	}
	
	@Test
	public void visit_multipleStatementsInFor_shouldNotTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Collections.emptyList();\n" + 
				"        for (String value : values) {\n" + 
				"            if (value.endsWith(\"t\")) {\n" + 
				"                contains = true;\n" + 
				"            }\n" + 
				"            contains = true;\n" + 
				"        }";
		
		assertNoChange(original);
	}
	
	@Test
	public void visit_multipleStatementsInIf_shouldNotTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Collections.emptyList();\n" + 
				"        for (String value : values) {\n" + 
				"            if (value.endsWith(\"t\")) {\n" + 
				"                contains = true;\n" + 
				"                contains = true;\n" + 
				"            }\n" + 
				"        }";
		
		assertNoChange(original);
	}
	
	@Test
	public void visit_objectCreationInIfCondition_shouldNotTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Collections.emptyList();\n" + 
				"        for (String value : values) {\n" + 
				"            if (value.endsWith(new StringBuilder().append(\"t\").toString())) {\n" + 
				"                contains = true;\n" + 
				"            }\n" + 
				"        }";
		
		assertNoChange(original);
	}

	@Test
	public void visit_objectCreationInIfCondition2_shouldNotTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Collections.emptyList();\n" + 
				"        for (String value : values) {\n" + 
				"            if (new StringBuilder().append(\"t\").toString().endsWith(value)) {\n" + 
				"                contains = true;\n" + 
				"            }\n" + 
				"        }";
		
		assertNoChange(original);
	}
	
	@Test
	public void visit_missingAssignment_shouldNotTransform() throws Exception {
		String original = "" +
				"        boolean contains = true;\n" + 
				"        List<String> values = Arrays.asList(\"value1\", \"value2\", \"3\");\n" + 
				"        for (String value : values) {\n" + 
				"            if (value.isEmpty()) {\n" + 
				"                System.out.print(value);\n" + 
				"            }\n" + 
				"        }";
		
		assertNoChange(original);
	}
	
	@Test
	public void visit_conditionWithIncrementOperator_shouldNotTransform() throws Exception {
		String original = "" +
				"        boolean contains = false;\n" + 
				"        List<Integer> values = Collections.emptyList();\n" + 
				"        for (int value : values) {\n" + 
				"            if (++value == 2) {\n" + 
				"                contains = true;\n" + 
				"            }\n" + 
				"        }";
		
		assertNoChange(original);
	}
	
	@Test
	public void visit_conditionWithPostfixExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"        boolean contains = false;\n" + 
				"        List<Integer> values = Collections.emptyList();\n" + 
				"        for (int value : values) {\n" + 
				"            if (value++ == 2) {\n" + 
				"                contains = true;\n" + 
				"            }\n" + 
				"        }";
		
		assertNoChange(original);
	}
}
