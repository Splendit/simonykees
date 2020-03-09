package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

public class UseStringJoinASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	private UseStringJoinASTVisitor visitor;
	
	@BeforeEach
	public void setUp() {
		visitor = new UseStringJoinASTVisitor();
	}
	
	@Test
	public void visit_baseCase_shouldTransform() throws Exception {
		List<String> values = new ArrayList<>();
		values.stream().collect(Collectors.joining(","));
		Collectors.joining("", "pre", "suffix");
		String.join(",", values);
		
		String original = "" +
				"		List<String> values = new ArrayList<>();\n" + 
				"		values.stream().collect(Collectors.joining(\",\"));";
		String expected = "" +
				"		List<String> values = new ArrayList<>();\n" + 
				"		String.join(\",\", values);";
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.ArrayList.class.getName());
		fixture.addImport(java.util.stream.Collectors.class.getName());
		
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(expected), fixture.getMethodBlock());
	}

}
