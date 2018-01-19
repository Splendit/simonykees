package eu.jsparrow.standalone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.sql.Ref;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * test class for {@link ListRulesUtil}
 * 
 * @author Matthias Webhofer
 * @since 2.5.0
 */
public class ListRulesUtilTest {

	private ListRulesUtil listRulesUtil;
	
	private RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule1;
	private RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule2;
	private RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule3;
	
	private List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		rule1 = mock(RefactoringRule.class);
		rule2 = mock(RefactoringRule.class);
		rule3 = mock(RefactoringRule.class);
		
		rules = new LinkedList<>();
		rules.add(rule1);
		rules.add(rule2);
		rules.add(rule3);

		listRulesUtil = new TestableListRulesUtil();
	}

	@Test
	public void listRules_noRulesSelected_shouldReturnNoRulesAvailable() {
		String rule1id = "rule1"; //$NON-NLS-1$
		String rule2id = "rule2"; //$NON-NLS-1$
		String rule3id = "rule3"; //$NON-NLS-1$

		String ruleId = "nonExistingRule"; //$NON-NLS-1$
		
		String expectedResult = "No rules available!"; //$NON-NLS-1$

		when(rule1.getId()).thenReturn(rule1id);
		when(rule2.getId()).thenReturn(rule2id);
		when(rule3.getId()).thenReturn(rule3id);

		String output = listRulesUtil.listRules(ruleId);
		
		assertThat(output, containsString(expectedResult));
	}
	
	@Test
	public void listRules_allRulesSelected_shouldReturnRulesDescription() {
		String rule1id = "rule1"; //$NON-NLS-1$
		String rule1Name = "Rule 1"; //$NON-NLS-1$
		String rule1Description = "Rule 1 Description"; //$NON-NLS-1$
		
		String rule2id = "rule2"; //$NON-NLS-1$
		String rule2Name = "Rule 2"; //$NON-NLS-1$
		String rule2Description = "Rule 2 Description"; //$NON-NLS-1$
		
		String rule3id = "rule3"; //$NON-NLS-1$
		String rule3Name = "Rule 3"; //$NON-NLS-1$
		String rule3Description = "Rule 3 Description"; //$NON-NLS-1$
		
		RuleDescription ruleDescription1 = mock(RuleDescription.class);
		RuleDescription ruleDescription2 = mock(RuleDescription.class);
		RuleDescription ruleDescription3 = mock(RuleDescription.class);
		
		when(rule1.getId()).thenReturn(rule1id);
		when(rule1.getRuleDescription()).thenReturn(ruleDescription1);
		when(ruleDescription1.getName()).thenReturn(rule1Name);
		when(ruleDescription1.getDescription()).thenReturn(rule1Description);
		
		when(rule2.getId()).thenReturn(rule2id);
		when(rule2.getRuleDescription()).thenReturn(ruleDescription2);
		when(ruleDescription2.getName()).thenReturn(rule2Name);
		when(ruleDescription2.getDescription()).thenReturn(rule2Description);
		
		when(rule3.getId()).thenReturn(rule3id);
		when(rule3.getRuleDescription()).thenReturn(ruleDescription3);
		when(ruleDescription3.getName()).thenReturn(rule3Name);
		when(ruleDescription3.getDescription()).thenReturn(rule3Description);

		String output = listRulesUtil.listRules();
		
		assertThat(output, containsString(rule1id));
		assertThat(output, containsString(rule1Name));
		assertThat(output, containsString(rule1Description));
		
		assertThat(output, containsString(rule2id));
		assertThat(output, containsString(rule2Name));
		assertThat(output, containsString(rule2Description));
		
		assertThat(output, containsString(rule3id));
		assertThat(output, containsString(rule3Name));
		assertThat(output, containsString(rule3Description));
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
		
		String rule2id = "rule2"; //$NON-NLS-1$
		String rule2Name = "Rule 2"; //$NON-NLS-1$
		
		String rule3id = "rule3"; //$NON-NLS-1$
		String rule3Name = "Rule 3"; //$NON-NLS-1$
		
		RuleDescription ruleDescription1 = mock(RuleDescription.class);
		RuleDescription ruleDescription2 = mock(RuleDescription.class);
		RuleDescription ruleDescription3 = mock(RuleDescription.class);
		
		when(rule1.getId()).thenReturn(rule1id);
		when(rule1.getRuleDescription()).thenReturn(ruleDescription1);
		when(ruleDescription1.getName()).thenReturn(rule1Name);
		
		when(rule2.getId()).thenReturn(rule2id);
		when(rule2.getRuleDescription()).thenReturn(ruleDescription2);
		when(ruleDescription2.getName()).thenReturn(rule2Name);
		
		when(rule3.getId()).thenReturn(rule3id);
		when(rule3.getRuleDescription()).thenReturn(ruleDescription3);
		when(ruleDescription3.getName()).thenReturn(rule3Name);
		
		String output = listRulesUtil.listRulesShort();
		
		assertThat(output, containsString(rule1id));
		assertThat(output, containsString(rule1Name));
		
		assertThat(output, containsString(rule2id));
		assertThat(output, containsString(rule2Name));
		
		assertThat(output, containsString(rule3id));
		assertThat(output, containsString(rule3Name));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void getAllRulesFilteredById_noRuleIdProvided_shouldReturnAllRules() {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = listRulesUtil
			.getAllRulesFilteredById(null);

		assertThat(rules, contains(rule1, rule2, rule3));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getAllRulesFilteredById_ruleIdProvided_shouldReturnFilteredRules() {
		String rule1id = "rule1"; //$NON-NLS-1$
		String rule2id = "rule2"; //$NON-NLS-1$
		String rule3id = "rule3"; //$NON-NLS-1$

		String ruleId = rule1id + ",," + rule2id; //$NON-NLS-1$

		when(rule1.getId()).thenReturn(rule1id);
		when(rule2.getId()).thenReturn(rule2id);
		when(rule3.getId()).thenReturn(rule3id);

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = listRulesUtil
			.getAllRulesFilteredById(ruleId);

		assertThat(rules, contains(rule1, rule2));
	}

	@Test
	public void getMaxWordLength() {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = listRulesUtil.getAllRulesFromContainer();

		String rule1id = "rule1"; //$NON-NLS-1$
		String rule2id = "rule22"; //$NON-NLS-1$
		String rule3id = "rule333"; //$NON-NLS-1$

		when(rule1.getId()).thenReturn(rule1id);
		when(rule2.getId()).thenReturn(rule2id);
		when(rule3.getId()).thenReturn(rule3id);

		Optional<Integer> maxWordLength = listRulesUtil.getMaxWordLength(rules);

		assertTrue(maxWordLength.isPresent());
		assertTrue(maxWordLength.get() == rule3id.length());
	}

	@Test
	public void calculateWhitespace() {
		String whitespace = listRulesUtil.calculateWhitespace(5, 10);

		assertTrue(whitespace.length() == 6);
		assertThat(whitespace, equalToIgnoringWhiteSpace("")); //$NON-NLS-1$
	}

	class TestableListRulesUtil extends ListRulesUtil {
		@Override
		protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getAllRulesFromContainer() {
			return rules;
		}
	}
}
