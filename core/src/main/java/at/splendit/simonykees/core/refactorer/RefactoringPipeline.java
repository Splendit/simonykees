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
import at.splendit.simonykees.core.exception.model.NotWorkingRuleModel;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.RefactoringUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.ExceptionMessages;

/**
 * This class manages the selected {@link RefactoringRule}s and the selected
 * {@link IJavaElement}s and offers functionality to apply the first to the
 * latter.
 * 
 * @author Ludwig Werzowa, Andreja Sambolec
 * @since 1.2
 */
public class RefactoringPipeline {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringPipeline.class);

	/**
	 * List of selected {@link IJavaElement}s wrapped as
	 * {@link RefactoringState}s
	 */
	private List<RefactoringState> refactoringStates;

	/**
	 * List of selected rules.
	 */
	private List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules;

	/**
	 * Constructor without parameters, used to create RefactoringPipeline before
	 * SelectRulesWizard is opened
	 */
	public RefactoringPipeline() {
		this.refactoringStates = new ArrayList<>();
	}

	/**
	 * Stores the selected rules.
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

	public List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getRules() {
		return rules;
	}

	/**
	 * Setter for rules when finish button is pressed in SelectRulesWizard to
	 * store selected rules
	 * 
	 * @param rules
	 *            selected rules
	 */
	public void setRules(List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) {
		this.rules = rules;
	}

	public Map<ICompilationUnit, DocumentChange> getChangesForRule(
			RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		Map<ICompilationUnit, DocumentChange> currentChanges = new HashMap<ICompilationUnit, DocumentChange>();

		for (RefactoringState refactoringState : refactoringStates) {
			DocumentChange documentChange = refactoringState.getChangeIfPresent(rule);
			if (null != documentChange) {
				currentChanges.put(refactoringState.getWorkingCopy(), documentChange);
			} else if (refactoringState.wasChangeInitialyPresent(rule)) {
				currentChanges.put(refactoringState.getWorkingCopy(), null);
			}
		}

		return currentChanges;
	}

	/**
	 * Check if any {@link RefactoringRule} lead to changes in any
	 * {@link RefactoringState}.
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
	 * When prepare refactoring is finished it can happen that there is no
	 * refactoringStates if all selected classes have compilation errors.
	 * 
	 * @return true if has at least one refactoring state, false otherwise
	 */
	public boolean hasRefactoringStates() {
		if (!refactoringStates.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Prepare working copies for refactoring.
	 * <p>
	 * Takes a list of {@link IJavaElement}s and creates
	 * {@link ICompilationUnit}s for them. Those {@link ICompilationUnit}s are
	 * stored as working copies in a list of {@link RefactoringState}s.
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
	 * @see RefactoringUtil#collectICompilationUnits(List, List)
	 */
	public List<ICompilationUnit> prepareRefactoring(List<IJavaElement> javaElements, IProgressMonitor monitor)
			throws RefactoringException {

		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		List<ICompilationUnit> containingErrorList = new ArrayList<>();

		try {
			RefactoringUtil.collectICompilationUnits(compilationUnits, javaElements, monitor);
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
					if (RefactoringUtil.checkForSyntaxErrors(compilationUnit)) {
						containingErrorList.add(compilationUnit);
					} else {
						refactoringStates
								.add(new RefactoringState(compilationUnit, compilationUnit.getWorkingCopy(null)));
					}

					/*
					 * If cancel is pressed on progress monitor, abort all and
					 * return, else continue
					 */
					if (subMonitor.isCanceled()) {
						return containingErrorList;
					} else {
						subMonitor.worked(1);
					}
				}

				/**
				 * if there are syntax errors within source files display it to
				 * the user
				 */
				if (!containingErrorList.isEmpty()) {
					logger.info(NLS.bind(ExceptionMessages.RefactoringPipeline_syntax_errors_exist, containingErrorList
							.stream().map(ICompilationUnit::getElementName).collect(Collectors.joining(", ")))); //$NON-NLS-1$

				}
				return containingErrorList;
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
			throw new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
					ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e);
		}
	}

	/**
	 * Apply {@link RefactoringRule}s to the working copies of each
	 * {@link RefactoringState}. Changes are <b>not</b> yet committed.
	 * <p>
	 * All rules that throw an exception are collected and thrown as a
	 * RuleException at the end.
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
	 * @see RefactoringState#addRulesAndGenerateDocumentChanges(List)
	 * 
	 */
	public void doRefactoring(IProgressMonitor monitor) throws RefactoringException, RuleException {
		if (refactoringStates.isEmpty()) {
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
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100).setWorkRemaining(rules.size());
		subMonitor.setTaskName(""); //$NON-NLS-1$

		List<NotWorkingRuleModel> notWorkingRules = new ArrayList<>();
		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> refactoringRule : rules) {

			subMonitor.subTask(refactoringRule.getName());

			/*
			 * Sends new child of subMonitor which takes in progress bar size of
			 * 1 of rules size In method that part of progress bar is split to
			 * number of compilation units
			 */
			applyRuleToAllStates(refactoringRule, true, subMonitor.newChild(1), notWorkingRules);

			/*
			 * If cancel is pressed on progress monitor, abort all and return,
			 * else continue
			 */
			if (subMonitor.isCanceled()) {
				return;
			}
		}

		if (!notWorkingRules.isEmpty()) {
			String notWorkingRulesCollected = NotWorkingRuleModel.asString(notWorkingRules);
			throw new RuleException(
					NLS.bind(ExceptionMessages.RefactoringPipeline_rule_execute_failed, notWorkingRulesCollected),
					NLS.bind(ExceptionMessages.RefactoringPipeline_user_rule_execute_failed, notWorkingRulesCollected));
		}
	}

	/**
	 * Apply {@link RefactoringRule}s to the working copies with changed check
	 * state of each {@link RefactoringState}
	 * 
	 * @param changedCompilationUnits
	 *            unselected compilation units
	 * @param currentRule
	 *            rule for which unselection of units was made
	 * @throws RuleException
	 */
	public void doAdditionalRefactoring(List<ICompilationUnit> changedCompilationUnits,
			RefactoringRule<? extends AbstractASTRewriteASTVisitor> currentRule, IProgressMonitor monitor)
			throws RuleException {
		List<NotWorkingRuleModel> notWorkingRules = new ArrayList<>();

		/*
		 * Converts the monitor to a SubMonitor and sets name of task on
		 * progress monitor dialog Size is set to number 100 and then scaled to
		 * size of the rules list Each refactoring rule increases worked amount
		 * for same size
		 */
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100)
				.setWorkRemaining(rules.size() * changedCompilationUnits.size());
		subMonitor.setTaskName(""); //$NON-NLS-1$

		for (RefactoringState refactoringState : refactoringStates) {
			if (changedCompilationUnits.stream()
					.anyMatch(unit -> unit.getElementName().equals(refactoringState.getWorkingCopyName()))) {
				refactoringState.resetWorkingCopy();
			}
		}

		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> refactoringRule : rules) {
			for (RefactoringState refactoringState : refactoringStates) {
				if (changedCompilationUnits.stream()
						.anyMatch(unit -> unit.getElementName().equals(refactoringState.getWorkingCopyName()))) {
					subMonitor.subTask(refactoringRule.getName() + ": " + refactoringState.getWorkingCopyName()); //$NON-NLS-1$
					if (refactoringRule.equals(currentRule)) {
						refactoringState.addRuleToIgnoredRules(currentRule);
					} else if (!refactoringState.getIgnoredRules().contains(refactoringRule)) {
						try {
							refactoringState.addRuleAndGenerateDocumentChanges(refactoringRule, false);
						} catch (JavaModelException | ReflectiveOperationException | RefactoringException e) {
							logger.error(e.getMessage(), e);
							notWorkingRules.add(new NotWorkingRuleModel(refactoringRule.getName(),
									refactoringState.getWorkingCopyName()));
						}
					}
					if (subMonitor.isCanceled()) {
						return;
					} else {
						subMonitor.worked(1);
					}
				}
			}
		}

		if (!notWorkingRules.isEmpty()) {
			String notWorkingRulesCollected = NotWorkingRuleModel.asString(notWorkingRules);
			throw new RuleException(
					NLS.bind(ExceptionMessages.RefactoringPipeline_rule_execute_failed, notWorkingRulesCollected),
					NLS.bind(ExceptionMessages.RefactoringPipeline_user_rule_execute_failed, notWorkingRulesCollected));
		}
	}

	/**
	 * Immediate refactoring when one working copy is selected from previously
	 * unselected state
	 * 
	 * @param newSelection
	 *            working copy which is newly selected
	 * @param currentRule
	 *            rule for which working copy is selected
	 * @throws RuleException
	 */
	public void refactoringForCurrent(ICompilationUnit newSelection,
			RefactoringRule<? extends AbstractASTRewriteASTVisitor> currentRule) throws RuleException {
		List<NotWorkingRuleModel> notWorkingRules = new ArrayList<>();

		// get the correct RefactoringState
		RefactoringState refactoringState = refactoringStates.stream()
				.filter(s -> newSelection.getElementName().equals(s.getWorkingCopyName())).findFirst().get();

		refactoringState.resetWorkingCopy();

		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> refactoringRule : rules) {
			try {

				if (refactoringRule.equals(currentRule)) {
					refactoringState.removeRuleFromIgnoredRules(currentRule);
				}
				if (!refactoringState.getIgnoredRules().contains(refactoringRule)) {
					refactoringState.addRuleAndGenerateDocumentChanges(refactoringRule, false);
				}

			} catch (JavaModelException | ReflectiveOperationException | RefactoringException e) {
				logger.error(e.getMessage(), e);
				notWorkingRules
						.add(new NotWorkingRuleModel(refactoringRule.getName(), refactoringState.getWorkingCopyName()));
			}
		}

		if (!notWorkingRules.isEmpty()) {
			String notWorkingRulesCollected = NotWorkingRuleModel.asString(notWorkingRules);
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
	 * @throws RefactoringException
	 *             if no working copies were found
	 * @throws ReconcileException
	 *             if a working copy cannot be applied to the underlying
	 *             {@link ICompilationUnit}
	 * 
	 * @since 0.9
	 */
	public void commitRefactoring() throws RefactoringException, ReconcileException {
		if (refactoringStates.isEmpty()) {
			logger.warn(ExceptionMessages.RefactoringPipeline_warn_no_working_copies_found);
			throw new RefactoringException(ExceptionMessages.RefactoringPipeline_warn_no_working_copies_found);
		}
		List<String> refactoringStatesNotCommited = new ArrayList<>();
		for (Iterator<RefactoringState> iterator = refactoringStates.iterator(); iterator.hasNext();) {
			RefactoringState refactoringState = (RefactoringState) iterator.next();
			try {
				refactoringState.commitAndDiscardWorkingCopy();
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
		refactoringStates.forEach(s -> s.discardWorkingCopy());
		refactoringStates.clear();
	}

	/**
	 * Adds a {@link RefactoringRule} to all {@link RefactoringState}s.
	 * <p>
	 * If an Exception occurs while applying a rule to a state, the combination
	 * of rule and state is added to the "not working rules" list and the
	 * refactoring continues.
	 * <p>
	 * This functionality used to be in the {@link RefactoringRule}.
	 * 
	 * @param rule
	 *            {@link RefactoringRule} to apply to all
	 *            {@link RefactoringState} instances
	 * @param initialApply
	 * @param subMonitor
	 * @param returnListNotWorkingRules
	 *            rules that throw an exception are added to this list
	 */
	private void applyRuleToAllStates(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule,
			boolean initialApply, IProgressMonitor subMonitor, List<NotWorkingRuleModel> returnListNotWorkingRules) {

		SubMonitor monitor = SubMonitor.convert(subMonitor).setWorkRemaining(refactoringStates.size());

		for (RefactoringState refactoringState : refactoringStates) {

			subMonitor.subTask(rule.getName() + ": " + refactoringState.getWorkingCopyName()); //$NON-NLS-1$

			try {
				refactoringState.addRuleAndGenerateDocumentChanges(rule, true);
			} catch (JavaModelException | ReflectiveOperationException | RefactoringException e) {
				logger.error(e.getMessage(), e);
				returnListNotWorkingRules
						.add(new NotWorkingRuleModel(rule.getName(), refactoringState.getWorkingCopyName()));
			}

			/*
			 * If cancel is pressed on progress monitor, abort all and return,
			 * else continue
			 */
			if (monitor.isCanceled()) {
				return;
			} else {
				monitor.worked(1);
			}
		}
	}

}
