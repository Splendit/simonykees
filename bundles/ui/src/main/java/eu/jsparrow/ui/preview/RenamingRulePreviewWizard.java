package eu.jsparrow.ui.preview;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.impl.PublicFieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * Wizard that holds {@link RenamingRulePreviewWizardPage} for
 * {@link PublicFieldsRenamingRule}. On Finish it commits all wanted renaming
 * changes to {@link CompilationUnit}s.
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.3.0
 *
 */
public class RenamingRulePreviewWizard extends AbstractPreviewWizard {

	private static final Logger logger = LoggerFactory.getLogger(RenamingRulePreviewWizard.class);
	private RefactoringPipeline refactoringPipeline;
	private List<FieldMetaData> metaData;

	private Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> documentChanges;
	private PublicFieldsRenamingRule rule;

	private List<ICompilationUnit> targetCompilationUnits;
	private Map<IPath, Document> originalDocuments;
	private RenamingRuleSummaryWizardPage summaryPage;

	public RenamingRulePreviewWizard(RefactoringPipeline refactoringPipeline, List<FieldMetaData> metadata,
			Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> documentChanges,
			List<ICompilationUnit> targetCompilationUnits, PublicFieldsRenamingRule rule) {
		this.refactoringPipeline = refactoringPipeline;
		this.metaData = metadata;
		this.documentChanges = documentChanges;
		this.targetCompilationUnits = targetCompilationUnits;
		this.originalDocuments = targetCompilationUnits.stream()
			.map(ICompilationUnit::getPrimary)
			.collect(Collectors.toMap(ICompilationUnit::getPath, this::createDocument));

		this.rule = rule;
		setNeedsProgressMonitor(true);
	}

	private Document createDocument(ICompilationUnit icu) {
		try {
			return new Document(icu.getSource());
		} catch (JavaModelException e1) {
			WizardMessageDialog.synchronizeWithUIShowInfo(
					new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
							ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e1));
			return new Document();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		RefactoringPreviewWizardModel model = new RefactoringPreviewWizardModel();
		Map<ICompilationUnit, DocumentChange> changesPerRule = refactoringPipeline.getChangesForRule(rule);

		Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> publicChanges = filterChangesByModifier(
				JavaAccessModifier.PUBLIC);
		Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> protectedChanges = filterChangesByModifier(
				JavaAccessModifier.PROTECTED);
		Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> packagePrivateChanges = filterChangesByModifier(
				JavaAccessModifier.PACKAGE_PRIVATE);
		Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> privateChanges = filterChangesByModifier(
				JavaAccessModifier.PRIVATE);

		model.addRule(rule);
		changesPerRule.keySet()
			.stream()
			.forEach(x -> model.addFileToRule(rule, x.getHandleIdentifier()));

		if (!publicChanges.isEmpty()) {
			addPage(new RenamingRulePreviewWizardPage(publicChanges, originalDocuments, rule));
		}

		if (!protectedChanges.isEmpty()) {
			addPage(new RenamingRulePreviewWizardPage(protectedChanges, originalDocuments, rule));
		}

		if (!packagePrivateChanges.isEmpty()) {
			addPage(new RenamingRulePreviewWizardPage(packagePrivateChanges, originalDocuments, rule));
		}

		if (!privateChanges.isEmpty()) {
			addPage(new RenamingRulePreviewWizardPage(privateChanges, originalDocuments, rule));
		}
		this.summaryPage = new RenamingRuleSummaryWizardPage(refactoringPipeline, model);
		addPage(summaryPage);
	}

	private Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> filterChangesByModifier(
			JavaAccessModifier modifier) {
		return documentChanges.entrySet()
			.stream()
			.filter(e -> e.getKey()
				.getFieldModifier()
				.equals(modifier))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * If page contains unchecked fields, remove uncheckedFields from metadata,
	 * create and set to refactoringPipeline new RefactoringStates without
	 * unchecked Fields -> doRefactoring -> commitRefactoring. Otherwise just
	 * commit refactoring changes.
	 */
	@Override
	public boolean performFinish() {
		commitChanges();
		return true;
	}

	/**
	 * Checks if license if valid. If it is, changes are committed, otherwise
	 * shows license expired message dialog. If exception occurred while
	 * committing changes, message about exception is displayed.
	 */
	private void commitChanges() {
		if (LicenseUtil.getInstance()
			.isValid()) {
			try {
				refactoringPipeline.commitRefactoring();
				Activator.setRunning(false);
			} catch (RefactoringException | ReconcileException e) {
				WizardMessageDialog.synchronizeWithUIShowError(e);
				Activator.setRunning(false);
			}
		} else {
			WizardMessageDialog.synchronizeWithUIShowLicenseError();
			Activator.setRunning(false);
		}
	}

	/**
	 * Creates a runnable which creates and sets to refactoringPipeline new
	 * RefactoringStates without unchecked Fields. Than calls doRefactoring on
	 * refactoringPipeline to recalculate all changes. At the end it computes
	 * all documentChanges for each field.
	 * 
	 * @return IRunnableWithProgress for recalculation of changes
	 */
	private IRunnableWithProgress createRecalculationJob() {
		return monitor -> {

			List<RefactoringState> refactoringStates = new ArrayList<>();
			if (!createRefactoringStates(refactoringStates)) {
				return;
			}

			refactoringPipeline.setRefactoringStates(refactoringStates);
			refactoringPipeline.updateInitialSourceMap();
			try {
				refactoringPipeline.doRefactoring(monitor);
				if (monitor.isCanceled()) {
					refactoringPipeline.clearStates();
				}
			} catch (RuleException e) {
				logger.error(e.getMessage(), e);
				WizardMessageDialog.synchronizeWithUIShowError(e);
			} catch (RefactoringException e) {
				WizardMessageDialog.synchronizeWithUIShowInfo(e);
				logger.warn(e.getMessage(), e);
			} finally {
				monitor.done();
			}
		};
	}

	/**
	 * Creates refactoring states for all compilation units from
	 * targetCompilationUnits list
	 * 
	 * @param refactoringStates
	 *            result list containing all created refactoring states
	 * @return false if exception occurred, true otherwise
	 */
	private boolean createRefactoringStates(List<RefactoringState> refactoringStates) {
		for (ICompilationUnit compilationUnit : targetCompilationUnits) {
//			try {
//				/*
//				 * TODO IProblemRequestor should be created when creating
//				 * working copy, and working copy owner should be set
//				 */
//				refactoringStates.add(new RefactoringState(compilationUnit, compilationUnit.getWorkingCopy(null), null));
//			} catch (JavaModelException e) {
//				WizardMessageDialog.synchronizeWithUIShowInfo(
//						new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
//								ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e));
//				return false;
//			}
//		}
//		return true;
//	}
//
//	
//	public void createRefactoringStates(List<ICompilationUnit> compilationUnits) {
//		targetCompilationUnits.forEach(compilationUnit -> {

			final ProblemRequestor problemRequestor = new ProblemRequestor();
			final WorkingCopyOwner wcOwner = createWorkingCopyOwner(problemRequestor);

			try {
				ICompilationUnit workingCopy = compilationUnit.getWorkingCopy(wcOwner, null);
				if (((ProblemRequestor) wcOwner.getProblemRequestor(workingCopy)).problems.isEmpty()) {
					refactoringStates.add(new RefactoringState(compilationUnit, workingCopy, wcOwner));
				} else {
					String loggerInfo = NLS.bind(Messages.RefactoringPipeline_CompilationUnitWithCompilationErrors,
							compilationUnit.getElementName(),
							((ProblemRequestor) wcOwner.getProblemRequestor(workingCopy)).problems.get(0));
					logger.info(loggerInfo);
				}
			} catch (JavaModelException e) {
				WizardMessageDialog.synchronizeWithUIShowInfo(
						new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
								ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e));
				return false;

			}
		};
		return true;
	}
	
	private WorkingCopyOwner createWorkingCopyOwner(ProblemRequestor problemRequestor) {
		return new WorkingCopyOwner() {

			@Override
			public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
				return problemRequestor;
			}
		};
	}

	private class ProblemRequestor implements IProblemRequestor {

		private List<IProblem> problems = new ArrayList<>();

		@Override
		public void acceptProblem(IProblem problem) {
			if (problem.isError()) {
				problems.add(problem);
			}
		}

		@Override
		public void beginReporting() {
			// not used
		}

		@Override
		public void endReporting() {
			// not used
		}

		@Override
		public boolean isActive() {
			return true;
		}

	}

	
	
	

	@Override
	public void updateViewsOnNavigation(IWizardPage page) {
		IWizardContainer container = getContainer();
		if (null == container) {
			return;
		}

		if (page instanceof RenamingRulePreviewWizardPage) {
			RenamingRulePreviewWizardPage previewPage = (RenamingRulePreviewWizardPage) page;
			boolean recalculate = previewPage.isRecalculateNeeded();
			if (recalculate) {
				performRecalculation(container);
				previewPage.clearNewSelections();
			}
			/*
			 * if there are no changes in refactoring page, just populate the
			 * view with current updated values
			 */
			previewPage.setSelection();
		}
	}

	private void performRecalculation(IWizardContainer container) {
		IRunnableWithProgress runnable = createRecalculationJob();
		try {
			container.run(true, true, runnable);
		} catch (InvocationTargetException | InterruptedException e1) {
			SimonykeesMessageDialog.openMessageDialog(getShell(),
					Messages.RefactoringPreviewWizard_err_runnableWithProgress, MessageDialog.ERROR);
			Activator.setRunning(false);
		}
	}

	/**
	 * Called from {@link WizardDialog} when Next button is pressed. Triggers
	 * recalculation if needed. Disposes control from current page which wont be
	 * visible any more
	 */
	public void pressedNext() {
		IWizardContainer container = getContainer();
		if (container == null) {
			return;
		}

		IWizardPage currentPage = container.getCurrentPage();
		if (currentPage instanceof RenamingRulePreviewWizardPage) {
			((RenamingRulePreviewWizardPage) currentPage).disposeControl();
		}

		getNextPage(currentPage);
	}

	/**
	 * Called from {@link WizardDialog} when Back button is pressed. Disposes
	 * all controls to be recalculated and created when needed
	 */
	public void pressedBack() {
		IWizardContainer container = getContainer();
		if (container == null) {
			return;
		}

		IWizardPage currentPage = container.getCurrentPage();
		if (currentPage instanceof RefactoringSummaryWizardPage) {
			((RefactoringSummaryWizardPage) currentPage).disposeCompareInputControl();
		}

		getPreviousPage(currentPage);
	}

	public void removeMetaData(FieldMetaData fieldData) {
		this.metaData.remove(fieldData);
	}

	public void addMetaData(FieldMetaData fieldData) {
		this.metaData.add(fieldData);
	}

	public RenamingRuleSummaryWizardPage getSummaryPage() {
		return this.summaryPage;
	}

}
