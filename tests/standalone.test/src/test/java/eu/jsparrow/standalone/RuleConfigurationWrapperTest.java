package eu.jsparrow.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLLoggerRule;
import eu.jsparrow.core.config.YAMLProfile;
import eu.jsparrow.core.config.YAMLRenamingRule;
import eu.jsparrow.core.rule.impl.CodeFormatterRule;
import eu.jsparrow.core.rule.impl.logger.LogLevelEnum;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.standalone.exceptions.StandaloneException;

public class RuleConfigurationWrapperTest {

	private static final String INFO = "info";
	private static final String ERROR = "error";
	private YAMLConfig config;
	private List<RefactoringRule> refactoringRules;
	private YAMLProfile selectedProfile;

	private String selectedProfileName = "selectedProfileName";
	private RefactoringRule refactoringRule = new CodeFormatterRule();

	@BeforeEach
	public void setUp() {
		config = mock(YAMLConfig.class);
		selectedProfile = mock(YAMLProfile.class);
		refactoringRules = Collections.singletonList(refactoringRule);

		when(selectedProfile.getName()).thenReturn(selectedProfileName);
		when(config.getProfiles()).thenReturn(Collections.singletonList(selectedProfile));
	}

	@Test
	public void computeConfiguration_selectedProfile_shouldComputeConfigFromSelectedProfile() throws Exception {
		when(config.getSelectedProfile()).thenReturn(selectedProfileName);

		RuleConfigurationWrapper configWrapper = new RuleConfigurationWrapper(config, refactoringRules);

		verify(selectedProfile, times(1)).getLoggerRule();
		verify(selectedProfile, times(1)).getRenamingRule();
		assertNotNull(configWrapper.getLoggerRuleConfigurationOptions());
		assertNotNull(configWrapper.getFieldRenamingRuleConfigurationOptions());
	}

	@Test
	public void computeConfiguration_noSelectedProfile_shouldComputeConfigFromRoot() throws Exception {

		RuleConfigurationWrapper configWrapper = new RuleConfigurationWrapper(config, refactoringRules);

		verify(config, times(1)).getLoggerRule();
		verify(config, times(1)).getRenamingRule();
		assertNotNull(configWrapper.getLoggerRuleConfigurationOptions());
		assertNotNull(configWrapper.getFieldRenamingRuleConfigurationOptions());
	}

	@Test
	public void computeConfiguration_invalidSelectedProfile_shouldThrowException() throws Exception {
		String invalidSelectedProfileName = "invalidProfileName";
		when(config.getSelectedProfile()).thenReturn(invalidSelectedProfileName);
		when(config.getProfiles()).thenReturn(Collections.emptyList());

		assertThrows(StandaloneException.class, () -> new RuleConfigurationWrapper(config, refactoringRules));

		verify(config, never()).getLoggerRule();
		verify(config, never()).getRenamingRule();
	}

	@Test
	public void getSelectedAutomaticRules_selectedProfile_shouldReturnRulesFromProfile() throws Exception {
		when(config.getSelectedProfile()).thenReturn(selectedProfileName);
		when(selectedProfile.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		List<RefactoringRule> rules = ruleConfigurationWrapper.getSelectedAutomaticRules();
		assertEquals(Arrays.asList(refactoringRule), rules);
	}

	@Test
	public void getSelectedAutomaticRules_noSelectedProfile_shouldReturnRulesRoot() throws Exception {
		when(config.getRules()).thenReturn(Collections.singletonList(refactoringRule.getId()));

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		List<RefactoringRule> rules = ruleConfigurationWrapper.getSelectedAutomaticRules();
		assertEquals(Arrays.asList(refactoringRule), rules);
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
	public void getLoggerConfiguration_defaultConfiguration_shouldReturnDefaultOptions() throws Exception {
		YAMLLoggerRule loggerRuleConfiguration = new YAMLLoggerRule();
		when(config.getLoggerRule()).thenReturn(loggerRuleConfiguration);

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		Map<String, String> loggerConfigurationOptions = ruleConfigurationWrapper.getLoggerRuleConfigurationOptions();

		assertEquals(7, loggerConfigurationOptions.size());
		assertEquals(INFO, loggerConfigurationOptions.get("system-out-print"));
		assertEquals(ERROR, loggerConfigurationOptions.get("system-err-print"));
		assertEquals(ERROR, loggerConfigurationOptions.get("print-stacktrace"));
		assertEquals(INFO, loggerConfigurationOptions.get("system-out-print-exception"));
		assertEquals(ERROR, loggerConfigurationOptions.get("system-err-print-exception"));
		assertEquals(ERROR, loggerConfigurationOptions.get("new-logging-statement"));
		assertEquals("true", loggerConfigurationOptions.get("attach-exception-object"));
	}

	@Test
	public void getLoggerConfiguration_customConfiguration_shouldReturnCustomOptions() throws Exception {
		YAMLLoggerRule loggerRuleConfiguration = new YAMLLoggerRule(LogLevelEnum.ERROR, LogLevelEnum.ERROR,
				LogLevelEnum.ERROR, LogLevelEnum.ERROR, LogLevelEnum.ERROR, LogLevelEnum.ERROR, false);
		when(config.getLoggerRule()).thenReturn(loggerRuleConfiguration);

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		Map<String, String> loggerConfigurationOptions = ruleConfigurationWrapper.getLoggerRuleConfigurationOptions();

		assertEquals(7, loggerConfigurationOptions.size());
		assertEquals(ERROR, loggerConfigurationOptions.get("system-out-print"));
		assertEquals(ERROR, loggerConfigurationOptions.get("system-err-print"));
		assertEquals(ERROR, loggerConfigurationOptions.get("print-stacktrace"));
		assertEquals(ERROR, loggerConfigurationOptions.get("system-out-print-exception"));
		assertEquals(ERROR, loggerConfigurationOptions.get("system-err-print-exception"));
		assertEquals(ERROR, loggerConfigurationOptions.get("new-logging-statement"));
		assertEquals("false", loggerConfigurationOptions.get("attach-exception-object"));
	}

	@Test
	public void getFieldRenamingOptions_emptyConfiguration_shouldReturnDefaultOptionsMapWithJMPlimitations()
			throws Exception {
		/*
		 * For limitations see SIM-1250 and SIM-1340
		 */
		YAMLRenamingRule renamingConfiguration = new YAMLRenamingRule();
		when(config.getRenamingRule()).thenReturn(renamingConfiguration);

		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(config, refactoringRules);
		Map<String, Boolean> renamingConfigurationOptions = ruleConfigurationWrapper
			.getFieldRenamingRuleConfigurationOptions();

		assertEquals(6, renamingConfigurationOptions.size());
		assertEquals(false, renamingConfigurationOptions.get("public"));
		assertEquals(false, renamingConfigurationOptions.get("package-protected"));
		assertEquals(false, renamingConfigurationOptions.get("protected"));
		assertEquals(true, renamingConfigurationOptions.get("private"));
		assertEquals(true, renamingConfigurationOptions.get("uppercase-after-underscore"));
		assertEquals(false, renamingConfigurationOptions.get("uppercase-after-dollar"));
	}
}
