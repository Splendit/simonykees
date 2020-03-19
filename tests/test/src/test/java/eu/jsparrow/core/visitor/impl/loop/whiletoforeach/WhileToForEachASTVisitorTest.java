package eu.jsparrow.core.visitor.impl.loop.whiletoforeach;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.loop.whiletoforeach.WhileToForEachASTVisitor;

@SuppressWarnings("nls")
public class WhileToForEachASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void beforeEach() {
		visitor = new WhileToForEachASTVisitor();
	}
	
	@Test
	public void visit_whileLoopUpdatingIterator_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		
		assertNoChange("" +
				"		List<String> list = new ArrayList<>();\n" + 
				"		int i = 0;\n" + 
				"		while(i < list.size()) {\n" + 
				"			String value = list.get(i);\n" + 
				"			if(value.contains(\"0\")) {\n" + 
				"				list.add(\"-\");\n" + 
				"			}\n" + 
				"			System.out.println(value);\n" + 
				"			i++;\n" + 
				"		}");
	}
	
	@Test
	public void visit_reassigningIterator_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		
		assertNoChange("" +
				"		List<String> list = new ArrayList<>();\n" + 
				"		int i = 0;\n" + 
				"		while(i < list.size()) {\n" + 
				"			String value = list.get(i);\n" + 
				"			if(value.contains(\"0\")) {\n" + 
				"				list = new ArrayList<>();\n" + 
				"			}\n" + 
				"			System.out.println(value);\n" + 
				"			i++;\n" + 
				"		}");
	}
}
