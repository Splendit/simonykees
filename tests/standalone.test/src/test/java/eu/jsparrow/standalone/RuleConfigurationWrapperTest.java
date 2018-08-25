package eu.jsparrow.standalone;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.*;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLProfile;
import eu.jsparrow.core.rule.impl.CodeFormatterRule;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.standalone.exceptions.StandaloneException;

@SuppressWarnings("nls")
public class RuleConfigurationWrapperTest {

	private YAMLConfig config;
	private List<RefactoringRule> refactoringRules;
	private YAMLProfile selectedProfile;

	private String selectedProfileName = "selectedProfileName";
	private RefactoringRule refactoringRule = new CodeFormatterRule();
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		config = mock(YAMLConfig.class);
		selectedProfile = mock(YAMLProfile.class);
		refactoringRules = Collections.singletonList(refactoringRule);
		
		when(config.getSelectedProfile()).thenReturn(selectedProfileName);
		when(selectedProfile.getName()).thenReturn(selectedProfileName);
		when(config.getProfiles()).thenReturn(Collections.singletonList(selectedProfile));

		
	}

	@Test
	public void computeConfiguration_selectedProfile_shouldComputeConfigFromSelectedProfile() throws Exception {


		new RuleConfigurationWrapper(config, refactoringRules);

		verify(selectedProfile, times(1)).getLoggerRule();
		verify(selectedProfile, times(1)).getRenamingRule();
	}
	
	@Test
	public void computeConfiguration_noSelectedProfile_shouldComputeConfigFromRoot() throws Exception {
		when(config.getSelectedProfile()).thenReturn("");

		new RuleConfigurationWrapper(config, refactoringRules);

		verify(config, times(1)).getLoggerRule();
		verify(config, times(1)).getRenamingRule();
	}
	
	@Test
	public void computeConfiguration_invalidSelectedProfile_shouldThrowException() throws Exception {
		String invalidSelectedProfileName = "invalidProfileName";
		when(config.getSelectedProfile()).thenReturn(invalidSelectedProfileName);
		when(config.getProfiles()).thenReturn(Collections.emptyList());

		expectedException.expect(StandaloneException.class);
		new RuleConfigurationWrapper(config, refactoringRules);

		verify(config, never()).getLoggerRule();
		verify(config, never()).getRenamingRule();
	}
	
	@Test
	public void getSelectedAutomaticRules_selectedProfile_shouldReturnRulesFromProfile() throws Exception {
		when(selectedProfile.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));
		
		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		List<RefactoringRule> rules = ruleConfigurationWrapper.getSelectedAutomaticRules();
		
		assertThat(rules, allOf(hasSize(1), contains(refactoringRule)));
	}
	
	@Test
	public void getSelectedAutomaticRules_noSelectedProfile_shouldReturnRulesRoot() throws Exception {
		when(config.getSelectedProfile()).thenReturn("");
		when(config.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));
	
		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		List<RefactoringRule> rules = ruleConfigurationWrapper.getSelectedAutomaticRules();
		
		assertThat(rules, allOf(hasSize(1), contains(refactoringRule)));
	}
	
	@Test
	public void isSelectedRule_selectedProfile_shouldReturnTrue() throws Exception {
		when(selectedProfile.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));
		
		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		boolean selectedRefactoring = ruleConfigurationWrapper.isSelectedRule(refactoringRule.getId());
		
		assertTrue(selectedRefactoring);
	}
	
	@Test
	public void isSelectedRule_selectedProfile_shouldReturnFalse() throws Exception {
		when(selectedProfile.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));
		
		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		boolean selectedRefactoring = ruleConfigurationWrapper.isSelectedRule("some-other-rule-id");
		
		assertFalse(selectedRefactoring);
	}
	
	@Test
	public void isSelectedRule_noSelectedProfile_shouldReturnTrue() throws Exception {
		when(config.getSelectedProfile()).thenReturn("");
		when(config.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));
		
		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		boolean selectedRefactoring = ruleConfigurationWrapper.isSelectedRule(refactoringRule.getId());
		
		assertTrue(selectedRefactoring);
	}
	
	@Test
	public void isSelectedRule_noSelectedProfile_shouldReturnFalse() throws Exception {
		when(config.getSelectedProfile()).thenReturn("");
		when(config.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));
		
		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		boolean selectedRefactoring = ruleConfigurationWrapper.isSelectedRule("some-other-rule");
		
		assertFalse(selectedRefactoring);
	}
}
