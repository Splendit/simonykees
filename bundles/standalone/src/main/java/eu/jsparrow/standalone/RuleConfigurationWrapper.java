package eu.jsparrow.standalone;

import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.ATTACH_EXCEPTION_OBJECT;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.MISSING_LOG_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.PRINT_STACKTRACE_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.config.YAMLLoggerRule;
import eu.jsparrow.core.config.YAMLProfile;
import eu.jsparrow.core.config.YAMLRenamingRule;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.rule.impl.logger.LogLevelEnum;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.standalone.exceptions.StandaloneException;

public class RuleConfigurationWrapper {

	private static final Logger logger = LoggerFactory.getLogger(RuleConfigurationWrapper.class);

	private YAMLConfig yamlConfig;
	private List<RefactoringRule> refactoringRules;
	private YAMLProfile yamlProfile;
	private YAMLLoggerRule loggerRuleConfiguration;
	private YAMLRenamingRule renamingConfiguration;

	public RuleConfigurationWrapper(YAMLConfig yamlConfig, List<RefactoringRule> refactoringRules)
			throws StandaloneException {
		this.yamlConfig = yamlConfig;
		this.refactoringRules = refactoringRules;
		computeConfiguration();
	}

	private void computeConfiguration() throws StandaloneException {
		String selectedProfile = yamlConfig.getSelectedProfile();
		if (selectedProfile != null && !selectedProfile.isEmpty()) {
			String exceptionMessage = NLS.bind(Messages.Activator_standalone_DefaultProfileDoesNotExist,
					selectedProfile);
			yamlProfile = findSelectedProfile(selectedProfile)
				.orElseThrow(() -> new StandaloneException(exceptionMessage));
			loggerRuleConfiguration = yamlProfile.getLoggerRule();
			renamingConfiguration = yamlProfile.getRenamingRule();
		} else {
			loggerRuleConfiguration = yamlConfig.getLoggerRule();
			renamingConfiguration = yamlConfig.getRenamingRule();
		}
	}

	public List<RefactoringRule> getSelectedAutomaticRules() throws StandaloneException {
		List<RefactoringRule> configuredRules;
		if (yamlProfile != null) {
			List<String> selectedIds = yamlProfile.getRules();
			configuredRules = getConfigRules(selectedIds);

		} else {
			List<String> selectedIds = yamlConfig.getRules();
			configuredRules = getConfigRules(selectedIds);
		}

		List<RefactoringRule> selectedAutomaticRules = refactoringRules.stream()
			.filter(RefactoringRule::isEnabled)
			.filter(configuredRules::contains)
			.collect(Collectors.toList());

		logSelectedRulesWithUnsatisfiedDeps(selectedAutomaticRules);
		return selectedAutomaticRules;
	}

	private Optional<YAMLProfile> findSelectedProfile(String profileName) {
		return yamlConfig.getProfiles()
			.stream()
			.filter(profile -> profileName.equals(profile.getName()))
			.findFirst();
	}

	public boolean isSelectedRule(String ruleId) {
		if (yamlProfile != null) {
			return yamlProfile.getRules()
				.contains(ruleId);
		}
		return yamlConfig.getRules()
			.contains(ruleId);
	}

	/**
	 * this method takes a list of rule IDs and produces a list of rules
	 * 
	 * @param configRules
	 *            rule IDs
	 * @return list of rules ({@link RefactoringRule})
	 * @throws YAMLConfigException
	 *             is thrown if a given rule ID does not exist
	 */
	private List<RefactoringRule> getConfigRules(List<String> configRules) throws StandaloneException {
		List<RefactoringRule> rules = RulesContainer.getAllRules(true);
		List<RefactoringRule> configSelectedRules = new LinkedList<>();
		List<String> nonExistentRules = new LinkedList<>();

		for (String configRule : configRules) {
			Optional<RefactoringRule> currentRule = rules.stream()
				.filter(rule -> rule.getId()
					.equals(configRule))
				.findFirst();
			if (currentRule.isPresent()) {
				configSelectedRules.add(currentRule.get());
			} else {
				nonExistentRules.add(configRule);
			}
		}

		if (!nonExistentRules.isEmpty()) {
			String exceptionMessage = NLS.bind(Messages.Activator_standalone_RulesDoNotExist,
					nonExistentRules.toString());
			throw new StandaloneException(exceptionMessage);
		}

		return configSelectedRules;
	}

	public Map<String, String> getLoggerConfigurationOptions() {
		Map<String, LogLevelEnum> selection = new HashMap<>();
		selection.put(SYSTEM_OUT_PRINT_KEY, loggerRuleConfiguration.getSystemOutReplaceOption());
		selection.put(SYSTEM_ERR_PRINT_KEY, loggerRuleConfiguration.getSystemErrReplaceOption());
		selection.put(PRINT_STACKTRACE_KEY, loggerRuleConfiguration.getPrintStacktraceReplaceOption());
		selection.put(SYSTEM_OUT_PRINT_EXCEPTION_KEY,
				loggerRuleConfiguration.getSystemOutPrintExceptionReplaceOption());
		selection.put(SYSTEM_ERR_PRINT_EXCEPTION_KEY,
				loggerRuleConfiguration.getSystemErrPrintExceptionReplaceOption());
		selection.put(MISSING_LOG_KEY, loggerRuleConfiguration.getAddMissingLoggingStatement());

		Map<String, String> selectionMap = selection.entrySet()
			.stream()
			.filter(map -> map.getValue() != null)
			.collect(Collectors.toMap(Entry::getKey, map -> ((LogLevelEnum) map.getValue()).getLogLevel()));

		if (null != loggerRuleConfiguration.getAttachExceptionObject()) {
			selectionMap.put(ATTACH_EXCEPTION_OBJECT, loggerRuleConfiguration.getAttachExceptionObject()
				.toString());
		}
		return selectionMap;
	}

	public Map<String, Boolean> getFieldRenamingOptions() {
		List<String> fieldTypes = renamingConfiguration.getFieldTypes();
		String dollarReplacement = renamingConfiguration.getDollarReplacementOption();
		String underscoreReplacement = renamingConfiguration.getUnderscoreReplacementOption();
		boolean addComments = renamingConfiguration.isAddTodoComments();

		Map<String, Boolean> options = new HashMap<>();
		for (String fieldType : fieldTypes) {
			options.put(fieldType, true);
		}

		if ("Upper".equals(dollarReplacement)) { //$NON-NLS-1$
			options.put(FieldDeclarationOptionKeys.UPPER_CASE_FOLLOWING_DOLLAR_SIGN, true);
		}

		if ("Upper".equals(underscoreReplacement)) { //$NON-NLS-1$
			options.put(FieldDeclarationOptionKeys.UPPER_CASE_FOLLOWING_UNDERSCORE, true);
		}
		options.put(FieldDeclarationOptionKeys.ADD_COMMENT, addComments);

		return options;
	}

	private void logSelectedRulesWithUnsatisfiedDeps(List<RefactoringRule> selectedRules) {
		List<RefactoringRule> unsatisfiedRules = refactoringRules.stream()
			.filter(rule -> !rule.isEnabled())
			.filter(selectedRules::contains)
			.collect(Collectors.toList());

		if (!unsatisfiedRules.isEmpty()) {
			String loggerInfo = NLS.bind(Messages.YAMLConfigUtil_rulesWithUnsatisfiedRequirements,
					unsatisfiedRules.toString());
			logger.info(loggerInfo);
		}
	}
}
