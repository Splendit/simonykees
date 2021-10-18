package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class UseCollectionsSingletonListASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new UseCollectionsSingletonListASTVisitor());
		fixture.addImport(java.util.Arrays.class.getName());
	}

	@Test
	public void visit_asListZeroArguments_shouldTransform() throws Exception {
		String original = "List<String> strings = Arrays.asList();";
		String expected = "List<String> strings = Collections.emptyList();";

		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Collections.class.getName());

		assertChange(original, expected);
	}

	@Test
	public void visit_asListOneArgument_shouldTransform() throws Exception {
		String original = "List<String> strings = Arrays.asList(\"value\");";
		String expected = "List<String> strings = Collections.singletonList(\"value\");";

		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Collections.class.getName());

		assertChange(original, expected);
	}

	@Test
	public void visit_asListStaticImport_shouldTransform() throws Exception {
		String original = "List<String> strings = asList(\"value\");";
		String expected = "List<String> strings = Collections.singletonList(\"value\");";

		fixture.addImport(java.util.Arrays.class.getName() + ".asList", true, false);
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Collections.class.getName());

		assertChange(original, expected);
	}

	@Test
	public void visit_asListEmptyListStaticImport_shouldTransform() throws Exception {
		String original = "List<String> strings = asList();";
		String expected = "List<String> strings = Collections.emptyList();";

		fixture.addImport(java.util.Arrays.class.getName() + ".asList", true, false);
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.Collections.class.getName());

		assertChange(original, expected);
	}

	@Test
	public void visit_asListMoreThanOneArguments_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.List.class.getName());

		assertNoChange("List<String> strings = Arrays.asList(\"value1\", \"value2\");");
	}

	@Test
	public void visit_usingArrayAsArgument_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.List.class.getName());

		assertNoChange("String[]array = {}; List<String> strings = Arrays.asList(array);");
	}
}
