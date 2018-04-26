package eu.jsparrow.standalone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * test class for {@link ListRulesUtil}
 * 
 * @author Matthias Webhofer
 * @since 2.5.0
 */
public class ListRulesUtilTest {

	private ListRulesUtil listRulesUtil;

	private RefactoringRule rule1;

	private List<RefactoringRule> rules;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		rule1 = mock(RefactoringRuleImpl.class);

		rules = new LinkedList<>();
		rules.add(rule1);

		listRulesUtil = new TestableListRulesUtil();
	}

	@Test
	public void listRules_noRulesSelected_shouldReturnNoRulesAvailable() {
		String rule1id = "rule1"; //$NON-NLS-1$
		String ruleId = "nonExistingRule"; //$NON-NLS-1$
		String expectedResult = "No rules available!"; //$NON-NLS-1$

		when(rule1.getId()).thenReturn(rule1id);

		String output = listRulesUtil.listRules(ruleId);

		assertThat(output, containsString(expectedResult));
	}

	@Test
	public void listRules_allRulesSelected_shouldReturnRulesDescription() {
		String rule1id = "rule1"; //$NON-NLS-1$
		String rule1Name = "Rule 1"; //$NON-NLS-1$
		String rule1Description = "Rule 1 Description"; //$NON-NLS-1$

		RuleDescription ruleDescription1 = mock(RuleDescription.class);

		when(rule1.getId()).thenReturn(rule1id);
		when(rule1.getRuleDescription()).thenReturn(ruleDescription1);
		when(ruleDescription1.getName()).thenReturn(rule1Name);
		when(ruleDescription1.getDescription()).thenReturn(rule1Description);

		String output = listRulesUtil.listRules();

		assertThat(output, containsString(rule1id));
		assertThat(output, containsString(rule1Name));
		assertThat(output, containsString(rule1Description));
	}

	@Test
	public void listRulesShort_noRulesAvailable() {
		String expectedResult = "No rules available!"; //$NON-NLS-1$

		rules.clear();

		String output = listRulesUtil.listRulesShort();

		assertThat(output, containsString(expectedResult));
	}

	@Test
	public void listRulesShort_rulesAreAvailable() {
		String rule1id = "rule1"; //$NON-NLS-1$
		String rule1Name = "Rule 1"; //$NON-NLS-1$

		RuleDescription ruleDescription1 = mock(RuleDescription.class);

		when(rule1.getId()).thenReturn(rule1id);
		when(rule1.getRuleDescription()).thenReturn(ruleDescription1);
		when(ruleDescription1.getName()).thenReturn(rule1Name);

		String output = listRulesUtil.listRulesShort();

		assertThat(output, containsString(rule1id));
		assertThat(output, containsString(rule1Name));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getAllRulesFilteredById_noRuleIdProvided_shouldReturnAllRules() {
		List<RefactoringRule> rules = listRulesUtil
			.getAllRulesFilteredById(null);

		assertThat(rules, contains(rule1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getAllRulesFilteredById_ruleIdProvided_shouldReturnFilteredRules() {
		RefactoringRule rule2 = addExtraRule();

		String rule1id = "rule1"; //$NON-NLS-1$
		String rule2id = "rule2"; //$NON-NLS-1$

		String ruleId = rule1id + ",,"; //$NON-NLS-1$

		when(rule1.getId()).thenReturn(rule1id);
		when(rule2.getId()).thenReturn(rule2id);

		List<RefactoringRule> rules = listRulesUtil
			.getAllRulesFilteredById(ruleId);

		assertThat(rules, contains(rule1));
	}

	@Test
	public void getMaxWordLength() {
		RefactoringRule rule2 = addExtraRule();

		List<RefactoringRule> rules = listRulesUtil.getAllRulesFromContainer();

		String rule1id = "rule1"; //$NON-NLS-1$
		String rule2id = "rule22"; //$NON-NLS-1$

		when(rule1.getId()).thenReturn(rule1id);
		when(rule2.getId()).thenReturn(rule2id);

		Optional<Integer> maxWordLength = listRulesUtil.getMaxWordLength(rules);

		assertTrue(maxWordLength.isPresent());
		assertTrue(maxWordLength.get() == rule2id.length());
	}

	@Test
	public void calculateWhitespace() {
		String whitespace = listRulesUtil.calculateWhitespace(5, 10);

		assertTrue(whitespace.length() == 6);
		assertThat(whitespace, equalToIgnoringWhiteSpace("")); //$NON-NLS-1$
	}

	private RefactoringRule addExtraRule() {
		RefactoringRule rule = mock(RefactoringRuleImpl.class);

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
