package eu.jsparrow.core.visitor.impl.optional;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.optional.OptionalMapASTVisitor;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class OptionalMapASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	private OptionalMapASTVisitor visitor;
	
	@BeforeEach
	public void beforeEach() throws Exception {
		visitor = new OptionalMapASTVisitor();
		fixture.addImport("java.util.Optional");
	}
	
	@Test
	public void test_baseCase_shouldTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			String test = value.replace(\"t\", \"o\");\n" + 
				"			System.out.print(test);\n" + 
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional\n" + 
				"			.map(value -> value.replace(\"t\", \"o\"))\n" + 
				"			.ifPresent(test -> System.out.print(test));";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void test_multipleRemainingStatements_shouldTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			String test = value.replace(\"t\", \"o\");\n" + 
				"			System.out.print(test);\n" + 
				"			System.out.print(test);\n" + 
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.map(value -> value.replace(\"t\", \"o\")).ifPresent(test -> {\n" + 
				"			System.out.print(test);\n" + 
				"			System.out.print(test);\n" + 
				"		});";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void test_multipleExtractedStatements_shouldTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			System.out.print(value);\n" + 
				"			String test = value.replace(\"t\", \"o\");\n" + 
				"			System.out.print(test);\n" + 
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.map(value -> {\n" + 
				"			System.out.print(value);\n" + 
				"			return value.replace(\"t\", \"o\");\n" + 
				"		}).ifPresent(test -> System.out.print(test));";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void test_primitiveTypes_shouldTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			int length = value.length();\n" + 
				"			if(length > 0) {\n" + 
				"				System.out.println(\"Length is \" + length);\n" + 
				"			}\n" + 
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.map(value -> value.length()).ifPresent(length -> {\n" + 
				"			if(length > 0) {\n" + 
				"				System.out.println(\"Length is \" + length);\n" + 
				"			}\n" + 
				"		});";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void test_usingParameterType_shouldTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.ifPresent((String value) -> {\n" + 
				"			String test = value.replace(\"t\", \"o\"); \n" + 
				"			System.out.print(test);\n" + 
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.map((String value) -> value.replace(\"t\", \"o\")).ifPresent((String test) -> System.out.print(test));";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(expected), fixture.getMethodBlock());
	}
	
	// Negative test cases 
	
	@Test
	public void test_nonExtractableBody_shouldNotTransform() throws Exception {
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
	public void test_singleStatementLambdaBlock_shouldNotTransform() throws Exception {
		String original = "" + 
				"		Optional<String> optional = Optional.of(\"value\");\n" + 
				"		optional.ifPresent(value -> {\n" + 
				"			if(value.length() > 1) {				\n" + 
				"				System.out.print(value);\n" + 
				"			}\n" + 
				"		});";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createBlockFromString(original), fixture.getMethodBlock());
	}
	

}
