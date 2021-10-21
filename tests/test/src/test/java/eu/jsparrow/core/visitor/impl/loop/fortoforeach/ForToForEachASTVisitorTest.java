package eu.jsparrow.core.visitor.impl.loop.fortoforeach;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;

@SuppressWarnings("nls")
public class ForToForEachASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void beforeEach() throws Exception {
		setVisitor(new ForToForEachASTVisitor());
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
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

		String expected = "" +
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

		String expected = "" +
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

		String expected = "" +
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

	@Test
	public void visit_updatingCollectionInsideLoop_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> list = new ArrayList<>();\n" +
				"		list.add(\"value\");\n" +
				"		for(int i = 0; i<list.size(); i++) {\n" +
				"			String value = list.get(i);\n" +
				"			if(list.size() < 5 && value.contains(\"0\")) {\n" +
				"				list.add(\"0\");\n" +
				"			}\n" +
				"		}");
	}

	@Test
	public void visit_reassigningCollection_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());

		assertNoChange("" +
				"		List<String> list = new ArrayList<>();\n" +
				"		list.add(\"value\");\n" +
				"		for(int i = 0; i<list.size(); i++) {\n" +
				"			String value = list.get(i);\n" +
				"			if(list.size() < 5 && value.contains(\"0\")) {\n" +
				"				list = new ArrayList<>();\n" +
				"			}\n" +
				"		}");
	}

	@Test
	public void visit_passingCollectionAsParameter_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> list = new ArrayList<>();\n" +
				"		for (int i =0; i<list.size(); i++) {\n" +
				"			String value = list.get(i);\n" +
				"			if(value.contains(\"0\")) {\n" +
				"				List<String> newList = new ArrayList<>();\n" +
				"				newList.addAll(list);\n" +
				"			}\n" +
				"		}");
	}

	@Test
	public void visit_passingCollectionAsConstructorParameter_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> list = new ArrayList<>();\n" +
				"		for (int i =0; i<list.size(); i++) {\n" +
				"			String value = list.get(i);\n" +
				"			if(value.contains(\"0\")) {\n" +
				"				List<String> newList = new ArrayList<>(list);\n" +
				"			}\n" +
				"		}");
	}

	@Test
	public void visit_reassigningArrays_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		String[] list = new String[10];\n" +
				"		for (int i =0; i<list.length; i++) {\n" +
				"			String value = list[i];\n" +
				"			if(value.contains(\"1\")) {\n" +
				"				list = new String[3];\n" +
				"			}\n" +
				"		}");
	}

	@Test
	public void visit_assigningIterableToOtherIterable_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> list = new ArrayList<>();\n" +
				"		for (int i =0; i<list.size(); i++) {\n" +
				"			String value = list.get(i);\n" +
				"			if(value.contains(\"0\")) {\n" +
				"				List<String> newList = new ArrayList<>();\n" +
				"				newList = list;\n" +
				"			}\n" +
				"		}");
	}

	@Test
	public void visit_iterableAsVariableInitializer_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> list = new ArrayList<>();\n" +
				"		for (int i =0; i<list.size(); i++) {\n" +
				"			String value = list.get(i);\n" +
				"			if(value.contains(\"0\")) {\n" +
				"				List<String> newList = list;\n" +
				"			}\n" +
				"		}");
	}

	@Test
	public void visit_discardedIteratingIndex_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		List<String> list = new ArrayList<>();\n" +
				"		for (int i =0; i<list.size(); i++) {\n" +
				"			String value = list.get(0);\n" +
				"			if(value.contains(\"0\")) {\n" +
				"				System.out.println(value);\n" +
				"			}\n" +
				"		}");
	}
}
