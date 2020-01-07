package eu.jsparrow.core.visitor.impl.optional;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.optional.OptionalFilterASTVisitor;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class OptionalFilterASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private OptionalFilterASTVisitor visitor;
	
	@BeforeEach
	public void beforeEach() throws Exception {
		visitor = new OptionalFilterASTVisitor();
		fixture.addImport("java.util.Optional");
	}
	
	
	@Test
	public void visit_baseCase_shouldTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.empty();\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			if(!value.isEmpty()) {\n" + 
				"				System.out.println(value);\n" + 
				"			}\n" + 
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		optional.filter(value -> !value.isEmpty()).ifPresent(value -> {\n" + 
				"			System.out.println(value);\n" + 
				"		});";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(expected), fixture.getMethodBlock());
	}
	
	//TODO: Using explicit types
	// 
	
	/*
	 * Negative Test Cases
	 */
	
	@Test
	public void visit_multipleBodyStatements_shouldNotTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			if(!value.isEmpty()) {\n" + 
				"				System.out.println(value);\n" + 
				"				String test = value.replace(\"t\", \"o\");\n" + 
				"			}\n" + 
				"			System.out.print(value);\n" + 
				"		});";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingIfStatement_shouldNotTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			String test = value.replace(\"t\", \"o\");\n" + 
				"			System.out.print(test);\n" + 
				"			System.out.print(value);\n" + 
				"		});";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_unrelatedIfCondition_shouldNotTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.empty();\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			if (!optional.isEmpty()) {\n" + 
				"				System.out.println(value);\n" + 
				"			}\n" + 
				"		});";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_emptyIfStatement_shouldNotTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.empty();\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			if (!optional.isEmpty()) {\n" + 
				"\n" + 
				"			}\n" + 
				"		});";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_ifThenElse_shouldNotTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.empty();\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			if (!value.isEmpty()) {\n" + 
				"				System.out.println(value);\n" + 
				"			} else {\n" + 
				"				\n" + 
				"			}\n" + 
				"		});";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(original), fixture.getMethodBlock());
	}

}
