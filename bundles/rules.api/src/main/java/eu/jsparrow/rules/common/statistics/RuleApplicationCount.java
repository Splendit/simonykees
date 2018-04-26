package eu.jsparrow.rules.common.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.visitor.ASTRewriteEvent;
import eu.jsparrow.rules.common.visitor.ASTRewriteVisitorListener;

/**
 * A {@link ASTRewriteVisitorListener} that simply increases a counter when
 * notified. Used in {@link RefactoringRuleImpl} in order to count how often a
 * specific rule has been used.
 * 
 * @author Hans-Jörg Schrödl, Matthias Webhofer
 * @since 2.3.0
 */
public class RuleApplicationCount implements ASTRewriteVisitorListener {

	private static final Map<RefactoringRule, RuleApplicationCount> applicationCounters = new HashMap<>();

	private Map<String, FileChangeCount> changesPerCompilationUnit = new HashMap<>();

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
	public static RuleApplicationCount getFor(RefactoringRule rule) {
		applicationCounters.putIfAbsent(rule, new RuleApplicationCount());
		return applicationCounters.get(rule);
	}

	/**
	 * Removes all application counters.
	 */
	public static void clear() {
		applicationCounters.clear();
	}

	/**
	 * Returns the number of changes for the given file.
	 * 
	 * @param compilationUnitHandle
	 *            handle of the file to count changes for
	 * @return the number of changes for the given file
	 */
	public FileChangeCount getApplicationsForFile(String compilationUnitHandle) {
		changesPerCompilationUnit.putIfAbsent(compilationUnitHandle, new FileChangeCount(compilationUnitHandle));
		return changesPerCompilationUnit.get(compilationUnitHandle);
	}

	/**
	 * Returns the number of changes for the given file handles.
	 * 
	 * @param compilationUnitHandles
	 *            compilation unit file handles for the files to track changes
	 *            for
	 * @return the total number of changes in the given files
	 */
	public int getApplicationsForFiles(List<String> compilationUnitHandles) {
		return compilationUnitHandles.stream()
			.mapToInt(x -> getApplicationsForFile(x).getCount())
			.sum();
	}

	/**
	 * Gets a map of files and their number of changes for this rule.
	 * 
	 * @return the files with changes for this rule
	 */
	public Map<String, FileChangeCount> getApplicationsInFiles() {
		return changesPerCompilationUnit;
	}

	@Override
	public void update(ASTRewriteEvent event) {
		FileChangeCount count = getApplicationsForFile(event.getCompilationUnit());
		count.update();
	}
}
