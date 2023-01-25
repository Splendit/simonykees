package eu.jsparrow.core.refactorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.exception.model.NotWorkingRuleModel;
import eu.jsparrow.core.rule.RulesForProjectsData;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.util.GeneratedNodesUtil;
import eu.jsparrow.rules.common.util.JdtCoreVersionBindingUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;

/**
 * This class manages the selected {@link RefactoringRule}s and the selected
 * {@link IJavaElement}s and offers functionality to apply the first to the
 * latter.
 * 
 * @author Ludwig Werzowa, Andreja Sambolec, Matthias Webhofer
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
	private List<RefactoringRule> rules;

	/**
	 * Holder map for original source code, used for summary page
	 */
	private Map<RefactoringState, String> initialSource = new HashMap<>();

	private boolean multipleProjects = false;

	private WorkingCopyOwnerDecorator workingCopyOwner;

	private int fileCount;

	/**
	 * If possible, then a new instance of {@link RefactoringPipeline} is
	 * created and a wizard to select rules for refactoring is opened. In all
	 * cases, the state of the old {@link RefactoringPipeline} is cleared.
	 * 
	 * @param refactoringPipeline
	 *            cannot be reused any more after invocation of this method
	 *            because its state is cleared and therefore important
	 *            informations for its usage are removed.
	 * @param showSelectRulesWizardLambda
	 *            specifies the method to open a wizard to select rules for
	 *            refactoring.
	 * @return true if it is possible to open a wizard for selecting rules,
	 *         otherwise false.
	 */
	public static boolean showSelectRulesWithNewPipeline(RefactoringPipeline refactoringPipeline,
			RulesForProjectsData dataForSelectRulesWizard,
			BiConsumer<RefactoringPipeline, RulesForProjectsData> showSelectRulesWizardLambda) {
		refactoringPipeline.refactoringStates.forEach(RefactoringState::resetAll);
		refactoringPipeline.initialSource.clear();
		RefactoringPipeline newRefactoringPipeline = new RefactoringPipeline();
		newRefactoringPipeline.refactoringStates.addAll(refactoringPipeline.refactoringStates);
		newRefactoringPipeline.fileCount = refactoringPipeline.fileCount;
		refactoringPipeline.refactoringStates.clear();
		refactoringPipeline.clearStates();
		showSelectRulesWizardLambda.accept(newRefactoringPipeline, dataForSelectRulesWizard);
		return true;
	}

	/**
	 * Constructor without parameters, used to create RefactoringPipeline before
	 * SelectRulesWizard is opened
	 */
	public RefactoringPipeline() {
		this.refactoringStates = new ArrayList<>();
		this.workingCopyOwner = new WorkingCopyOwnerDecorator();
	}

	/**
	 * Stores the selected rules.
	 * 
	 * @param rules
	 *            {@link List} of {@link RefactoringRule}s to apply to the
	 *            selected {@link IJavaElement}s
	 */
	public RefactoringPipeline(List<RefactoringRule> rules) {

		/*
		 * Note: We cannot immediately call prepareRefactoring because we need
		 * to call prepareRefactoring in the SelectRulesWizard when finishing
		 * (in a Job) but we need the RefactoringPipeline instance outside of
		 * the Job. Since the Job needs the pipeline to be
		 * "final or effectively final", the constructor has to be called
		 * outside of the Job. Plus we only know the list of rules when
		 * finishing.
		 */
		this();
		this.rules = rules;
	}

	public List<RefactoringRule> getRules() {
		return rules;
	}

	/**
	 * Setter for rules when finish button is pressed in SelectRulesWizard to
	 * store selected rules
	 * 
	 * @param rules
	 *            selected rules
	 */
	public void setRules(List<RefactoringRule> rules) {
		this.rules = rules;
	}

	public Map<ICompilationUnit, DocumentChange> getChangesForRule(RefactoringRule rule) {
		Map<ICompilationUnit, DocumentChange> currentChanges = new HashMap<>();

		refactoringStates.forEach(refactoringState -> {
			DocumentChange documentChange = refactoringState.getChangeIfPresent(rule);
			if (null != documentChange) {
				currentChanges.put(refactoringState.getWorkingCopy(), documentChange);
			} else if (refactoringState.wasChangeInitialyPresent(rule)) {
				currentChanges.put(refactoringState.getWorkingCopy(), null);
			}
		});

		return currentChanges;
	}

	/**
	 * this is a helper method mainly for logging purposes.
	 * 
	 * @return a semicolon-separated string with all rules, that have changes
	 */
	public String getRulesWithChangesAsString() {
		return this.getRules()
			.stream()
			.filter(rule -> null != this.getChangesForRule(rule) && !this.getChangesForRule(rule)
				.isEmpty())
			.map(RefactoringRule::toString)
			.collect(Collectors.joining(", ")); //$NON-NLS-1$
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
		return refactoringStates.stream()
			.anyMatch(RefactoringState::hasChange);
	}

	/**
	 * When prepare refactoring is finished it can happen that there is no
	 * refactoringStates if all selected classes have compilation errors.
	 * 
	 * @return true if has at least one refactoring state, false otherwise
	 */
	public boolean hasRefactoringStates() {
		return !refactoringStates.isEmpty();
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
	public List<ICompilationUnit> prepareRefactoring(List<ICompilationUnit> compilationUnits, IProgressMonitor monitor)
			throws RefactoringException {
		this.fileCount = compilationUnits.size();
		List<ICompilationUnit> containingErrorList = new ArrayList<>();

		try {
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
				SubMonitor subMonitor = SubMonitor.convert(monitor, 100)
					.setWorkRemaining(compilationUnits.size());
				subMonitor.setTaskName(""); //$NON-NLS-1$

				for (ICompilationUnit compilationUnit : compilationUnits) {
					subMonitor.subTask(compilationUnit.getElementName());

					createRefactoringState(compilationUnit, containingErrorList);

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
					String loggerInfo = NLS.bind(ExceptionMessages.RefactoringPipeline_syntax_errors_exist,
							containingErrorList.stream()
								.map(ICompilationUnit::getElementName)
								.collect(Collectors.joining(", "))); //$NON-NLS-1$
					logger.info(loggerInfo);

				}
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
			throw new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
					ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e);
		}
		return containingErrorList;
	}

	/**
	 * Creates {@link RefactoringState}s for all provided
	 * {@link ICompilationUnit}s without compilation error. Returns list of
	 * {@link ICompilationUnit}s that were containing compilation error.
	 * 
	 * @param compilationUnits
	 *            list of {@link ICompilationUnit}s for which
	 *            {@link RefactoringState}s should be created
	 * @return list of {@link ICompilationUnit}s that contain compilation error
	 * @throws JavaModelException
	 */
	public List<ICompilationUnit> createRefactoringStates(List<ICompilationUnit> compilationUnits)
			throws JavaModelException {
		this.fileCount = compilationUnits.size();
		List<ICompilationUnit> containingErrorList = new ArrayList<>();

		for (ICompilationUnit compilationUnit : compilationUnits) {
			createRefactoringState(compilationUnit, containingErrorList);
		}

		return containingErrorList;
	}

	/**
	 * Creates instance of {@link RefactoringState} for provided
	 * {@link ICompilationUnit} if it doesn't contain compilation errors.
	 * Otherwise it adds {@link ICompilationUnit} with compilation error to
	 * containingErrorList.
	 * 
	 * @param compilationUnit
	 *            {@link ICompilationUnit} instance for which
	 *            {@link RefactoringState} instance should be created
	 * @param containingErrorList
	 *            list containing all {@link ICompilationUnit}s with compilation
	 *            error
	 * @throws JavaModelException
	 *             if the content of the provided {@link ICompilationUnit}
	 *             cannot be determined.
	 */
	public void createRefactoringState(ICompilationUnit compilationUnit, List<ICompilationUnit> containingErrorList)
			throws JavaModelException {
		ICompilationUnit workingCopy = compilationUnit.getWorkingCopy(workingCopyOwner, null);
		IProblemRequestor problemRequestor = workingCopyOwner.getProblemRequestor(workingCopy);
		List<IProblem> problems = ((ProblemRequestor) problemRequestor).getProblems();
		if (problems.isEmpty()) {
			refactoringStates.add(new RefactoringState(compilationUnit, workingCopy, workingCopyOwner));
		} else {
			String loggerInfo = NLS.bind(Messages.RefactoringPipeline_CompilationUnitWithCompilationErrors,
					compilationUnit.getElementName(), problems.get(0));
			logger.warn(loggerInfo);
			containingErrorList.add(compilationUnit);
		}
	}

	public boolean isMultipleProjects() {
		return multipleProjects;
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

		// When starting a new refactoring clear the old application counters
		RuleApplicationCount.clear();

		/*
		 * Converts the monitor to a SubMonitor and sets name of task on
		 * progress monitor dialog Size is set to number 100 and then scaled to
		 * size of the rules list Each refactoring rule increases worked amount
		 * for same size
		 */
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100)
			.setWorkRemaining(refactoringStates.size());
		subMonitor.setTaskName(""); //$NON-NLS-1$

		List<NotWorkingRuleModel> notWorkingRules = new ArrayList<>();
		for (RefactoringState state : refactoringStates) {
			subMonitor.subTask(state.getWorkingCopyName());

			/*
			 * Sends new child of subMonitor which takes in progress bar size of
			 * 1 of rules size In method that part of progress bar is split to
			 * number of compilation units
			 */
			applyRulesToRefactoringState(state, subMonitor.newChild(1), notWorkingRules);

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
	public void doAdditionalRefactoring(List<ICompilationUnit> changedCompilationUnits, RefactoringRule currentRule,
			IProgressMonitor monitor) throws RuleException {
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

		List<RefactoringState> changedRefactoringStates = refactoringStates.stream()
			.filter(refactoringState -> changedCompilationUnits.stream()
				.anyMatch(unit -> unit.getElementName()
					.equals(refactoringState.getWorkingCopyName())))
			.collect(Collectors.toList());

		changedRefactoringStates.forEach(RefactoringState::resetWorkingCopy);

		for (RefactoringState refactoringState : changedRefactoringStates) {
			CompilationUnit astRoot = RefactoringUtil.parse(refactoringState.getWorkingCopy());
			List<RefactoringRule> ignoredRules = refactoringState.getIgnoredRules();
			for (RefactoringRule rule : rules) {
				subMonitor.subTask(rule.getRuleDescription()
					.getName() + ": " + refactoringState.getWorkingCopyName()); //$NON-NLS-1$
				if (rule.equals(currentRule)) {
					refactoringState.addRuleToIgnoredRules(currentRule);
				} else if (!ignoredRules.contains(rule)) {
					GeneratedNodesUtil.removeAllGeneratedNodes(astRoot);
					astRoot = applyToRefactoringState(refactoringState, notWorkingRules, astRoot, rule, false);
				}
				if (subMonitor.isCanceled()) {
					return;
				} else {
					subMonitor.worked(1);
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
	public void refactoringForCurrent(ICompilationUnit newSelection, RefactoringRule currentRule) throws RuleException {
		List<NotWorkingRuleModel> notWorkingRules = new ArrayList<>();

		// get the correct RefactoringState
		RefactoringState refactoringState = refactoringStates.stream()
			.filter(s -> newSelection.getElementName()
				.equals(s.getWorkingCopyName()))
			.findFirst()
			.orElseThrow(RuleException::new);

		refactoringState.resetWorkingCopy();

		CompilationUnit astRoot = RefactoringUtil.parse(refactoringState.getWorkingCopy());
		List<RefactoringRule> ignoredRules = refactoringState.getIgnoredRules();
		for (RefactoringRule refactoringRule : rules) {

			if (refactoringRule.equals(currentRule)) {
				refactoringState.removeRuleFromIgnoredRules(currentRule);
			}
			if (!ignoredRules.contains(refactoringRule)) {
				GeneratedNodesUtil.removeAllGeneratedNodes(astRoot);
				astRoot = applyToRefactoringState(refactoringState, notWorkingRules, astRoot, refactoringRule, false);
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
		List<RefactoringStateNotCommited> refactoringStatesNotCommited = new LinkedList<>();
		for (Iterator<RefactoringState> iterator = refactoringStates.iterator(); iterator.hasNext();) {
			RefactoringState refactoringState = iterator.next();
			try {
				refactoringState.commitAndDiscardWorkingCopy();
				iterator.remove();
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), e);
				refactoringStatesNotCommited.add(new RefactoringStateNotCommited(refactoringState.getWorkingCopy()
					.getPath()
					.toString(), e));
			}
		}
		if (!refactoringStatesNotCommited.isEmpty()) {
			String notWorkingRulesCollected = refactoringStatesNotCommited.stream()
				.map(Object::toString)
				.collect(Collectors.joining("\n")); //$NON-NLS-1$
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
		refactoringStates.forEach(RefactoringState::discardWorkingCopy);
		refactoringStates.clear();
		initialSource.clear();
	}

	/**
	 * Adds all {@link RefactoringRule}s to a {@link RefactoringState}.
	 * <p>
	 * If an Exception occurs while applying a rule to a state, the combination
	 * of rule and state is added to the "not working rules" list and the
	 * refactoring continues.
	 * <p>
	 * 
	 * @param refactoringState
	 *            {@link RefactoringState} instance to which all rules should be
	 *            applied
	 * @param subMonitor
	 * @param returnListNotWorkingRules
	 *            rules that throw an exception are added to this list
	 */
	private void applyRulesToRefactoringState(RefactoringState refactoringState, IProgressMonitor subMonitor,
			List<NotWorkingRuleModel> returnListNotWorkingRules) {

		SubMonitor monitor = SubMonitor.convert(subMonitor)
			.setWorkRemaining(refactoringStates.size());

		CompilationUnit astRoot = RefactoringUtil.parse(refactoringState.getWorkingCopy());

		for (RefactoringRule rule : rules) {
			subMonitor.subTask(rule.getRuleDescription()
				.getName() + ": " + refactoringState.getWorkingCopyName()); //$NON-NLS-1$

			GeneratedNodesUtil.removeAllGeneratedNodes(astRoot);
			astRoot = applyToRefactoringState(refactoringState, returnListNotWorkingRules, astRoot, rule, true);

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

	private CompilationUnit applyToRefactoringState(RefactoringState refactoringState,
			List<NotWorkingRuleModel> returnListNotWorkingRules, CompilationUnit astRoot, RefactoringRule rule,
			boolean initialApply) {
		CompilationUnit newAstRoot = astRoot;

		try {
			boolean hasChanges = refactoringState.addRuleAndGenerateDocumentChanges(rule, newAstRoot, initialApply);
			if (hasChanges) {
				Version jdtVersion = JdtCoreVersionBindingUtil.findCurrentJDTCoreVersion();
				ICompilationUnit workingCopy = refactoringState.getWorkingCopy();
				newAstRoot = workingCopy.reconcile(JdtCoreVersionBindingUtil.findJLSLevel(jdtVersion), true, null,
						null);
			}
		} catch (JavaModelException | ReflectiveOperationException | RefactoringException e) {
			logger.error(e.getMessage(), e);
			returnListNotWorkingRules.add(new NotWorkingRuleModel(rule.getRuleDescription()
				.getName(), refactoringState.getWorkingCopyName()));
		}
		return newAstRoot;
	}

	public void updateInitialSourceMap() {
		Map<RefactoringState, String> sourceMap = getInitialSourceMap();
		sourceMap.clear();
		putAllRefactoringStateSources(sourceMap);
	}

	/**
	 * Method for creating Map with relation from {@link RefactoringState} to
	 * current source code
	 * 
	 */
	public void putAllRefactoringStateSources(Map<RefactoringState, String> sourceMap) {
		refactoringStates.stream()
			.forEach(refactoringState -> {
				try {
					sourceMap.put(refactoringState, refactoringState.getWorkingCopy()
						.getSource());
				} catch (JavaModelException e) {
					logger.error(e.getMessage(), e);
				}
			});
	}

	/**
	 * This method is intended to be called before re-using the same
	 * {@link RefactoringPipeline} instance to open a wizard for selecting rules
	 * after having cancelled the refactoring preview wizard.
	 */
	public void cancelFileChanges() {
		refactoringStates.forEach(RefactoringState::resetAll);
		initialSource.clear();
	}

	/**
	 * Getter for map with original source code for all refactoring states
	 * 
	 * @return
	 */
	public Map<RefactoringState, String> getInitialSourceMap() {
		return initialSource;
	}

	/**
	 * Getter for refactoring states, used to remove all files without any
	 * change from summary page
	 * 
	 * @return
	 */
	public List<RefactoringState> getRefactoringStates() {
		return refactoringStates;
	}

	public int getFileCount() {
		return fileCount;
	}
}
