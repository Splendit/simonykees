package eu.jsparrow.rules.api;

import java.util.List;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RefactoringRuleInterface;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Provides you with all the rules.
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public interface RuleService {
	
	List< RefactoringRule<AbstractASTRewriteASTVisitor>> loadRules();  

}
