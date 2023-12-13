package eu.jsparrow.core.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Model class for configuration data.
 * 
 * @author Matthias Webhofer
 * @since 2.2.2
 */
public class YAMLConfig {

	@SuppressWarnings("nls")
	private static final List<String> RULES_FOR_TEST = Collections.unmodifiableList(
			Arrays.asList(
					"UseTernaryOperator",
					"RemoveUnusedLocalVariables", "TryWithResource", "MultiCatch", "FunctionalInterface",
					"ImmutableStaticFinalCollections",
					"DiamondOperator", "OverrideAnnotation", "RearrangeClassMembers", "BracketsToControl",
					"MultiVariableDeclarationLine", "InlineLocalVariables", "EnumsWithoutEquals",
					"ReImplementingInterface", "RemoveDoubleNegation", "GuardCondition", "CollapseIfStatements",
					"RemoveExplicitCallToSuper", "RemoveEmptyStatement", "RemoveUnnecessaryThrows",
					"RemoveModifiersInInterfaceProperties", "RemoveUnusedParameter", "ReorderModifiers", "UseListSort",
					"HideDefaultConstructorInUtilityClasses", "MakeFieldsAndVariablesFinal", "RemoveCollectionAddAll",
					"RemoveRedundantTypeCast", "UseFilesBufferedWriter", "UsePredefinedStandardCharset",
					"RemoveRedundantClose", "ReplaceWrongClassForLogger", "UseSecureRandom",
					"ReplaceJUnit4AnnotationsWithJupiter", "ReplaceJUnit4AssertionsWithJupiter",
					"RemoveNewStringConstructor", "InefficientConstructor", "PrimitiveBoxedForString",
					"IndexOfToContains", "RemoveToStringOnString", "StringLiteralEqualityCheck", "StringConcatToPlus",
					"UseIsEmptyOnCollections", "ArithmethicAssignment", "StringBufferToBuilder", "WhileToForEach",
					"ForToForEach", "EnhancedForLoopToStreamForEach", "LambdaForEachIfWrapperToFilter",
					"StatementLambdaToExpression", "LambdaForEachMap", "FlatMapInsteadOfNestedLoops",
					"EnhancedForLoopToStreamAnyMatch", "EnhancedForLoopToStreamFindFirst", "LambdaToMethodReference",
					"UseStringBuilderAppend", "CodeFormatter"
			// , "OrganizeImports"
			// The rule with the id "OrganizeImports" is not in the RulesContainer

			));

	/**
	 * this list holds all IDs of rules which should be executed if no default
	 * profile has been specified
	 */
	private List<String> rules;

	/**
	 * this list holds all specified profiles (see {@link YAMLProfile}
	 */
	private List<YAMLProfile> profiles;

	private String selectedProfile;

	private YAMLExcludes excludes;

	private YAMLRenamingRule renamingRule;

	private YAMLLoggerRule loggerRule;

	public YAMLConfig() {
		this.rules = new LinkedList<>();
		this.profiles = new LinkedList<>();
		this.selectedProfile = ""; //$NON-NLS-1$
		this.excludes = new YAMLExcludes();
		this.renamingRule = new YAMLRenamingRule();
		this.loggerRule = new YAMLLoggerRule();
	}

	public YAMLConfig(List<String> rules, List<YAMLProfile> profiles, String defaultProfile, YAMLExcludes excludes,
			YAMLRenamingRule renamingRule, YAMLLoggerRule loggerRule) {
		this.rules = rules;
		this.profiles = profiles;
		this.selectedProfile = defaultProfile;
		this.excludes = excludes;
		this.renamingRule = renamingRule;
		this.loggerRule = loggerRule;
	}

	/**
	 * provides a default configuration for jsparrow
	 * 
	 * @return default configuration
	 */
	public static YAMLConfig getDefaultConfig() {
		YAMLConfig config = new YAMLConfig();

		List<String> profileRules = new LinkedList<>();

		profileRules.add("CodeFormatter"); //$NON-NLS-1$
		profileRules.add("DiamondOperator"); //$NON-NLS-1$
		profileRules.add("ForToForEach"); //$NON-NLS-1$
		profileRules.add("EnhancedForLoopToStreamForEach"); //$NON-NLS-1$
		profileRules.add("WhileToForEach"); //$NON-NLS-1$
		profileRules.add("MultiCatch"); //$NON-NLS-1$
		profileRules.add("LambdaForEachIfWrapperToFilter"); //$NON-NLS-1$
		profileRules.add("TryWithResource"); //$NON-NLS-1$

		YAMLProfile profile = new YAMLProfile();
		profile.setName("default"); //$NON-NLS-1$
		profile.setRules(profileRules);

		config.getProfiles()
			.add(profile);

		config.setSelectedProfile("default"); //$NON-NLS-1$

		return config;
	}

	/**
	 * provides a default configuration for jsparrow
	 * 
	 * @return default configuration
	 */
	public static YAMLConfig getTestConfig(String filter) {
		YAMLConfig config = new YAMLConfig();

		List<String> profileRules = new LinkedList<>();

		String upperCaseFilter = filter.toUpperCase();
		if (upperCaseFilter != null && !upperCaseFilter.isEmpty()) {
			RULES_FOR_TEST.stream()
				.filter(rule -> rule.toUpperCase()
					.contains(upperCaseFilter))
				.forEach(profileRules::add);
		} else {
			RULES_FOR_TEST.stream()
				.forEach(profileRules::add);
		}

		YAMLProfile profile = new YAMLProfile();
		profile.setName("test"); //$NON-NLS-1$
		profile.setRules(profileRules);

		config.getProfiles()
			.add(profile);

		config.setSelectedProfile("test"); //$NON-NLS-1$

		return config;
	}

	/**
	 * this list holds all IDs of rules which should be executed if no default
	 * profile has been specified
	 */
	public List<String> getRules() {
		if (rules == null) {
			rules = new LinkedList<>();
		}
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	/**
	 * this list holds all specified profiles (see {@link YAMLProfile}
	 */
	public List<YAMLProfile> getProfiles() {
		if (profiles == null) {
			profiles = new LinkedList<>();
		}
		return profiles;
	}

	public void setProfiles(List<YAMLProfile> profiles) {
		this.profiles = profiles;
	}

	/**
	 * default profile specifies a profile from the {@link YAMLConfig#profiles}
	 * list which gets executed by default
	 */
	public String getSelectedProfile() {
		return selectedProfile;
	}

	public void setSelectedProfile(String defaultProfile) {
		this.selectedProfile = defaultProfile;
	}

	public YAMLExcludes getExcludes() {
		return excludes;
	}

	public void setExcludes(YAMLExcludes excludes) {
		this.excludes = excludes;
	}

	public YAMLRenamingRule getRenamingRule() {
		return renamingRule;
	}

	public void setRenamingRule(YAMLRenamingRule renamingRule) {
		this.renamingRule = renamingRule;
	}

	public YAMLLoggerRule getLoggerRule() {
		return loggerRule;
	}

	public void setLoggerRule(YAMLLoggerRule loggerRule) {
		this.loggerRule = loggerRule;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "YAMLConfig [rules=" + rules + ", profiles=" + profiles + ", defaultProfile=" + selectedProfile + ", "
				+ excludes.toString() + ", " + renamingRule.toString() + ", " + loggerRule.toString() + "]";
	}
}
