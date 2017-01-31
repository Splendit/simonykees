package at.splendit.simonykees.core.refactorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.osgi.util.NLS;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.i18n.ExceptionMessages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.impl.TryWithResourceRule;
import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Applies {@link RefactoringRule}s to {@link IJavaElement}s.<br>
 * The work flow should be
 * <ol>
 * <li>{@link #prepareRefactoring()}</li>
 * <li>{@link #doRefactoring()}</li>
 * <li>{@link #commitRefactoring()}</li>
 * </ol>
 * 
 * @author Hannes Schweighofer
 * @since 0.9
 *
 */
public abstract class AbstractRefactorer {

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
	public AbstractRefactorer(List<IJavaElement> javaElements, List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) {
		this.javaElements = javaElements;
		this.rules = rules;
	}

	/**
	 * Prepare working copies for refactoring<br>
	 * Find {@link ICompilationUnit}s and create working copies for the
	 * {@link IJavaElement}s
	 * 
	 * @throws RefactoringException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 * 
	 * @since 0.9
	 * 
	 * @see SimonykeesUtil#collectICompilationUnits(List, List)
	 */
	public void prepareRefactoring() throws RefactoringException {
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		try {
			SimonykeesUtil.collectICompilationUnits(compilationUnits, javaElements);
			if (compilationUnits.isEmpty()) {
				Activator.log(Status.WARNING, ExceptionMessages.AbstractRefactorer_warn_no_compilation_units_found,
						null);
				throw new RefactoringException(ExceptionMessages.AbstractRefactorer_warn_no_compilation_units_found,
						ExceptionMessages.AbstractRefactorer_user_warn_no_compilation_units_found);
			} else if (!workingCopies.isEmpty()) {
				Activator.log(Status.WARNING,
						ExceptionMessages.AbstractRefactorer_warn_working_copies_already_generated, null);
				throw new RefactoringException(
						ExceptionMessages.AbstractRefactorer_warn_working_copies_already_generated);
			} else {
				for (ICompilationUnit compilationUnit : compilationUnits) {
					workingCopies.add(compilationUnit.getWorkingCopy(null));
				}
			}
		} catch (JavaModelException e) {
			Activator.log(Status.ERROR, e.getMessage(), e);
			throw new RefactoringException(ExceptionMessages.AbstractRefactorer_java_element_resoltuion_failed,
					ExceptionMessages.AbstractRefactorer_user_java_element_resoltuion_failed, e);
		}
	}

	/**
	 * Apply {@link RefactoringRule}s to the working copies
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
	public void doRefactoring() throws RefactoringException, RuleException {
		if (workingCopies.isEmpty()) {
			Activator.log(Status.WARNING, ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung, null);
			throw new RefactoringException(ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung);
		}
		List<String> notWorkingRules = new ArrayList<>();
		for (RefactoringRule<? extends ASTVisitor> refactoringRule : rules) {
			//TODO catch all exceptions from ASTVisitor execution?
			// if any exception is thrown discard all changes from this rule
			try {
				refactoringRule.generateDocumentChanges(workingCopies);
				
				if(refactoringRule instanceof TryWithResourceRule){
					refactoringRule.generateDocumentChanges(workingCopies);
				}
			} catch (JavaModelException | ReflectiveOperationException e) {
				Activator.log(Status.ERROR, e.getMessage(), e);
				notWorkingRules.add(refactoringRule.getName());
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
			Activator.log(Status.WARNING, ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung, null);
			throw new RefactoringException(ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung);
		}
		List<String> workingCopiesNotCommited = new ArrayList<>();
		for (Iterator<ICompilationUnit> iterator = workingCopies.iterator(); iterator.hasNext();) {
			ICompilationUnit workingCopy = (ICompilationUnit) iterator.next();
			try {
				SimonykeesUtil.commitAndDiscardWorkingCopy(workingCopy);
				iterator.remove();
			} catch (JavaModelException e) {
				Activator.log(Status.ERROR, e.getMessage(), e);
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
}
