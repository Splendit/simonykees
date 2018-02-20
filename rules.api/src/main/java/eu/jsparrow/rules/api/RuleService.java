package eu.jsparrow.rules.api;

import java.util.List;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Service interface for rule providers. Implementors provide a list of rules
 * that may be used by service consumers.
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public interface RuleService {

	List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> loadRules();

}
