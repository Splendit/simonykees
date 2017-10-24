package eu.jsparrow.core.rule;

import java.util.HashMap;
import java.util.Map;

import eu.jsparrow.core.visitor.ASTRewriteVisitorListener;

/**
 * A {@link ASTRewriteVisitorListener} that simply increases a counter when
 * notified. Used in {@link RefactoringRule} in order to count how often a
 * specific rule has been used.
 * 
 * @author Hans-Jörg Schrödl
 */
public class RuleApplicationCount implements ASTRewriteVisitorListener {

	private static final Map<RefactoringRuleInterface, RuleApplicationCount> applicationCounters = new HashMap<>();

	private int applicationCounter = 0;

	// Internal visibility for usage in unit tests
	RuleApplicationCount() {
	}

	/**
	 * Converts the instance to an integer.
	 * 
	 * @return the current application counter
	 */
	public int toInt() {
		return applicationCounter;
	}

	@Override
	public void update() {
		applicationCounter++;
	}

	/**
	 * Returns the application count for a specific rule. If no application
	 * counter is available for a given rule an new one is created.
	 * 
	 * @param rule
	 *            rule to get the application count for
	 * @return the application count for a given rule
	 */
	public static RuleApplicationCount get(RefactoringRuleInterface rule) {
		applicationCounters.putIfAbsent(rule, new RuleApplicationCount());
		return applicationCounters.get(rule);
	}

	/**
	 * Removes all application counters.
	 */
	public static void clear() {
		applicationCounters.clear();
	}
}
