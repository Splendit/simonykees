package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerOptions;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerRule;
import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

public class LoggerRuleWizardPageModel {

	private StandardLoggerRule rule;

	private Map<String, String> currentSelectionMap = new HashMap<>();
	
	Map<String, Integer> systemOutReplaceOptions = new HashMap<>();
	Map<String, Integer> systemErrReplaceOptions = new HashMap<>();
	Map<String, Integer> printStackTraceReplaceOptions = new HashMap<>();

	Set<IValueChangeListener> listeners = new HashSet<>();
	
	private String selectionStatus = ""; //$NON-NLS-1$

	public LoggerRuleWizardPageModel(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		this.rule = (StandardLoggerRule) rule;

		currentSelectionMap.putAll(this.rule.getDefaultOptions());
		
		systemOutReplaceOptions.putAll(this.rule.getSystemOutReplaceOptions());
		systemErrReplaceOptions.putAll(this.rule.getSystemErrReplaceOptions());
		printStackTraceReplaceOptions.putAll(this.rule.getPrintStackTraceReplaceOptions());
	}

	/**
	 * Getter for map that contains all three logging types and current severity
	 * level chosen for each of them.
	 * 
	 * @return map with current combo box selection
	 */
	public Map<String, String> getCurrentSelectionMap() {
		return currentSelectionMap;
	}

	/**
	 * Adds listener to model which notifies view to refresh data when ever
	 * something in model changes
	 */
	public void addListener(IValueChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Called from every method in model that changes anything in model.
	 * Notifies view to redraw all elements with new data.
	 */
	public void notifyListeners() {
		for (IValueChangeListener listener : listeners) {
			listener.valueChanged();
		}
	}

	public void setNewSelection(String source, String selection) {
		currentSelectionMap.put(source, selection);
		validateSelection();
		notifyListeners();
	}

	private void validateSelection() {
		String sysOutCurr = currentSelectionMap.get(StandardLoggerOptions.SYSTEM_OUT_PRINT);
		String sysErrCurr = currentSelectionMap.get(StandardLoggerOptions.SYSTEM_ERR_PRINT);
		String stackTraceCurr = currentSelectionMap.get(StandardLoggerOptions.PRINT_STACKTRACE);

		int sysOutCurrSeverityLevel = (sysOutCurr.isEmpty()) ? 0 : rule.getSystemOutReplaceOptions().get(sysOutCurr);
		int sysErrCurrSeverityLevel = (sysErrCurr.isEmpty()) ? 0 : rule.getSystemErrReplaceOptions().get(sysErrCurr);
		int stackTraceCurrSeverityLevel = (stackTraceCurr.isEmpty()) ? 0 : rule.getPrintStackTraceReplaceOptions().get(stackTraceCurr);

		if (!(stackTraceCurrSeverityLevel == 0) && (stackTraceCurrSeverityLevel < sysOutCurrSeverityLevel
				|| stackTraceCurrSeverityLevel < sysErrCurrSeverityLevel)) {
			// if stackTraceCurrSeverityLevel is empty skip validation of it
			// TODO validation failed, StatusInfo warning
			selectionStatus = "printStackTrace shouldn't have lesser severity level than System.out.println or System.err.println";
			// stack.trace shouldn't have lesser severity level than System.out
			// or System.err
		} else if (!(sysErrCurrSeverityLevel == 0) && (sysErrCurrSeverityLevel < sysOutCurrSeverityLevel)) {
			// TODO validation failed, StatusInfo warning
			selectionStatus = "System.err.println shouldn't have lesser severity level than System.out.println";
			// System.err shouldn't have lesser severity level than System.out
		} else if (sysOutCurrSeverityLevel == 0 || sysErrCurrSeverityLevel == 0 || stackTraceCurrSeverityLevel == 0) {
			// TODO validation failed, StatusInfo warning
			selectionStatus = "No logging type should be left blank";
			// No logging type should be left blank
		} else {
			selectionStatus = ""; //$NON-NLS-1$
		}
	}

	public Set<String> getSystemOutReplaceOptions() {
		return systemOutReplaceOptions.keySet();
	}

	public Set<String> getSystemErrReplaceOptions() {
		return systemErrReplaceOptions.keySet();
	}

	public Set<String> getPrintStackTraceReplaceOptions() {
		return printStackTraceReplaceOptions.keySet();
	}

	public String getSelectionStatus() {
		return selectionStatus;
	}

}
