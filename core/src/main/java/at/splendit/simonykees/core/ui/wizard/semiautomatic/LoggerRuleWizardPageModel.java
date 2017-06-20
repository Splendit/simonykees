package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerConstants;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerRule;
import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * Wizard page model for configuring logger rule when applying to selected
 * resources
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class LoggerRuleWizardPageModel {

	private StandardLoggerRule rule;

	private Map<String, String> currentSelectionMap = new HashMap<>();

	Map<String, Integer> systemOutReplaceOptions = new LinkedHashMap<>();
	Map<String, Integer> systemErrReplaceOptions = new LinkedHashMap<>();
	Map<String, Integer> printStackTraceReplaceOptions = new LinkedHashMap<>();

	Set<IValueChangeListener> listeners = new HashSet<>();

	private String selectionStatus = ""; //$NON-NLS-1$

	private final String NO_SEVERITY_LEVEL = Messages.LoggerRuleWizardPageModel_noSeverityLevel;

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

	/**
	 * Setter for new selection. Sets new value in map containing current
	 * selection for all combos. Validate new selection and notify listeners of
	 * a change to update view.
	 * 
	 * @param source
	 *            field that was changed
	 * @param selection
	 *            new value of selected field
	 */
	public void setNewSelection(String source, String selection) {
		if (selection.equals(NO_SEVERITY_LEVEL)) {
			selection = ""; //$NON-NLS-1$
		}
		currentSelectionMap.put(source, selection);
		validateSelection();
		notifyListeners();
	}

	/**
	 * Method for validation of current selection in all combos. Sets String
	 * filed with message used to show user warning info if current selection is
	 * not recommended.
	 * 
	 */
	private void validateSelection() {
		String sysOutCurr = currentSelectionMap.get(StandardLoggerConstants.SYSTEM_OUT_PRINT);
		String sysErrCurr = currentSelectionMap.get(StandardLoggerConstants.SYSTEM_ERR_PRINT);
		String stackTraceCurr = currentSelectionMap.get(StandardLoggerConstants.PRINT_STACKTRACE);

		int sysOutCurrSeverityLevel = (sysOutCurr.equals(NO_SEVERITY_LEVEL) || sysOutCurr.isEmpty()) ? 0
				: rule.getSystemOutReplaceOptions().get(sysOutCurr);
		int sysErrCurrSeverityLevel = (sysErrCurr.equals(NO_SEVERITY_LEVEL) || sysErrCurr.isEmpty()) ? 0
				: rule.getSystemErrReplaceOptions().get(sysErrCurr);
		int stackTraceCurrSeverityLevel = (stackTraceCurr.equals(NO_SEVERITY_LEVEL) || stackTraceCurr.isEmpty()) ? 0
				: rule.getPrintStackTraceReplaceOptions().get(stackTraceCurr);

		if (sysOutCurrSeverityLevel == 0 && sysErrCurrSeverityLevel == 0 && stackTraceCurrSeverityLevel == 0) {
			selectionStatus = Messages.LoggerRuleWizardPageModel_err_noTransformation;
		} else if (!(stackTraceCurrSeverityLevel == 0) && (stackTraceCurrSeverityLevel < sysOutCurrSeverityLevel
				|| stackTraceCurrSeverityLevel < sysErrCurrSeverityLevel)) {
			// if stackTraceCurrSeverityLevel is empty skip validation of it
			selectionStatus = Messages.LoggerRuleWizardPageModel_warn_stackTraceSeverity;
			/*
			 * stack.trace shouldn't have lesser severity level than System.out
			 * or System.err
			 */
		} else if (!(sysErrCurrSeverityLevel == 0) && (sysErrCurrSeverityLevel < sysOutCurrSeverityLevel)) {
			// System.err shouldn't have lesser severity level than System.out
			selectionStatus = Messages.LoggerRuleWizardPageModel_warn_errSeverity;
//		} else if (sysOutCurrSeverityLevel == 0 || sysErrCurrSeverityLevel == 0 || stackTraceCurrSeverityLevel == 0) {
//			// No logging type should be left blank
//			selectionStatus = Messages.LoggerRuleWizardPageModel_warn_blankLoggingType;
		} else {
			selectionStatus = ""; //$NON-NLS-1$
		}
	}

	/**
	 * Returns the available options for replacing the System.out.println method
	 * 
	 * @return a set of replacement options.
	 */
	public Set<String> getSystemOutReplaceOptions() {
		Set<String> systemOutReplaceOptionsSet = new LinkedHashSet<>();
		systemOutReplaceOptionsSet.add(NO_SEVERITY_LEVEL);
		systemOutReplaceOptionsSet.addAll(systemOutReplaceOptions.keySet());
		return systemOutReplaceOptionsSet;
	}

	/**
	 * Returns the available options for replacing the System.err.println method
	 * 
	 * @return a set of replacement options.
	 */
	public Set<String> getSystemErrReplaceOptions() {
		Set<String> systemErrReplaceOptionsSet = new LinkedHashSet<>();
		systemErrReplaceOptionsSet.add(NO_SEVERITY_LEVEL);
		systemErrReplaceOptionsSet.addAll(systemErrReplaceOptions.keySet());
		return systemErrReplaceOptionsSet;
	}

	/**
	 * Returns the available options for replacing the printStackTrace method
	 * 
	 * @return a set of replacement options.
	 */
	public Set<String> getPrintStackTraceReplaceOptions() {
		Set<String> printStackTraceReplaceOptionsSet = new LinkedHashSet<>();
		printStackTraceReplaceOptionsSet.add(NO_SEVERITY_LEVEL);
		printStackTraceReplaceOptionsSet.addAll(printStackTraceReplaceOptions.keySet());
		return printStackTraceReplaceOptionsSet;
	}

	/**
	 * Getter for validation status result
	 * 
	 * @return String result of validation. Message for warning if selection is
	 *         not recommended or empty String if it is acceptable
	 */
	public String getSelectionStatus() {
		return selectionStatus;
	}

}
