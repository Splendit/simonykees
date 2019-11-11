package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class UseListSortASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	private UseListSortASTVisitor visitor;
	
	@BeforeEach 
	public void setUp() throws Exception {
		visitor = new UseListSortASTVisitor();
		fixture.addImport(java.util.Collections.class.getName());
		fixture.addImport(java.util.Comparator.class.getName());
		fixture.addImport(java.util.List.class.getName());
	}
	
	@Test
	public void visit_baseCase_shouldTransform() throws Exception {
		String original = "" + 
				"	List<String> strings = Collections.emptyList();\n" + 
				"	Comparator<String> comparator = (String string1, String string2) -> string1.compareTo(string2);\n" + 
				"	Collections.sort(strings, comparator);";
		String expected = "" + 
				"	List<String> strings = Collections.emptyList();\n" + 
				"	Comparator<String> comparator = (String string1, String string2) -> string1.compareTo(string2);\n" + 
				"	strings.sort(comparator);";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	
	@Test
	public void visit_staticImportToCollectionsSort_shouldTransform() throws Exception {
		fixture.addImport(java.util.Collections.class.getName() + ".sort", true, false);
		String original = "" + 
				"	List<String> strings = Collections.emptyList();\n" + 
				"	Comparator<String> comparator = (String string1, String string2) -> string1.compareTo(string2);\n" + 
				"	sort(strings, comparator);";
		String expected = "" + 
				"	List<String> strings = Collections.emptyList();\n" + 
				"	Comparator<String> comparator = (String string1, String string2) -> string1.compareTo(string2);\n" + 
				"	strings.sort(comparator);";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	
	@Test
	public void visit_noComparator_shouldNotTransform() throws Exception {
		String original = "" + 
				"	List<String> strings = Collections.emptyList();\n" + 
				"	Collections.sort(strings);";
		fixture.addImport(java.util.Collections.class.getName() + "." + "sort");
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_baseCaseWithComparatorDeclaredInMethodArgument_shouldTransform() throws Exception {
		String original = "" + 
				"	List<String> strings = Collections.emptyList();\n" + 
				"	Collections.sort(strings, (String string1, String string2) -> string1.compareTo(string2));";
		String expected = "" + 
				"	List<String> strings = Collections.emptyList();\n" + 
				"	strings.sort((String string1, String string2) -> string1.compareTo(string2));";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	
	@Test
	public void visit_baseCaseWithAnonymousClassInArgument_shouldTransform() throws Exception {
		String original = "" + 
				"	List<String> strings = Collections.emptyList();\n" + 
				"	Collections.sort(strings, new Comparator<String>() {\n" + 
				"		@Override\n" + 
				"		public int compare(String o1, String o2) {\n" + 
				"			return o1.compareTo(o2);\n" + 
				"		}\n" + 
				"	});";
		String expected = "" + 
				"	List<String> strings = Collections.emptyList();\n" + 
				"	strings.sort(new Comparator<String>() {\n" + 
				"		@Override\n" + 
				"		public int compare(String o1, String o2) {\n" + 
				"			return o1.compareTo(o2);\n" + 
				"		}\n" + 
				"	});";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		
		fixture.accept(visitor);
		
		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}

}
