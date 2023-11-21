package eu.jsparrow.standalone;

import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.ATTACH_EXCEPTION_OBJECT;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.MISSING_LOG_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.PRINT_STACKTRACE_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_ERR_PRINT_EXCEPTION_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_OUT_PRINT_EXCEPTION_KEY;
import static eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.RENAME_PACKAGE_PROTECTED_FIELDS;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.RENAME_PRIVATE_FIELDS;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.RENAME_PROTECTED_FIELDS;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.RENAME_PUBLIC_FIELDS;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.UPPER_CASE_FOLLOWING_DOLLAR_SIGN;
import static eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys.UPPER_CASE_FOLLOWING_UNDERSCORE;

import java.util.Collections;
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
import eu.jsparrow.core.rule.impl.FieldsRenamingRule;
import eu.jsparrow.core.rule.impl.logger.LogLevelEnum;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationOptionKeys;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * Reads the selected profile and configurations for the
 * {@link StandardLoggerRule} and {@link FieldsRenamingRule} from a
 * {@link YAMLConfig}. If no selected profile is defined in the
 * {@link YAMLConfig} then the list of the rules in the root yaml are considered
 * as the selected rule.
 * 
 * 
 * @since 2.6.0
 *
 */
public class RuleConfigurationWrapper {

	private static final Logger logger = LoggerFactory.getLogger(RuleConfigurationWrapper.class);
	
	public static final String UPPER = "Upper"; //$NON-NLS-1$
	public static final String LEAVE = "Leave"; //$NON-NLS-1$

	private YAMLConfig yamlConfig;
	private List<RefactoringRule> refactoringRules;
	private YAMLProfile yamlProfile;
	private YAMLLoggerRule loggerRuleConfiguration;
	private YAMLRenamingRule renamingConfiguration;
	private List<RefactoringRule> configuredRules;

	/**
	 * Creates an instance of this wrapper and finds the list of the selected
	 * rules, the configuration for the {@link StandardLoggerRule} and
	 * {@link FieldsRenamingRule}.
	 * 
	 * @param yamlConfig
	 * @param refactoringRules
	 * @throws StandaloneException
	 */
	public RuleConfigurationWrapper(YAMLConfig yamlConfig, List<RefactoringRule> refactoringRules)
			throws StandaloneException {
		this.yamlConfig = yamlConfig;
		this.refactoringRules = refactoringRules;
		computeConfiguration();
	}

	private void computeConfiguration() throws StandaloneException {
		String selectedProfile = yamlConfig.getSelectedProfile();
		if (selectedProfile != null && !selectedProfile.isEmpty()) {
			useConfigurationFromProfile(selectedProfile);
			return;
		}
		useRootConfiguration();
	}

	private void useRootConfiguration() throws StandaloneException {
		loggerRuleConfiguration = getOrDefault(yamlConfig.getLoggerRule());
		renamingConfiguration = getOrDefault(yamlConfig.getRenamingRule());
		List<String> selectedIds = yamlConfig.getRules();
		configuredRules = getConfigRules(selectedIds);
	}

	private void useConfigurationFromProfile(String selectedProfile) throws StandaloneException {
		String exceptionMessage = NLS.bind(Messages.Activator_standalone_DefaultProfileDoesNotExist,
				selectedProfile);
		yamlProfile = findSelectedProfile(selectedProfile)
			.orElseThrow(() -> new StandaloneException(exceptionMessage));
		loggerRuleConfiguration = getOrDefault(yamlProfile.getLoggerRule());
		renamingConfiguration = getOrDefault(yamlProfile.getRenamingRule());
		
		List<String> selectedIds = yamlProfile.getRules();
		configuredRules = getConfigRules(selectedIds);
	}

	private YAMLRenamingRule getOrDefault(YAMLRenamingRule renamingRule) {
		return renamingRule != null ? renamingRule : new YAMLRenamingRule();
	}

	private YAMLLoggerRule getOrDefault(YAMLLoggerRule loggerConfig) {
		return loggerConfig != null ? loggerConfig : new YAMLLoggerRule();
	}

	/**
	 * Finds the list of the selected rules either from the {@link YAMLProfile}
	 * or from the root of {@link YAMLConfig}. Does NOT include the rules which
	 * require configuration, i.e. {@link StandardLoggerRule} or
	 * {@link FieldsRenamingRule}.
	 * 
	 * @return the list of the automatic rules form the selected profile if
	 *         there is one, or the list of the rules from the {@code rules}
	 *         node of the {@link YAMLConfig}.
	 * @throws StandaloneException
	 *             if an invalid rule id is found in the list of the selected
	 *             rules.
	 */
	public List<RefactoringRule> getSelectedAutomaticRules() throws StandaloneException {

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

	/**
	 * Checks whether the given rule id matches any of the rule id-s in the
	 * selected rules.
	 * 
	 * @param ruleId
	 *            the id of the rule to be checked
	 * @return {@code true} if a match is found, and {@code false} otherwise.
	 */
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

		List<String> automaticConfigRules = configRules.stream()
			.filter(id -> !FieldsRenamingRule.FIELDS_RENAMING_RULE_ID.equals(id))
			.filter(id -> !StandardLoggerRule.STANDARD_LOGGER_RULE_ID.equals(id))
			.collect(Collectors.toList());

		for (String configRule : automaticConfigRules) {
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

	/**
	 * Unwraps the configuration options for the {@link StandardLoggerRule} from
	 * the {@link #loggerRuleConfiguration}
	 * 
	 * @return a map with configuration options from
	 *         {@link #loggerRuleConfiguration}
	 */
	public Map<String, String> getLoggerRuleConfigurationOptions() {
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

	/**
	 * Unwraps the configuration options for the {@link FieldsRenamingRule} from
	 * the {@link YAMLRenamingRule}.
	 * 
	 * @return a map with the configuration options from
	 *         {@link #renamingConfiguration}.
	 */
	public Map<String, Boolean> getFieldRenamingRuleConfigurationOptions() {
		List<String> fieldTypes = renamingConfiguration.getFieldTypes();
		String dollarReplacement = renamingConfiguration.getDollarReplacementOption();
		String underscoreReplacement = renamingConfiguration.getUnderscoreReplacementOption();

		Map<String, Boolean> fieldTypesMap = getFieldTypesMap();

		Map<String, Boolean> options = new HashMap<>();
		options.putAll(fieldTypesMap);
		for (String fieldType : fieldTypes) {
			options.put(fieldType, true);
		}

		options.put(UPPER_CASE_FOLLOWING_DOLLAR_SIGN, UPPER.equals(dollarReplacement));
		options.put(UPPER_CASE_FOLLOWING_UNDERSCORE, UPPER.equals(underscoreReplacement));
		
		/*
		 * see SIM-1250. If we are dealing with a multimodule project, we limit
		 * the renaming rule to run only for private fields.
		 * 
		 * SIM-1340. Since a Field can be referenced in test sources and we are
		 * not processing test sources with the JMP, then limitation must apply
		 * also for the single-module projects.
		 */
		options.put(FieldDeclarationOptionKeys.RENAME_PUBLIC_FIELDS, false);
		options.put(FieldDeclarationOptionKeys.RENAME_PROTECTED_FIELDS, false);
		options.put(FieldDeclarationOptionKeys.RENAME_PACKAGE_PROTECTED_FIELDS, false);

		return options;
	}

	private Map<String, Boolean> getFieldTypesMap() {
		Map<String, Boolean> fieldTypesMap = new HashMap<>();
		fieldTypesMap.put(RENAME_PRIVATE_FIELDS, false);
		fieldTypesMap.put(RENAME_PROTECTED_FIELDS, false);
		fieldTypesMap.put(RENAME_PACKAGE_PROTECTED_FIELDS, false);
		fieldTypesMap.put(RENAME_PUBLIC_FIELDS, false);
		return Collections.unmodifiableMap(fieldTypesMap);
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
