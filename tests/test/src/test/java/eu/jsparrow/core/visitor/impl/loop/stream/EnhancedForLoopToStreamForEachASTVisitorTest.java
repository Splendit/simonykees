package eu.jsparrow.core.visitor.impl.loop.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;

class EnhancedForLoopToStreamForEachASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setVisitor(new EnhancedForLoopToStreamForEachASTVisitor());
	}

	@Test
	public void visit_rawCollection_shouldNotTransform() throws Exception {

		String original = "" +
				"		List strings = new ArrayList<String>();\n" +
				"		for(Object string : strings) {\n" +
				"			int hashCode = string.hashCode();\n" +
				"		}";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");

		assertNoChange(original);
	}

	@Test
	public void visit_conditionalLoopExpression_shouldNotTranform() throws Exception {
		String original = "" +
				"		List<String> strings = new ArrayList<String>();\n" +
				"		List<String> strings2 = new ArrayList<>();\n" +
				"		Object object = new Object();\n" +
				"		for(Object string : object == null ? strings : strings2) {\n" +
				"			int hashCode = string.hashCode(); \n" +
				"			}";
		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");

		assertNoChange(original);
	}

	@Test
	public void visit_typedCollection_shouldTransform() throws Exception {

		String original = "" +
				"		List<String> strings = new ArrayList<String>();\n" +
				"		for(Object string : strings) {\n" +
				"			int hashCode = string.hashCode();\n" +
				"		}";
		String expected = "" +
				"		List<String> strings = new ArrayList<String>();\n" +
				"		strings.forEach(string -> {\n" +
				"			int hashCode = string.hashCode();\n" +
				"		});";

		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");

		assertChange(original, expected);
	}

	@Test
	public void modifiers_should_not_be_lost() throws Exception {

		String original = "" +
				"		List<String> strings = new ArrayList<String>();\n" +
				"		for(final Object string : strings) {\n" +
				"			int hashCode = string.hashCode();\n" +
				"		}";
		String expected = "" +
				"		List<String> strings = new ArrayList<String>();\n" +
				"		strings.forEach((final Object string) -> {\n" +
				"			int hashCode = string.hashCode();\n" +
				"		});";

		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");

		assertChange(original, expected);
	}

	@Test
	public void annotations_should_not_be_lost() throws Exception {
		String original = "" +
				"		List<String> strings = new ArrayList<String>();\n" +
				"		for(@InitParam(name = \"\", value = \"\") Object string : strings) {\n" +
				"			int hashCode = string.hashCode();\n" +
				"		}";
		String expected = "" +
				"		List<String> strings = new ArrayList<String>();\n" +
				"		strings.forEach((@InitParam(name = \"\", value = \"\")  Object string) -> {\n" +
				"			int hashCode = string.hashCode();\n" +
				"		});";

		fixture.addImport("java.util.List");
		fixture.addImport("java.util.ArrayList");

		assertChange(original, expected);
	}

	@Test
	public void visit_iterableWithWildCard_shouldTransform() throws Exception {
		String original = "" +
				"		class R {\n" +
				"			final String matcher = \"matcher\";\n" +
				"			final String sampler = \"sampler\";\n" +
				"		}\n" +
				"		class Rule extends R {}\n" +
				"		\n" +
				"		List<? extends Rule> rules = new ArrayList<>();\n" +
				"		Map<String, String> map = new HashMap<>();\n" +
				"		for (Rule rule : rules) {\n" +
				"			map.put(rule.matcher, rule.sampler);\n" +
				"		}";
		String expected = "" +
				"		class R {\n" +
				"			final String matcher = \"matcher\";\n" +
				"			final String sampler = \"sampler\";\n" +
				"		}\n" +
				"		class Rule extends R {}\n" +
				"		\n" +
				"		List<? extends Rule> rules = new ArrayList<>();\n" +
				"		Map<String, String> map = new HashMap<>();\n" +
				"		rules.forEach((Rule rule) -> {\n" +
				"			map.put(rule.matcher, rule.sampler);\n" +
				"		});";
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addImport(java.util.Map.class.getName());
		fixture.addImport(java.util.HashMap.class.getName());

		assertChange(original, expected);
	}

}
