package eu.jsparrow.core.visitor.impl.lambdaforeach;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachMapASTVisitor;

class LambdaForEachMapASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setVisitor(new LambdaForEachMapASTVisitor());
	}

	@Test
	void visit_rawStream_shouldNotReplace() throws Exception {

		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");

		assertNoChange("" +
				"		List strings = new ArrayList<String>();\n" +
				"		strings.forEach(value -> {\n" +
				"			String subValue = value.toString();\n" +
				"			int length = subValue.length();\n" +
				"			if(length > 0) {\n" +
				"				\n" +
				"			}\n" +
				"		});");
	}

	@Test
	void visit_typedStream_shouldReplace() throws Exception {
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");

		String original = "" +
				"	List<String> strings = new ArrayList<String>();\n" +
				"	strings.forEach(value -> {\n" +
				"		String subValue = value.toString();\n" +
				"		int length = subValue.length();\n" +
				"		if(length > 0) {\n" +
				"			\n" +
				"		}\n" +
				"	});";

		String expected = "" +
				"List<String> strings=new ArrayList<String>();\n" +
				"strings.stream()" +
				"	.map(value -> value.toString())" +
				"	.forEach(subValue -> {\n" +
				"		int length=subValue.length();\n" +
				"		if (length > 0) {\n" +
				"			\n" +
				" 		}\n" +
				"	});";

		assertChange(original, expected);
	}
	
	@Test
	void visit_duplicatedFinalModifier_shouldReplace() throws Exception {
		/*
		 * SIM-1914
		 */
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");

		String original = "" +
				"	List<String> strings = new ArrayList<String>();\n" +
				"	strings.forEach((final String value) -> {\n" +
				"		final String subValue = value.toString();\n" +
				"		int length = subValue.length();\n" +
				"	});";

		String expected = "" +
				"List<String> strings=new ArrayList<String>();\n" +
				"strings.stream()" +
				"	.map((final String value) -> value.toString())" +
				"	.forEach((final String subValue) -> {\n" +
				"		int length=subValue.length();\n" +
				"	});";

		assertChange(original, expected);
	}
}
