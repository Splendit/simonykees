package eu.jsparrow.core.visitor.unused;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

class UnusedFieldAndLabelOfSameNameVisitorTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		x: while (true)\n" +
					"			break x;",
			"" +
					"		while (true) {\n" +
					"			x: while (true)\n" +
					"				continue x;\n" +
					"		}"
	})
	void testLabelXAndFieldX_shouldBeRemoved(String statementWithLabelX) throws Exception {
		Map<String, Boolean> options = new HashMap<>();
		options.put("private-fields", true);
		options.put("remove-initializers-side-effects", false);
		UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(options);

		String originalCode = "" +
				"	private int x;\n" +
				"	void labelXAndFieldX() {\n" +
				statementWithLabelX +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, originalCode);
		defaultFixture.accept(visitor);

		List<UnusedFieldWrapper> removedUnusedFields = visitor.getUnusedPrivateFields();
		assertEquals(1, removedUnusedFields.size());
		UnusedFieldWrapper unusedFieldWrapper = removedUnusedFields.get(0);
		String removedUnusedFieldName = unusedFieldWrapper.getClassMemberIdentifier();
		assertEquals("x", removedUnusedFieldName);
	}
}
