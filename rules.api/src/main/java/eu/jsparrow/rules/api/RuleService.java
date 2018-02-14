package eu.jsparrow.rules.api;

import java.util.List;

import eu.jsparrow.core.rule.RefactoringRuleInterface;

/**
 * Provides you with all the rules.
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public interface RuleService {
	
	List<RefactoringRuleInterface> loadRules();  

}
