package eu.jsparrow.core.rule.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RefactoringRuleInterface;
import eu.jsparrow.core.visitor.ASTRewriteEvent;
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

	private Map<String, FileChangeCount> changesPerCompilationUnit = new HashMap<>();

	// Internal visibility for usage in unit tests
	RuleApplicationCount() {
	}

	/**
	 * Converts the instance to an integer.
	 * 
	 * @return the current application counter
	 */
	public int toInt() {
		return changesPerCompilationUnit.values()
			.stream()
			.mapToInt(FileChangeCount::getCount)
			.sum();
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

	public FileChangeCount getApplicationsForFile(String compilationUnitHandle) {
		changesPerCompilationUnit.putIfAbsent(compilationUnitHandle, new FileChangeCount(compilationUnitHandle));
		return changesPerCompilationUnit.get(compilationUnitHandle);
	}

	@Override
	public void update(ASTRewriteEvent event) {
		String compilationUnitHandle = event.getCompilationUnit();
		getApplicationsForFile(compilationUnitHandle).update();
	}
}
