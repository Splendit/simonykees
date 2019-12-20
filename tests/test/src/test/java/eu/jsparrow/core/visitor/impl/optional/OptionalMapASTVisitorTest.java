package eu.jsparrow.core.visitor.impl.optional;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import java.util.Optional;

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
		Optional<String> optional = Optional.of("value");
		optional
			.map(value -> value.replace("t", "o"))
			.ifPresent(test -> System.out.print(test));
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

}
