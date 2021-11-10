package eu.jsparrow.core.rule;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * A factory of rule descriptions for rules registered in {@link RulesContainer}.  
 * 
 * @since 4.5.0
 *
 */
public class RuleDescriptionFactory {

	private static Map<String, RuleDescription> descriptions;

	private RuleDescriptionFactory() {
		/*
		 * Hide default constructor. 
		 */
	}
	
	public static synchronized RuleDescription findByRuleId(String id) {
		if (descriptions == null) {
			descriptions = createRuleDescriptions();
		}
		return descriptions.get(id);
	}

	private static Map<String, RuleDescription> createRuleDescriptions() {
		List<RefactoringRule> allRules = RulesContainer.getAllRules(false);
		return allRules.stream()
			.collect(Collectors.toMap(RefactoringRule::getId, RefactoringRule::getRuleDescription));
	}

}
