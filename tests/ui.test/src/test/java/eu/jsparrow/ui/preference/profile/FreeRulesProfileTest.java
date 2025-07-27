package eu.jsparrow.ui.preference.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FreeRulesProfileTest {

	private FreeRulesProfile freeRulesProfile;

	@BeforeEach
	public void setUp() {
		freeRulesProfile = new FreeRulesProfile();
	}

	@Test
	void freeRuleIds_shouldReturn20FreeRules() {
		List<String> ruleIds = freeRulesProfile.getEnabledRuleIds();
		assertEquals(20, ruleIds.size());
		assertEquals(Arrays.asList(
				"TryWithResource",
				"OverrideAnnotation",
				"MultiVariableDeclarationLine",
				"EnumsWithoutEquals",
				"RemoveDoubleNegation",
				"OptionalFilter",
				"RemoveNullCheckBeforeInstanceof",
				"CollapseIfStatements",
				"RemoveEmptyStatement",
				"RemoveUnnecessaryThrows",
				"UseSecureRandom",
				"InefficientConstructor",
				"PrimitiveBoxedForString",
				"RemoveToStringOnString",
				"UseOffsetBasedStringMethods",
				"StringLiteralEqualityCheck",
				"UseIsEmptyOnCollections",
				"ForToForEach",
				"LambdaToMethodReference",
				"OrganizeImports"), ruleIds);
	}

	@Test
	void profileName_shouldReturnFreeProfile() {
		String name = freeRulesProfile.getProfileName();
		assertEquals("Free Rules", name, () -> "Incorrect profile name");
	}

	@Test
	void isBuiltIn_shouldReturnTrue() {
		boolean isBuiltIn = freeRulesProfile.isBuiltInProfile();
		assertTrue(isBuiltIn, () -> "The 'Free Rules' profile should be built-in");
	}

	@Test
	void containsRule_shouldReturnTrue() {
		boolean containsCollapseIfStatements = freeRulesProfile.containsRule("CodeFormatter");
		assertFalse(containsCollapseIfStatements, () -> "Code formatter should not be a Free Rule");
	}

	@Test
	void containsRule_shouldReturnFalse() {
		boolean containsCollapseIfStatements = freeRulesProfile.containsRule("CollapseIfStatements");
		assertTrue(containsCollapseIfStatements, () -> "Collapse If Statements should be a Free Rule");
	}

}
