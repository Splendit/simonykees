package eu.jsparrow.core.visitor.impl.inline;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings({ "nls" })
public class InlineLocalVariablesASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new InlineLocalVariablesASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_usedForReturn_shouldTransform() throws Exception {
		String original = "" +
				"	int useForReturn() {\n" +
				"		int x = 1;\n" +
				"		return x;\n" +
				"	}";
		String expected = "" +
				"	int useForReturn() {\n" +
				"		return 1;\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	void visit_usedAsInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"	void useAsInitializer() {\n" +
				"		int x = 1;\n" +
				"		int x2 = x;\n" +
				"	}";
		assertNoChange(original);
	}
}
