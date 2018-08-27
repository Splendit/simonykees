package eu.jsparrow.standalone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLLoggerRule;
import eu.jsparrow.core.config.YAMLProfile;
import eu.jsparrow.core.config.YAMLRenamingRule;
import eu.jsparrow.core.rule.impl.CodeFormatterRule;
import eu.jsparrow.core.rule.impl.logger.LogLevelEnum;
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

		when(selectedProfile.getName()).thenReturn(selectedProfileName);
		when(config.getProfiles()).thenReturn(Collections.singletonList(selectedProfile));
	}

	@Test
	public void computeConfiguration_selectedProfile_shouldComputeConfigFromSelectedProfile() throws Exception {
		when(config.getSelectedProfile()).thenReturn(selectedProfileName);

		new RuleConfigurationWrapper(config, refactoringRules);

		verify(selectedProfile, times(1)).getLoggerRule();
		verify(selectedProfile, times(1)).getRenamingRule();
	}

	@Test
	public void computeConfiguration_noSelectedProfile_shouldComputeConfigFromRoot() throws Exception {

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
		when(config.getSelectedProfile()).thenReturn(selectedProfileName);
		when(selectedProfile.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		List<RefactoringRule> rules = ruleConfigurationWrapper.getSelectedAutomaticRules();

		assertThat(rules, hasSize(1));
		assertThat(rules, contains(refactoringRule));
	}

	@Test
	public void getSelectedAutomaticRules_noSelectedProfile_shouldReturnRulesRoot() throws Exception {
		when(config.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		List<RefactoringRule> rules = ruleConfigurationWrapper.getSelectedAutomaticRules();

		assertThat(rules, hasSize(1));
		assertThat(rules, contains(refactoringRule));
	}

	@Test
	public void isSelectedRule_selectedProfile_shouldReturnTrue() throws Exception {
		when(config.getSelectedProfile()).thenReturn(selectedProfileName);
		when(selectedProfile.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		boolean selectedRefactoring = ruleConfigurationWrapper.isSelectedRule(refactoringRule.getId());

		assertTrue(selectedRefactoring);
	}

	@Test
	public void isSelectedRule_selectedProfile_shouldReturnFalse() throws Exception {
		when(config.getSelectedProfile()).thenReturn(selectedProfileName);
		when(selectedProfile.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		boolean selectedRefactoring = ruleConfigurationWrapper.isSelectedRule("some-other-rule-id");

		assertFalse(selectedRefactoring);
	}

	@Test
	public void isSelectedRule_noSelectedProfile_shouldReturnTrue() throws Exception {
		when(config.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		boolean selectedRefactoring = ruleConfigurationWrapper.isSelectedRule(refactoringRule.getId());

		assertTrue(selectedRefactoring);
	}

	@Test
	public void isSelectedRule_noSelectedProfile_shouldReturnFalse() throws Exception {
		when(config.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		boolean selectedRefactoring = ruleConfigurationWrapper.isSelectedRule("some-other-rule");

		assertFalse(selectedRefactoring);
	}

	@Test
	public void getLoggerConfiguration_emptyConfiguration_shouldReturnEmptyMap() throws Exception {
		YAMLLoggerRule loggerRuleConfiguration = new YAMLLoggerRule();
		when(config.getLoggerRule()).thenReturn(loggerRuleConfiguration);

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		Map<String, String> loggerConfigurationOptions = ruleConfigurationWrapper.getLoggerRuleConfigurationOptions();

		assertTrue(loggerConfigurationOptions.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getLoggerConfiguration_defaultConfiguration_shouldDefaultOptions() throws Exception {
		YAMLLoggerRule loggerRuleConfiguration = new YAMLLoggerRule(LogLevelEnum.INFO, LogLevelEnum.ERROR,
				LogLevelEnum.ERROR, LogLevelEnum.INFO, LogLevelEnum.ERROR, LogLevelEnum.ERROR, true);
		when(config.getLoggerRule()).thenReturn(loggerRuleConfiguration);

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		Map<String, String> loggerConfigurationOptions = ruleConfigurationWrapper.getLoggerRuleConfigurationOptions();

		assertThat(loggerConfigurationOptions,
				allOf(hasEntry("system-out-print", "info"), hasEntry("system-err-print", "error"),
						hasEntry("print-stacktrace", "error"), hasEntry("system-out-print-exception", "info"),
						hasEntry("system-err-print-exception", "error"), hasEntry("new-logging-statement", "error"),
						hasEntry("attach-exception-object", "true")));

	}

	@Test
	public void getFieldRenamingOptions_emptyConfiguration_shouldReturnEmptyMap() throws Exception {
		YAMLRenamingRule renamingConfiguration = new YAMLRenamingRule();
		when(config.getRenamingRule()).thenReturn(renamingConfiguration);

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		Map<String, Boolean> renamingConfigurationOptions = ruleConfigurationWrapper.getFieldRenamingRuleConfigurationOptions();

		assertThat(renamingConfigurationOptions,
				allOf(hasEntry("public", true), hasEntry("package-private", true), hasEntry("protected", true),
						hasEntry("private", true), hasEntry("add-todo", false),
						hasEntry("uppercase-after-underscore", true)));
	}
}
