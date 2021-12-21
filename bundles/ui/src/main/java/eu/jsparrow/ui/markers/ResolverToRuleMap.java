package eu.jsparrow.ui.markers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.jsparrow.core.markers.ResolverVisitorsFactory;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * A map from jSparrow Marker IDs to their corresponding refactoring rule.
 * 
 * @since 4.7.0
 *
 */
public class ResolverToRuleMap {

	private static final Map<String, RefactoringRule> registry = initResolverToRule();
	
	private ResolverToRuleMap() {
		/*
		 * Hide default constructor
		 */
	}

	private static Map<String, RefactoringRule> initResolverToRule() {
		List<RefactoringRule> allRules = RulesContainer.getAllRules(false);
		Map<String, RuleDescription> allMarkerDescriptions = ResolverVisitorsFactory.getAllMarkerDescriptions();
		
		Map<String, RefactoringRule> result = new HashMap<>();
		for (Map.Entry<String, RuleDescription> entry : allMarkerDescriptions.entrySet()) {
			String resolverId = entry.getKey();
			RuleDescription description = entry.getValue();
			findRuleByDescription(allRules, description).ifPresent(rule -> result.put(resolverId, rule));
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private static Optional<RefactoringRule> findRuleByDescription(List<RefactoringRule>allRules, RuleDescription description) {
		String ruleName = description.getName();
		return allRules.stream()
				.filter(rule -> ruleName.equals(rule.getRuleDescription().getName()))
				.findFirst();
	}
	
	public static Map<String, RefactoringRule> get() {
		return registry;
	}
}
