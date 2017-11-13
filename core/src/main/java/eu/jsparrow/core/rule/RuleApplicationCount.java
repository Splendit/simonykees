package eu.jsparrow.core.rule;

import java.util.HashMap;
import java.util.Map;

import eu.jsparrow.core.visitor.ASTRewriteVisitorListener;

/**
 * A {@link ASTRewriteVisitorListener} that simply increases a counter when
 * notified. Used in {@link RefactoringRule} in order to count how often a
 * specific rule has been used.
 * 
 * @author Hans-Jörg Schrödl, Matthias Webhofer
 * @since 2.3.0
 */
public class RuleApplicationCount implements ASTRewriteVisitorListener {

	private static final Map<RefactoringRuleInterface, RuleApplicationCount> applicationCounters = new HashMap<>();

	private Map<String, Integer> applicationCounterPerCompilationUnit = new HashMap<>();

	// Internal visibility for usage in unit tests
	RuleApplicationCount() {
	}

	/**
	 * Converts the instance to an integer.
	 * 
	 * @return the current application counter
	 */
	public int toInt() {
		return applicationCounterPerCompilationUnit.values()
			.stream()
			.mapToInt(Integer::intValue)
			.sum();
	}

	public int toInt(String compilationUnitHandle) {
		return applicationCounterPerCompilationUnit.get(compilationUnitHandle);
	}

	@Override
	public void update(String compilationUnitHandle) {
		int count = 1;
		if (applicationCounterPerCompilationUnit.containsKey(compilationUnitHandle)) {
			count = (applicationCounterPerCompilationUnit.get(compilationUnitHandle)) + 1;
		}
		applicationCounterPerCompilationUnit.put(compilationUnitHandle, count);
	}

	/**
	 * Returns the application count for a specific rule. If no application
	 * counter is available for a given rule an new one is created.
	 * 
	 * @param rule
	 *            rule to get the application count for
	 * @return the application count for a given rule
	 */
	public static RuleApplicationCount getFor(RefactoringRuleInterface rule) {
		applicationCounters.putIfAbsent(rule, new RuleApplicationCount());
		return applicationCounters.get(rule);
	}

	/**
	 * Removes all application counters.
	 */
	public static void clear() {
		applicationCounters.clear();
	}

	public Map<String, Integer> getApplicationCounterPerCompilationUnit() {
		return applicationCounterPerCompilationUnit;
	}

	@Override
	public boolean remove(String compilationUnitHandle) {
		Integer currentCount = applicationCounterPerCompilationUnit.remove(compilationUnitHandle);
		return currentCount != null ? true : false;
	}
}
