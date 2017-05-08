package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

public class LoggerRuleWizardPageModel {

	private RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule;

	public final String sysOutComboValueId = "sysOutComboValue"; //$NON-NLS-1$
	public final String sysErrComboValueId = "sysErrComboValue"; //$NON-NLS-1$
	public final String stackTraceComboValueId = "stackTraceComboValue"; //$NON-NLS-1$

	private Map<String, String> currentSelectionMap = new HashMap<>();
	private Map<String, Integer> severityNameLevelMap = new HashMap<>();

	Set<IValueChangeListener> listeners = new HashSet<>();

	public LoggerRuleWizardPageModel(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		this.rule = rule;

		severityNameLevelMap.put("", 0);
		severityNameLevelMap.put("DEBUG", 1);
		severityNameLevelMap.put("INFO", 2);
		severityNameLevelMap.put("WARN", 3);
		severityNameLevelMap.put("ERROR", 4);

		currentSelectionMap.put(sysOutComboValueId, "INFO");
		currentSelectionMap.put(sysErrComboValueId, "ERROR");
		currentSelectionMap.put(stackTraceComboValueId, "ERROR");
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
	
	//TODO DELETE, just for testing!!!
	public Map<String, Integer> getSeverityNameLevelMap() {
		return severityNameLevelMap;
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
	}

	private void validateSelection() {
		String sysOutCurr = currentSelectionMap.get(sysOutComboValueId);
		String sysErrCurr = currentSelectionMap.get(sysErrComboValueId);
		String stackTraceCurr = currentSelectionMap.get(stackTraceComboValueId);

		int sysOutCurrSeverityLevel = severityNameLevelMap.get(sysOutCurr);
		int sysErrCurrSeverityLevel = severityNameLevelMap.get(sysErrCurr);
		int stackTraceCurrSeverityLevel = severityNameLevelMap.get(stackTraceCurr);

		if (!(stackTraceCurrSeverityLevel == 0) && (stackTraceCurrSeverityLevel < sysOutCurrSeverityLevel
				|| stackTraceCurrSeverityLevel < sysErrCurrSeverityLevel)) {
			// if stackTraceCurrSeverityLevel is empty skip validation of it
			// TODO validation failed, StatusInfo warning
			// stack.trace shouldn't have lesser severity level than System.out
			// or System.err
		} else if (!(sysErrCurrSeverityLevel == 0) && (sysErrCurrSeverityLevel < sysOutCurrSeverityLevel)) {
			// TODO validation failed, StatusInfo warning
			// System.err shouldn't have lesser severity level than System.out
		} else if (sysOutCurrSeverityLevel == 0 || sysErrCurrSeverityLevel == 0 || stackTraceCurrSeverityLevel == 0) {
			// TODO validation failed, StatusInfo warning
			// No logging type should be left blank
		}
	}

}
