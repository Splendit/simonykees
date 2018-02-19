package eu.jsparrow.rules.api;

import java.util.List;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Provides you with all the rules.
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public interface RuleService {
	
	List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> loadRules();  

}
