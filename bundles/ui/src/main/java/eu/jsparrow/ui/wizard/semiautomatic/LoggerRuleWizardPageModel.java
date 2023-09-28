package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import eu.jsparrow.core.rule.impl.logger.StandardLoggerConstants;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.wizard.IValueChangeListener;

/**
 * Wizard page model for configuring logger rule when applying to selected
 * resources
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class LoggerRuleWizardPageModel {

	private static final String NO_SEVERITY_LEVEL = Messages.LoggerRuleWizardPageModel_noSeverityLevel;

	private StandardLoggerRule rule;

	private Map<String, String> currentSelectionMap = new HashMap<>();

	Map<String, Integer> systemOutReplaceOptions = new LinkedHashMap<>();

	Map<String, Integer> systemErrReplaceOptions = new LinkedHashMap<>();

	Map<String, Integer> printStackTraceReplaceOptions = new LinkedHashMap<>();

	Map<String, Integer> missingLogInsertOptions = new LinkedHashMap<>();

	Set<IValueChangeListener> listeners = new HashSet<>();

	private String selectionStatus = ""; //$NON-NLS-1$

	public LoggerRuleWizardPageModel(RefactoringRule rule) {
		this.rule = (StandardLoggerRule) rule;

		currentSelectionMap.putAll(this.rule.getDefaultOptions());
		this.rule.getSelectedOptions()
			.entrySet()
			.forEach(entry -> {
				currentSelectionMap.put(entry.getKey(), entry.getValue());
			});
		systemOutReplaceOptions.putAll(this.rule.getSystemOutReplaceOptions());
		systemErrReplaceOptions.putAll(this.rule.getSystemErrReplaceOptions());
		printStackTraceReplaceOptions.putAll(this.rule.getPrintStackTraceReplaceOptions());
		missingLogInsertOptions.putAll(this.rule.getMissingLogInsertOptions());
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
		listeners.forEach(IValueChangeListener::valueChanged);
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
		String sysOutCurr = currentSelectionMap.get(StandardLoggerConstants.SYSTEM_OUT_PRINT_KEY);
		String sysErrCurr = currentSelectionMap.get(StandardLoggerConstants.SYSTEM_ERR_PRINT_KEY);
		String stackTraceCurr = currentSelectionMap.get(StandardLoggerConstants.PRINT_STACKTRACE_KEY);
		String missingLogCurr = currentSelectionMap.get(StandardLoggerConstants.MISSING_LOG_KEY);

		int sysOutCurrSeverityLevel = (NO_SEVERITY_LEVEL.equals(sysOutCurr) || StringUtils.isEmpty(sysOutCurr)) ? 0
				: rule.getSystemOutReplaceOptions()
					.get(sysOutCurr);
		int sysErrCurrSeverityLevel = (StringUtils.equalsIgnoreCase(NO_SEVERITY_LEVEL, sysErrCurr)
				|| StringUtils.isEmpty(sysErrCurr)) ? 0
						: rule.getSystemErrReplaceOptions()
							.get(sysErrCurr);
		int stackTraceCurrSeverityLevel = (NO_SEVERITY_LEVEL.equals(stackTraceCurr)
				|| StringUtils.isEmpty(stackTraceCurr)) ? 0
						: rule.getPrintStackTraceReplaceOptions()
							.get(stackTraceCurr);
		int missingLogCurrSeverityLevel = (StringUtils.equalsIgnoreCase(NO_SEVERITY_LEVEL, sysErrCurr)
				|| StringUtils.isEmpty(missingLogCurr)) ? 0
						: rule.getMissingLogInsertOptions()
							.get(missingLogCurr);

		selectionStatus = ""; //$NON-NLS-1$

		if (sysOutCurrSeverityLevel == 0 && sysErrCurrSeverityLevel == 0 && stackTraceCurrSeverityLevel == 0
				&& missingLogCurrSeverityLevel == 0) {
			selectionStatus = Messages.LoggerRuleWizardPageModel_err_noTransformation;
		} else if (missingLogCurrSeverityLevel != 0 && (missingLogCurrSeverityLevel < stackTraceCurrSeverityLevel
				|| missingLogCurrSeverityLevel < sysErrCurrSeverityLevel
				|| missingLogCurrSeverityLevel < sysOutCurrSeverityLevel)) {
			/*
			 * The newly inserted logging statement shouldn't have lower
			 * severity level than printStackTrace, System.out.println or
			 * System.err.println
			 */
			selectionStatus = Messages.LoggerRuleWizardPageModel_warn_missingLoggSeverity;
		} else if (stackTraceCurrSeverityLevel != 0 && (stackTraceCurrSeverityLevel < sysOutCurrSeverityLevel
				|| stackTraceCurrSeverityLevel < sysErrCurrSeverityLevel)) {
			// if stackTraceCurrSeverityLevel is empty skip validation of it
			selectionStatus = Messages.LoggerRuleWizardPageModel_warn_stackTraceSeverity;
			/*
			 * stack.trace shouldn't have lesser severity level than System.out
			 * or System.err
			 */
		} else if (sysErrCurrSeverityLevel != 0 && (sysErrCurrSeverityLevel < sysOutCurrSeverityLevel)) {
			// System.err shouldn't have lesser severity level than System.out
			selectionStatus = Messages.LoggerRuleWizardPageModel_warn_errSeverity;
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
	 * Returns the available options for inserting a missing logg statement in a
	 * catch clause.
	 * 
	 * @return a set of options.
	 */
	public Set<String> getMissingLogInsertOptions() {
		Set<String> missingLogInsertOptionsSet = new LinkedHashSet<>();
		missingLogInsertOptionsSet.add(NO_SEVERITY_LEVEL);
		missingLogInsertOptionsSet.addAll(missingLogInsertOptions.keySet());
		return missingLogInsertOptionsSet;
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
