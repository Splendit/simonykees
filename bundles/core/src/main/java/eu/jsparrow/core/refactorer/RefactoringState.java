package eu.jsparrow.core.refactorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;

/**
 * Manages the transformation state of one {@link ICompilationUnit} and offers
 * capabilities to apply or undo {@link RefactoringRule}s, as well as storing
 * the corresponding {@link DocumentChange}s for each {@link RefactoringRule}.
 * 
 * @author Ludwig Werzowa, Andreja Sambolec
 * @since 1.2
 */
public class RefactoringState {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringState.class);

	private ICompilationUnit original;

	private ICompilationUnit workingCopy;

	private WorkingCopyOwner workingCopyOwner;

	private Map<RefactoringRule, DocumentChange> initialChanges = new HashMap<>();

	private Map<RefactoringRule, DocumentChange> changes = new HashMap<>();

	private List<RefactoringRule> ignoredRules = new ArrayList<>();

	public RefactoringState(ICompilationUnit original, ICompilationUnit workingCopy,
			WorkingCopyOwner workingCopyOwner) {
		this.original = original;
		this.workingCopy = workingCopy;
		this.workingCopyOwner = workingCopyOwner;
	}

	/**
	 * File name with extension.
	 * 
	 * @return e.g.: Example.java
	 */
	public String getWorkingCopyName() {
		return this.workingCopy.getElementName();
	}

	/**
	 * Returns a specific {@link DocumentChange} related to one rule.
	 * 
	 * @param rules
	 *            {@link RefactoringRule} for which {@link DocumentChange}s
	 *            should be returned
	 * @return the corresponding {@link DocumentChange} to a rule or null
	 */
	public DocumentChange getChangeIfPresent(RefactoringRule rule) {
		return changes.get(rule);
	}

	public boolean wasChangeInitialyPresent(RefactoringRule rule) {
		return initialChanges.containsKey(rule);
	}

	/**
	 * Whether or not any changes were calculated for all given rules.
	 * 
	 * @return true if at least one rule generated changes, false otherwise
	 */
	public boolean hasChange() {
		return !changes.isEmpty();
	}

	/**
	 * Whether or not any change has been calculated for the given refactoring
	 * state.
	 * 
	 * @return true if at least one rule generated a {@link DocumentChange}
	 *         which is not null, otherwise false
	 */
	public boolean hasAnyValidChange() {
		if (changes.isEmpty()) {
			return false;
		}
		for (Entry<RefactoringRule, DocumentChange> entry : changes.entrySet()) {
			if (entry.getValue() != null) {
				return true;
			}
			logger.debug("entry.getValue() == null"); //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * Returns the working copy ({@link ICompilationUnit}) for this
	 * {@link RefactoringState}.
	 * 
	 * @return the working copy related to this instance
	 */
	public ICompilationUnit getWorkingCopy() {
		return workingCopy;
	}

	/**
	 * Returns the working copy owner ({@link WorkingCopyOwner}) for this
	 * {@link RefactoringState}.
	 * 
	 * @return the working copy owner related to this instances working copy
	 */
	public WorkingCopyOwner getworkingCopyOwner() {
		return workingCopyOwner;
	}

	/**
	 * Applies a given {@link RefactoringRule}s to the working copy. Changes to
	 * the working copy are <b>not</b> committed yet.
	 * 
	 * @param rule
	 *            {@link RefactoringRule} to be applied
	 * @param astRoot
	 *            the compilation unit for applying the rule to
	 * @return true if there was any change, false otherwise
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 * @throws ReflectiveOperationException
	 *             is thrown if the default constructor of {@link #visitorClass}
	 *             is not present and the reflective construction fails.
	 * @throws RefactoringException
	 */
	public boolean addRuleAndGenerateDocumentChanges(RefactoringRule rule, CompilationUnit astRoot,
			boolean initialApply) throws JavaModelException, ReflectiveOperationException, RefactoringException {
		DocumentChange documentChange = rule.applyRule(workingCopy, astRoot);
		if (documentChange != null) {
			changes.put(rule, documentChange);
			if (initialApply) {
				initialChanges.put(rule, documentChange);
			}
			return true;
		} else {
			String message = NLS.bind(ExceptionMessages.RefactoringState_no_changes_found, rule.getRuleDescription()
				.getName(), workingCopy.getElementName());
			logger.trace(message);
			return false;
		}

	}

	/**
	 * Commit changes to a {@code ICompilationUnit} and discard the working
	 * copy.
	 * 
	 * @param workingCopy
	 *            java document working copy where changes are present
	 * @throws JavaModelException
	 *             if this working copy could not commit. Reasons include: A
	 *             org.eclipse.core.runtime.CoreException occurred while
	 *             updating an underlying resource This element is not a working
	 *             copy (INVALID_ELEMENT_TYPES) A update conflict (described
	 *             above) (UPDATE_CONFLICT) if this working copy could not
	 *             return in its original mode.
	 * @since 0.9
	 */
	public void commitAndDiscardWorkingCopy() throws JavaModelException {
		workingCopy.commitWorkingCopy(false, null);
		discardWorkingCopy();
	}

	/**
	 * Discard a working copy of {@code ICompilationUnit}.
	 * 
	 * @param workingCopy
	 *            java document working copy where changes are present
	 * @throws JavaModelException
	 *             if the working copy could not be discarded or closed.
	 *             Possible reasons: if this working copy could not return in
	 *             its original mode OR if an error occurs closing this element.
	 */
	public void discardWorkingCopy() {
		try {
			workingCopy.discardWorkingCopy();
			workingCopy.close();
			original.close();
		} catch (JavaModelException e) {
			logger
				.error(NLS.bind(ExceptionMessages.RefactoringState_unable_to_discard_working_copy, workingCopy.getPath()
					.toString(), e.getMessage()), e);
		}
	}

	/**
	 * Returns list of rules that are ignored (unselected) for current
	 * refactoring state. This can contain only rules for which refactoring
	 * state contained changes at one point.
	 * 
	 * @return list of rules that are ignored
	 */
	public List<RefactoringRule> getIgnoredRules() {
		return ignoredRules;
	}

	/**
	 * Adds rule to list of rules that are ignored for current refactoring
	 * state. Used when working copy is unchecked.
	 * 
	 * @param rule
	 *            to be ignored
	 */
	public void addRuleToIgnoredRules(RefactoringRule rule) {
		ignoredRules.add(rule);
		changes.put(rule, null);
	}

	/**
	 * Removes rule from list of rules that are ignored for current refactoring
	 * state. Used when working copy is again checked from unchecked state.
	 * 
	 * @param rule
	 *            to be removed from ignored rules
	 */
	public void removeRuleFromIgnoredRules(RefactoringRule rule) {
		if (ignoredRules.contains(rule)) {
			ignoredRules.remove(rule);
		}
	}

	/**
	 * When working copy gets unselected in preview view its state has to be
	 * returned to original
	 */
	public void resetWorkingCopy() {
		try {
			workingCopy.discardWorkingCopy();
			workingCopy = original.getWorkingCopy(workingCopyOwner, null);
			changes.clear();
		} catch (JavaModelException e) {
			logger.error(NLS.bind(ExceptionMessages.RefactoringState_unable_to_reset_working_copy, workingCopy.getPath()
				.toString(), e.getMessage()), e);
		}
	}

	/**
	 * When working copy gets unselected in preview view its state has to be
	 * returned to original
	 */
	public void resetAll() {
		resetWorkingCopy();
		initialChanges.clear();
		ignoredRules.clear();
	}
}
