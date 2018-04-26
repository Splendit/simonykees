package eu.jsparrow.rules.api;

import java.util.List;

import eu.jsparrow.rules.common.RefactoringRule;

/**
 * Service interface for rule providers. Implementors provide a list of rules
 * that may be used by service consumers.
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public interface RuleService {

	List<RefactoringRule> loadRules();

}
