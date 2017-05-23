package at.splendit.simonykees.core.refactorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.impl.TryWithResourceRule;
import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.ExceptionMessages;

/**
 * Applies {@link RefactoringRule}s to {@link IJavaElement}s.<br>
 * The work flow should be
 * <ol>
 * <li>{@link #prepareRefactoring()}</li>
 * <li>{@link #doRefactoring()}</li>
 * <li>{@link #commitRefactoring()}</li>
 * </ol>
 * 
 * @author Hannes Schweighofer, Andreja Sambolec
 * @since 0.9
 *
 */
public abstract class AbstractRefactorer {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRefactorer.class);
	
	protected List<IJavaElement> javaElements;
	protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules;
	protected List<ICompilationUnit> workingCopies = new ArrayList<>();

	/**
	 * 
	 * @param javaElements
	 *            {@link List} of {@link IJavaElement}s which should be
	 *            refactored
	 * @param rules
	 *            {@link List} of {@link RefactoringRule}s to apply to the
	 *            {@link IJavaElement}s
	 * 
	 * @since 0.9
	 */
	public AbstractRefactorer(List<IJavaElement> javaElements,
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) {
		this.javaElements = javaElements;
		this.rules = rules;
	}

	/**
	 * Prepare working copies for refactoring<br>
	 * Find {@link ICompilationUnit}s and create working copies for the
	 * {@link IJavaElement}s
	 * 
	 * @param IProgressMonitor
	 *            monitor used to show progress in UI
	 * 
	 * @throws RefactoringException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 * 
	 * @since 0.9
	 * 
	 * @see SimonykeesUtil#collectICompilationUnits(List, List)
	 */
	public void prepareRefactoring(IProgressMonitor monitor) throws RefactoringException {
		List<ICompilationUnit> compilationUnits = new ArrayList<>();

		try {
			SimonykeesUtil.collectICompilationUnits(compilationUnits, javaElements, monitor);
			if (compilationUnits.isEmpty()) {
				logger.warn(ExceptionMessages.AbstractRefactorer_warn_no_compilation_units_found);
				throw new RefactoringException(ExceptionMessages.AbstractRefactorer_warn_no_compilation_units_found,
						ExceptionMessages.AbstractRefactorer_user_warn_no_compilation_units_found);
			} else if (!workingCopies.isEmpty()) {
				logger.warn(ExceptionMessages.AbstractRefactorer_warn_working_copies_already_generated);
				throw new RefactoringException(
						ExceptionMessages.AbstractRefactorer_warn_working_copies_already_generated);
			} else {

				/*
				 * Converts the monitor to a SubMonitor and sets name of task on
				 * progress monitor dialog. Size is set to number 100 and then
				 * scaled to size of the compilationUnits list. Each compilation
				 * unit increases worked amount for same size.
				 */
				SubMonitor subMonitor = SubMonitor.convert(monitor, 100).setWorkRemaining(compilationUnits.size());
				subMonitor.setTaskName(""); //$NON-NLS-1$
				
				List<ICompilationUnit> containingErrorList = new ArrayList<>();

				for (ICompilationUnit compilationUnit : compilationUnits) {
					subMonitor.subTask(compilationUnit.getElementName());
					if(SimonykeesUtil.checkForSyntaxErrors(compilationUnit)){
						containingErrorList.add(compilationUnit);
					}
					else {
						workingCopies.add(compilationUnit.getWorkingCopy(null));
					}
					
					/*
					 * If cancel is pressed on progress monitor, abort all and
					 * return, else continue
					 */
					if (subMonitor.isCanceled()) {
						return;
					} else {
						subMonitor.worked(1);
					}
				}
				
				/**
				 * if there are syntax errors within source files display it to the user
				 */
				if(!containingErrorList.isEmpty()) {
					// TODO SIM-416 add the opening of the dialog and processing
				}
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
			throw new RefactoringException(ExceptionMessages.AbstractRefactorer_java_element_resoltuion_failed,
					ExceptionMessages.AbstractRefactorer_user_java_element_resoltuion_failed, e);
		}
	}

	/**
	 * Apply {@link RefactoringRule}s to the working copies
	 * 
	 * @param IProgressMonitor
	 *            monitor used to show progress in UI
	 * 
	 * @throws RefactoringException
	 *             if no working copies were found to apply
	 *             {@link RefactoringRule}s to
	 * @throws RuleException
	 *             if the {@link RefactoringRule} could no be initialised or not
	 *             applied
	 * 
	 * @since 0.9
	 * 
	 * @see RefactoringRule#generateDocumentChanges(List)
	 * 
	 */
	public void doRefactoring(IProgressMonitor monitor) throws RefactoringException, RuleException {
		if (workingCopies.isEmpty()) {
			logger.warn(ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung);
			throw new RefactoringException(ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung);
		}

		/*
		 * Converts the monitor to a SubMonitor and sets name of task on
		 * progress monitor dialog Size is set to number 100 and then scaled to
		 * size of the rules list Each refactoring rule increases worked amount
		 * for same size
		 */
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100).setWorkRemaining(rules.size());
		subMonitor.setTaskName(""); //$NON-NLS-1$

		List<String> notWorkingRules = new ArrayList<>();
		for (RefactoringRule<? extends ASTVisitor> refactoringRule : rules) {
			/*
			 * TODO catch all exceptions from ASTVisitor execution? if any
			 * exception is thrown discard all changes from this rule
			 */
			subMonitor.subTask(refactoringRule.getName());
			try {

				/*
				 * Sends new child of subMonitor which takes in progress bar
				 * size of 1 of rules size In method that part of progress bar
				 * is split to number of compilation units
				 */
				refactoringRule.generateDocumentChanges(workingCopies, subMonitor.newChild(1));

				if (refactoringRule instanceof TryWithResourceRule) {
					refactoringRule.generateDocumentChanges(workingCopies, subMonitor.newChild(0));
				}
			} catch (JavaModelException | ReflectiveOperationException e) {
				logger.error(e.getMessage(), e);
				notWorkingRules.add(refactoringRule.getName());
			}
			// If cancel is pressed on progress monitor, abort all and return,
			// else continue
			if (subMonitor.isCanceled()) {
				return;
			}
		}
		if (!notWorkingRules.isEmpty()) {
			String notWorkingRulesCollected = notWorkingRules.stream().collect(Collectors.joining(", ")); //$NON-NLS-1$
			throw new RuleException(
					NLS.bind(ExceptionMessages.AbstractRefactorer_rule_execute_failed, notWorkingRulesCollected),
					NLS.bind(ExceptionMessages.AbstractRefactorer_user_rule_execute_failed, notWorkingRulesCollected));
		}
	}

	/**
	 * Commit the working copies to the underlying {@link ICompilationUnit}s
	 * 
	 * 
	 * @throws RefactoringException
	 *             if no working copies were found
	 * @throws ReconcileException
	 *             if a working copy cannot be applied to the underlying
	 *             {@link ICompilationUnit}
	 * 
	 * @since 0.9
	 * 
	 * @see SimonykeesUtil#commitAndDiscardWorkingCopy(ICompilationUnit)
	 */
	public void commitRefactoring() throws RefactoringException, ReconcileException {
		if (workingCopies.isEmpty()) {
			logger.warn(ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung);
			throw new RefactoringException(ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung);
		}
		List<String> workingCopiesNotCommited = new ArrayList<>();
		for (Iterator<ICompilationUnit> iterator = workingCopies.iterator(); iterator.hasNext();) {
			ICompilationUnit workingCopy = (ICompilationUnit) iterator.next();
			try {
				SimonykeesUtil.commitAndDiscardWorkingCopy(workingCopy);
				iterator.remove();
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), e);
				workingCopiesNotCommited.add(workingCopy.getPath().toString());
			}
		}
		if (!workingCopiesNotCommited.isEmpty()) {
			String notWorkingRulesCollected = workingCopiesNotCommited.stream().collect(Collectors.joining("\n")); //$NON-NLS-1$
			throw new ReconcileException(
					NLS.bind(ExceptionMessages.AbstractRefactorer_reconcile_failed, notWorkingRulesCollected),
					NLS.bind(ExceptionMessages.AbstractRefactorer_user_reconcile_failed, notWorkingRulesCollected));
		}
	}

	/**
	 * Get all {@link RefactoringRule}s available to the
	 * {@link AbstractRefactorer}
	 * 
	 * @return a {@link List} containing all {@link RefactoringRule}s
	 * 
	 * @since 0.9
	 */
	public List<RefactoringRule<? extends ASTVisitor>> getRules() {
		return Collections.unmodifiableList(rules);
	}

	/**
	 * Check if changes were made by any {@link RefactoringRule}
	 * 
	 * @return
	 *         <ul>
	 *         <li>{@code true} if changes were made by any
	 *         {@link RefactoringRule}</li>
	 *         <li>{@code false} if no changes were made</li>
	 *         </ul>
	 * 
	 * @since 0.9
	 */
	public boolean hasChanges() {
		for (RefactoringRule<? extends ASTVisitor> rule : rules) {
			if (!rule.getDocumentChanges().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public void clearWorkingCopies() {
		for (ICompilationUnit workingCopy : workingCopies) {
			try {
				SimonykeesUtil.discardWorkingCopy(workingCopy);
			} catch (JavaModelException e) {
				logger.error(NLS.bind(ExceptionMessages.AbstractRefactorer_unable_to_discard_working_copy,
								workingCopy.getPath().toString(), e.getMessage()), e);
			}
		}
		workingCopies.clear();
	}
}
