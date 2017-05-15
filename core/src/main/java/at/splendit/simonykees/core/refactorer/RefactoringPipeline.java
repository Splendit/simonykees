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
import at.splendit.simonykees.core.ui.preview.PreviewNode;
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
	 * 
	 * 
	 * @param rules
	 *            {@link List} of {@link RefactoringRule}s to apply to the
	 *            selected {@link IJavaElement}s
	 */
	public RefactoringPipeline(List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) {

		/*
		 * Note: We cannot immediately call prepareRefactoring because we need
		 * to call prepareRefactoring in the SelectRulesWizard when finishing
		 * (in a Job) but we need the RefactoringPipeline instance outside of
		 * the Job. Since the Job needs the pipeline to be
		 * "final or effectively final", the constructor has to be called
		 * outside of the Job. Plus we only know the list of rules when
		 * finishing.
		 */

		this.rules = rules;
		this.refactoringStates = new ArrayList<>();
	}

	/**
	 * Creates a map of changes per rule.
	 * <p>
	 * This method enables viewing all changes from one specific rule.
	 * 
	 * @return a list of {@link PreviewNode}s
	 */
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
	 * Check if any {@link RefactoringRule} lead to changes in any
	 * {@link RefactoringState}
	 * 
	 * @return
	 *         <ul>
	 *         <li>{@code true} if changes were made</li>
	 *         <li>{@code false} if no changes were made</li>
	 *         </ul>
	 * 
	 * @since 1.2
	 */
	public boolean hasChanges() {
		for (RefactoringState refactoringState : refactoringStates) {
			if (refactoringState.hasChange()) {
				return true;
			}
		}
		return false;
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
	public List<RefactoringState> prepareRefactoring(List<IJavaElement> javaElements, IProgressMonitor monitor)
			throws RefactoringException {

		List<ICompilationUnit> compilationUnits = new ArrayList<>();

		try {
			SimonykeesUtil.collectICompilationUnits(compilationUnits, javaElements, monitor);
			if (compilationUnits.isEmpty()) {
				logger.warn(ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found);
				throw new RefactoringException(ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found,
						ExceptionMessages.RefactoringPipeline_user_warn_no_compilation_units_found);
			} else if (!refactoringStates.isEmpty()) {
				logger.warn(ExceptionMessages.RefactoringPipeline_warn_working_copies_already_generated);
				throw new RefactoringException(
						ExceptionMessages.RefactoringPipeline_warn_working_copies_already_generated,
						ExceptionMessages.RefactoringPipeline_user_warn_changes_already_generated);
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
			throw new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
					ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e);
		}
	}

	/**
	 * Apply {@link RefactoringRule}s to the working copies of each
	 * {@link RefactoringState}
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
	 * @since 1.2
	 * 
	 * @see RefactoringRule#generateDocumentChanges(List)
	 * 
	 */
	public void doRefactoring(IProgressMonitor monitor) throws RefactoringException, RuleException {
		if (refactoringStates.isEmpty()) {
			// TODO warning adjustment
			logger.warn(ExceptionMessages.RefactoringPipeline_warn_no_working_copies_found);
			throw new RefactoringException(ExceptionMessages.RefactoringPipeline_warn_no_working_copies_found,
					ExceptionMessages.RefactoringPipeline_user_warn_no_java_files_found_to_apply_rules);
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

		List<String> notWorkingRules = new ArrayList<>();
		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> refactoringRule : rules) {

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
				applyRuleToAllStates(refactoringRule, subMonitor.newChild(1));

			} catch (JavaModelException | ReflectiveOperationException e) {
				logger.error(e.getMessage(), e);
				notWorkingRules.add(refactoringRule.getName());
			}
			/*
			 * If cancel is pressed on progress monitor, abort all and return,
			 * else continue
			 */
			if (subMonitor.isCanceled()) {
				return;
			}
		}

		if (!notWorkingRules.isEmpty()) {
			String notWorkingRulesCollected = notWorkingRules.stream().collect(Collectors.joining(", ")); //$NON-NLS-1$
			throw new RuleException(
					NLS.bind(ExceptionMessages.RefactoringPipeline_rule_execute_failed, notWorkingRulesCollected),
					NLS.bind(ExceptionMessages.RefactoringPipeline_user_rule_execute_failed, notWorkingRulesCollected));
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
			logger.warn(ExceptionMessages.RefactoringPipeline_warn_no_working_copies_found);
			throw new RefactoringException(ExceptionMessages.RefactoringPipeline_warn_no_working_copies_found);
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
					NLS.bind(ExceptionMessages.RefactoringPipeline_reconcile_failed, notWorkingRulesCollected),
					NLS.bind(ExceptionMessages.RefactoringPipeline_user_reconcile_failed, notWorkingRulesCollected));
		}
	}

	/**
	 * Clears everything.
	 * <p>
	 * This method should be called when canceling or finishing refactorings.
	 */
	public void clearStates() {
		refactoringStates.forEach(s -> s.clearWorkingCopies());
		refactoringStates.clear();
	}

	/**
	 * This functionality used to be in the {@link RefactoringRule}
	 * 
	 * @param rule
	 * @param subMonitor
	 * @throws JavaModelException
	 * @throws ReflectiveOperationException
	 */
	private void applyRuleToAllStates(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule,
			IProgressMonitor subMonitor) throws JavaModelException, ReflectiveOperationException {

		for (RefactoringState refactoringState : refactoringStates) {
			/*
			 * TODO catch all exceptions from ASTVisitor execution? if any
			 * exception is thrown discard all changes from this rule
			 */
			subMonitor.subTask(refactoringState.getWorkingCopyName());

			refactoringState.addRuleAndGenerateDocumentChanges(rule);

			// TODO we used to have a test for try with resource here

			/*
			 * If cancel is pressed on progress monitor, abort all and return,
			 * else continue
			 */
			if (subMonitor.isCanceled()) {
				return;
			}
		}
	}

}
