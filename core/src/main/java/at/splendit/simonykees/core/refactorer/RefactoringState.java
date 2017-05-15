package at.splendit.simonykees.core.refactorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.ExceptionMessages;
import at.splendit.simonykees.i18n.Messages;

/**
 * One {@link RefactoringState} per {@link CompilationUnit}.
 * 
 * @author Ludwig Werzowa
 * @since 1.2
 */
public class RefactoringState {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringState.class);

	private ICompilationUnit workingCopy;

	private Map<RefactoringRule<? extends AbstractASTRewriteASTVisitor>, DocumentChange> changes = new HashMap<RefactoringRule<? extends AbstractASTRewriteASTVisitor>, DocumentChange>();

	public RefactoringState(ICompilationUnit workingCopy) {
		super();
		this.workingCopy = workingCopy;
	}

	public String getWorkingCopyName() {
		return this.workingCopy.getElementName();
	}

	/**
	 * Returns the {@link DocumentChange} to one rule.
	 * 
	 * @param rule
	 * @return the corresponding {@link DocumentChange} to a rule or null
	 */
	public DocumentChange getChangeIfPresent(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		return changes.get(rule);
	}

	/**
	 * Whether or not any changes were calculated for all given rules.
	 * 
	 * @return true if at least one rule generated changes, false otherwise
	 */
	public boolean hasChange() {
		return !changes.isEmpty();
	}

	public ICompilationUnit getWorkingCopy() {
		return workingCopy;
	}

	public void addRulesAndGenerateDocumentChanges(List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules)
			throws JavaModelException, ReflectiveOperationException {

		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> refactoringRule : rules) {
			addRuleAndGenerateDocumentChanges(refactoringRule);
		}
	}

	// TODO add monitor
	/**
	 * Changes are applied to working copy but <b>not</b> committed. s
	 * 
	 * @param rule
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 * @throws ReflectiveOperationException
	 *             is thrown if the default constructor of {@link #visitor} is
	 *             not present and the reflective construction fails.
	 */
	public void addRuleAndGenerateDocumentChanges(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule)
			throws JavaModelException, ReflectiveOperationException {

		/*
		 * Sends new child of subMonitor which takes in progress bar size of 1
		 * of rules size In method that part of progress bar is split to number
		 * of compilation units
		 */
		// generateDocumentChanges(rule, subMonitor.newChild(1));

		boolean changesAlreadyPresent = changes.containsKey(rule);

		if (changesAlreadyPresent) {
			// already have changes
			logger.warn(NLS.bind(Messages.RefactoringState_warning_workingcopy_already_present, getWorkingCopyName()));
		} else {
			DocumentChange documentChange = rule.applyRule(workingCopy);
			if (documentChange != null) {
				changes.put(rule, documentChange);
			} else {
				logger.trace(NLS.bind(Messages.RefactoringState_no_changes_found, rule.getName(),
						workingCopy.getElementName()));
			}
		}

	}

	public void clearWorkingCopies() {
		try {
			SimonykeesUtil.discardWorkingCopy(workingCopy);
		} catch (JavaModelException e) {
			logger.error(NLS.bind(ExceptionMessages.RefactoringState_unable_to_discard_working_copy,
					workingCopy.getPath().toString(), e.getMessage()), e);
		}
	}
}
