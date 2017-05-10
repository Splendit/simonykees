/**
 * 
 */
package at.splendit.simonykees.core.refactorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.PreviewNode;
import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.ExceptionMessages;

/**
 * 
 * @author Ludwig Werzowa
 * @since 1.2
 */
public class RefactoringPipeline {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringPipeline.class);

	private List<RefactoringState> refactoringStates;

	private List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules;

	/**
	 * 1. We immediately prepare the working copies now instead of waiting for
	 * the SelectRulesWizard to finish.
	 * 
	 * @param javaElements
	 * @param rules
	 * @param monitor
	 * @throws RefactoringException
	 */
	public RefactoringPipeline() {
		/*
		 * We cannot immediately call prepareRefactoring because we need to call
		 * prepareRefactoring in the SelectRulesWizard when finishing (in a Job)
		 * but we need the RefactoringPipeline instance outside of the Job.
		 * Since the Job needs the pipeline to be "final or effectively final",
		 * the constructor has to be called outside of the Job. Plus we only
		 * know the list of rules when finishing.
		 */
	}
	
	public List<PreviewNode> getPreviewNodes() {
		
		List<PreviewNode> previewNodes = new ArrayList<>();
		
		Map<ICompilationUnit, DocumentChange> currentChanges;
		
		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule : rules) {
			
			currentChanges = new HashMap<ICompilationUnit, DocumentChange>();
			
			for (RefactoringState refactoringState : refactoringStates) {
				DocumentChange documentChange = refactoringState.getChangeIfPresent(rule);
				if (null != documentChange) {
					currentChanges.put(refactoringState.getWorkingCopy(), documentChange);
				}
			}
			
			if (!currentChanges.isEmpty()) {
				previewNodes.add(new PreviewNode(rule, currentChanges));
			}
		}
		
		return previewNodes;
	}

	/**
	 * TODO description
	 * 
	 * 
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
	public List<RefactoringState> prepareRefactoring(
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules, List<IJavaElement> javaElements,
			IProgressMonitor monitor) throws RefactoringException {

		// TODO make nicer
		this.rules = rules;
		this.refactoringStates = new ArrayList<>();

		List<ICompilationUnit> compilationUnits = new ArrayList<>();

		try {
			SimonykeesUtil.collectICompilationUnits(compilationUnits, javaElements, monitor);
			if (compilationUnits.isEmpty()) {
				logger.warn(ExceptionMessages.AbstractRefactorer_warn_no_compilation_units_found);
				throw new RefactoringException(ExceptionMessages.AbstractRefactorer_warn_no_compilation_units_found,
						ExceptionMessages.AbstractRefactorer_user_warn_no_compilation_units_found);
			} else {

				/*
				 * Converts the monitor to a SubMonitor and sets name of task on
				 * progress monitor dialog. Size is set to number 100 and then
				 * scaled to size of the compilationUnits list. Each compilation
				 * unit increases worked amount for same size.
				 */
				SubMonitor subMonitor = SubMonitor.convert(monitor, 100).setWorkRemaining(compilationUnits.size());
				subMonitor.setTaskName(""); //$NON-NLS-1$

				for (ICompilationUnit compilationUnit : compilationUnits) {
					subMonitor.subTask(compilationUnit.getElementName());
					// TODO monitor?
					refactoringStates.add(new RefactoringState(compilationUnit.getWorkingCopy(null)));

					/*
					 * If cancel is pressed on progress monitor, abort all and
					 * return, else continue
					 */
					if (subMonitor.isCanceled()) {
						return refactoringStates;
					} else {
						subMonitor.worked(1);
					}
				}

				return refactoringStates;
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
			throw new RefactoringException(ExceptionMessages.AbstractRefactorer_java_element_resoltuion_failed,
					ExceptionMessages.AbstractRefactorer_user_java_element_resoltuion_failed, e);
		}
	}
	
	/**
	 * TODO adjust description
	 * 
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
		if (refactoringStates.isEmpty()) {
			// TODO adjust
			logger.warn(ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung);
			throw new RefactoringException(ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung);
		}
		List<String> refactoringStatesNotCommited = new ArrayList<>();
		for (Iterator<RefactoringState> iterator = refactoringStates.iterator(); iterator.hasNext();) {
			RefactoringState refactoringState = (RefactoringState) iterator.next();
			try {
				SimonykeesUtil.commitAndDiscardWorkingCopy(refactoringState.getWorkingCopy());
				iterator.remove();
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), e);
				refactoringStatesNotCommited.add(refactoringState.getWorkingCopy().getPath().toString());
			}
		}
		if (!refactoringStatesNotCommited.isEmpty()) {
			String notWorkingRulesCollected = refactoringStatesNotCommited.stream().collect(Collectors.joining("\n")); //$NON-NLS-1$
			throw new ReconcileException(
					NLS.bind(ExceptionMessages.AbstractRefactorer_reconcile_failed, notWorkingRulesCollected),
					NLS.bind(ExceptionMessages.AbstractRefactorer_user_reconcile_failed, notWorkingRulesCollected));
		}
	}

	public void doRefactoring(IProgressMonitor monitor) throws RefactoringException, RuleException {
		if (refactoringStates.isEmpty()) {
			// TODO warning adjustment
			logger.warn(ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung);
			throw new RefactoringException(ExceptionMessages.AbstractRefactorer_warn_no_working_copies_foung);
		}

		/*
		 * Converts the monitor to a SubMonitor and sets name of task on
		 * progress monitor dialog Size is set to number 100 and then scaled to
		 * size of the rules list Each refactoring rule increases worked amount
		 * for same size
		 */
		// XXX we now check for refactoringStates size
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100).setWorkRemaining(refactoringStates.size());
		subMonitor.setTaskName(""); //$NON-NLS-1$

		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> refactoringRule : rules) {
			applyRule(refactoringRule, subMonitor);
		}

		// List<String> notWorkingRules = new ArrayList<>();
		// for (RefactoringRule<? extends ASTVisitor> refactoringRule : rules) {

		// if (!notWorkingRules.isEmpty()) {
		// String notWorkingRulesCollected =
		// notWorkingRules.stream().collect(Collectors.joining(", "));
		// //$NON-NLS-1$
		// throw new RuleException(
		// NLS.bind(ExceptionMessages.AbstractRefactorer_rule_execute_failed,
		// notWorkingRulesCollected),
		// NLS.bind(ExceptionMessages.AbstractRefactorer_user_rule_execute_failed,
		// notWorkingRulesCollected));
		// }
	}
	
	public void clearStates() {
		refactoringStates.forEach(s -> s.clearWorkingCopies());
		refactoringStates.clear();
	}

	private void applyRule(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule, IProgressMonitor subMonitor) {
		for (RefactoringState refactoringState : refactoringStates) {
			/*
			 * TODO catch all exceptions from ASTVisitor execution? if any
			 * exception is thrown discard all changes from this rule
			 */
			subMonitor.subTask(refactoringState.getName());
			try {

				refactoringState.addRule(rule);

				// TODO we used to have a test for try with resource here

			} catch (JavaModelException | ReflectiveOperationException e) {
				logger.error(e.getMessage(), e);
				// notWorkingRules.add(refactoringState.getName()); //TODO
			}
			// If cancel is pressed on progress monitor, abort all and return,
			// else continue
			if (subMonitor.isCanceled()) {
				return;
			}
		}
	}

}
