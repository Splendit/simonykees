package eu.jsparrow.core.rule;

import java.util.HashMap;
import java.util.Map;

import eu.jsparrow.core.visitor.ASTRewriteVisitorListener;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Counts how often a rule is applied
 * @author hans
 *
 */
public class RuleApplicationCounter implements ASTRewriteVisitorListener {
	
	private static final Map<RefactoringRule, RuleApplicationCounter> applicationCounters = new HashMap<>();

	private int applicationCounter = 0;
	
	RuleApplicationCounter() {
	}
	
	public int toInt() {
		return applicationCounter;
	}

	@Override
	public void update() {
		applicationCounter++;
	}
	
	public static RuleApplicationCounter get(RefactoringRule rule) {
		if(!applicationCounters.containsKey(rule)) {
			applicationCounters.put(rule, new RuleApplicationCounter());
		}
		return applicationCounters.get(rule);
	}
}
