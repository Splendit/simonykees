package eu.jsparrow.core.visitor.unused;

import static eu.jsparrow.core.rule.impl.unused.Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
class RemoveUnusedLocalVariabesASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put(REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		setVisitor(new RemoveUnusedLocalVariabesASTVisitor(options));
	}

	public static Stream<Arguments> notAllFragmentsUsed() throws Exception {
		return Stream.of(
				Arguments.of("x", "int x = 0;"),
				Arguments.of("y", "int y = 0;"),
				Arguments.of("z", "int z = 0;"),
				Arguments.of("x+y", "int x=0, y=0;"),
				Arguments.of("x+z", "int x=0, z=0;"),
				Arguments.of("y+z", "int y=0, z=0;"));
	}

	@ParameterizedTest
	@MethodSource("notAllFragmentsUsed")
	void visit_NotAllFragmentsUsed_shouldTransform(String expressionUsingVariables, String expectedDeclarationStatement)
			throws Exception {

		String original = String.format(
				"" +
						"		int x = 0, y = 0, z = 0;\n" +
						"		System.out.println(%s);",
				expressionUsingVariables);

		String expected = String.format(
				"" +
						"		%s\n" +
						"		System.out.println(%s);",
				expectedDeclarationStatement, expressionUsingVariables);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"int x;",
			"int x, y;",
			"int x, y, z;",
	})
	void visit_AllFragmentsUnused_shouldTransform(String variableDeclarationStatement) throws Exception {

		String original = "{" + variableDeclarationStatement + "}";
		String expected = "{}";

		assertChange(original, expected);
	}

	/**
	 * This test is expected to fail as soon as lambdas without side effect are
	 * tolerated.
	 * 
	 */
	@Test
	void visit_NotSupportedInitializer_shouldNotTransform() throws Exception {
		String original = "Runnable r = () -> {};";
		assertNoChange(original);

	}
}