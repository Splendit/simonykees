package eu.jsparrow.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * test class for {@link ListRulesUtil}
 * 
 * @author Matthias Webhofer
 * @since 2.5.0
 */
public class ListRulesUtilTest {

	private static final String RULE1_NAME = "rule1"; //$NON-NLS-1$

	private ListRulesUtil listRulesUtil;

	private RefactoringRule rule1;

	private List<RefactoringRule> rules;

	@BeforeEach
	public void setUp() {
		rule1 = mock(RefactoringRule.class);

		rules = new LinkedList<>();
		rules.add(rule1);

		listRulesUtil = new TestableListRulesUtil();
	}

	@Test
	public void listRules_noRulesSelected_shouldReturnNoRulesAvailable() {
		String rule1id = RULE1_NAME;
		String ruleId = "nonExistingRule"; //$NON-NLS-1$
		String expectedResult = "No rules available!"; //$NON-NLS-1$

		when(rule1.getId()).thenReturn(rule1id);

		String output = listRulesUtil.listRules(ruleId);

		assertTrue(output.contains(expectedResult));
	}

	@Test
	public void listRules_allRulesSelected_shouldReturnRulesDescription() {
		String rule1id = RULE1_NAME;
		String rule1Name = "Rule 1"; //$NON-NLS-1$
		String rule1Description = "Rule 1 Description"; //$NON-NLS-1$

		RuleDescription ruleDescription1 = mock(RuleDescription.class);

		when(rule1.getId()).thenReturn(rule1id);
		when(rule1.getRuleDescription()).thenReturn(ruleDescription1);
		when(ruleDescription1.getName()).thenReturn(rule1Name);
		when(ruleDescription1.getDescription()).thenReturn(rule1Description);

		String output = listRulesUtil.listRules();

		assertTrue(output.contains(rule1id));
		assertTrue(output.contains(rule1Name));
		assertTrue(output.contains(rule1Description));
	}

	@Test
	public void listRulesShort_noRulesAvailable() {
		String expectedResult = "No rules available!"; //$NON-NLS-1$

		rules.clear();

		String output = listRulesUtil.listRulesShort();

		assertTrue(output.contains(expectedResult));
	}

	@Test
	public void listRulesShort_rulesAreAvailable() {
		String rule1id = RULE1_NAME;
		String rule1Name = "Rule 1"; //$NON-NLS-1$

		RuleDescription ruleDescription1 = mock(RuleDescription.class);

		when(rule1.getId()).thenReturn(rule1id);
		when(rule1.getRuleDescription()).thenReturn(ruleDescription1);
		when(ruleDescription1.getName()).thenReturn(rule1Name);

		String output = listRulesUtil.listRulesShort();

		assertTrue(output.contains(rule1id));
		assertTrue(output.contains(rule1Name));
	}

	@Test
	public void getAllRulesFilteredById_noRuleIdProvided_shouldReturnAllRules() {
		List<RefactoringRule> testRules = listRulesUtil.getAllRulesFilteredById(null);
		assertEquals(Arrays.asList(rule1), testRules);
	}

	@Test
	public void getAllRulesFilteredById_ruleIdProvided_shouldReturnFilteredRules() {
		RefactoringRule rule2 = addExtraRule();

		String rule1id = RULE1_NAME;
		String rule2id = "rule2"; //$NON-NLS-1$

		String ruleId = rule1id + ",,"; //$NON-NLS-1$

		when(rule1.getId()).thenReturn(rule1id);
		when(rule2.getId()).thenReturn(rule2id);

		List<RefactoringRule> rules = listRulesUtil.getAllRulesFilteredById(ruleId);
		assertEquals(Arrays.asList(rule1), rules);
	}

	@Test
	public void getMaxWordLength() {
		RefactoringRule rule2 = addExtraRule();

		List<RefactoringRule> rules = listRulesUtil.getAllRulesFromContainer();

		String rule1id = RULE1_NAME;
		String rule2id = "rule22"; //$NON-NLS-1$

		when(rule1.getId()).thenReturn(rule1id);
		when(rule2.getId()).thenReturn(rule2id);

		Optional<Integer> maxWordLength = listRulesUtil.getMaxWordLength(rules);

		assertTrue(maxWordLength.isPresent());
		assertEquals(rule2id.length(), maxWordLength.get()
			.intValue());
	}

	@Test
	public void calculateWhitespace() {
		String whitespace = listRulesUtil.calculateWhitespace(5, 10);

		assertEquals(6, whitespace.length());
		assertTrue(whitespace.trim().isEmpty()); //$NON-NLS-1$
	}

	private RefactoringRule addExtraRule() {
		RefactoringRule rule = mock(RefactoringRule.class);

		rules.add(rule);

		return rule;
	}

	class TestableListRulesUtil extends ListRulesUtil {
		@Override
		protected List<RefactoringRule> getAllRulesFromContainer() {
			return rules;
		}
	}
}
