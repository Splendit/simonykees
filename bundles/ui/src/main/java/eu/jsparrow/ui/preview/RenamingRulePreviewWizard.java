package eu.jsparrow.ui.preview;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.FieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.statistics.StatisticsSection;
import eu.jsparrow.ui.preview.statistics.StatisticsSectionFactory;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * Wizard that holds {@link RenamingRulePreviewWizardPage} for
 * {@link FieldsRenamingRule}. On Finish it commits all wanted renaming changes
 * to {@link CompilationUnit}s.
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.3.0
 *
 */
public class RenamingRulePreviewWizard extends AbstractPreviewWizard {

	private static final Logger logger = LoggerFactory.getLogger(RenamingRulePreviewWizard.class);

	private List<FieldMetaData> metaData;

	private Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> documentChanges;
	private FieldsRenamingRule rule;

	private List<ICompilationUnit> targetCompilationUnits;
	private Map<IPath, Document> originalDocuments;
	private RenamingRuleSummaryWizardPage summaryPage;
	private StatisticsSection statisticsSection;
	private LicenseUtil licenseUtil = LicenseUtil.get();

	public RenamingRulePreviewWizard(RefactoringPipeline refactoringPipeline, List<FieldMetaData> metadata,
			Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> documentChanges,
			List<ICompilationUnit> targetCompilationUnits, FieldsRenamingRule rule) {
		super(refactoringPipeline);
		this.metaData = metadata;
		this.documentChanges = documentChanges;
		this.targetCompilationUnits = targetCompilationUnits;
		this.originalDocuments = targetCompilationUnits.stream()
			.map(ICompilationUnit::getPrimary)
			.collect(Collectors.toMap(ICompilationUnit::getPath, this::createDocument));
		this.statisticsSection = StatisticsSectionFactory.createStatisticsSectionForSummaryPage(refactoringPipeline);

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
		Map<ICompilationUnit, DocumentChange> changesPerRule = getChangesForRule(rule);

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
			addPage(new RenamingRulePreviewWizardPage(publicChanges, originalDocuments, rule, canFinish()));
		}

		if (!protectedChanges.isEmpty()) {
			addPage(new RenamingRulePreviewWizardPage(protectedChanges, originalDocuments, rule, canFinish()));
		}

		if (!packagePrivateChanges.isEmpty()) {
			addPage(new RenamingRulePreviewWizardPage(packagePrivateChanges, originalDocuments, rule, canFinish()));
		}

		if (!privateChanges.isEmpty()) {
			addPage(new RenamingRulePreviewWizardPage(privateChanges, originalDocuments, rule, canFinish()));
		}
		this.summaryPage = new RenamingRuleSummaryWizardPage(refactoringPipeline, model, canFinish(),
				statisticsSection);
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
		IWizardContainer container = getContainer();
		if (container != null) {
			IWizardPage currentPage = container.getCurrentPage();
			updateViewsOnNavigation(currentPage);
			if (!refactoringPipeline.hasAnyValidChange()) {
				showNoChangeFoundToCommit();
				return false;
			}
			commitChanges();
		}
		return true;
	}

	@Override
	public boolean performCancel() {
		clearRefactoringPipelineState();
		return super.performCancel();
	}

	@Override
	public void dispose() {
		clearRefactoringPipelineState();
		super.dispose();
	}

	/**
	 * Checks if license if valid. If it is, changes are committed, otherwise
	 * shows license expired message dialog. If exception occurred while
	 * committing changes, message about exception is displayed.
	 */
	private void commitChanges() {
		updateContainerOnCommit();
		try {
			refactoringPipeline.commitRefactoring();
			int sum = payPerUseCalculator.findTotalRequiredCredit(refactoringPipeline.getRules());
			licenseUtil.reserveQuantity(sum);
			showSuccessfulCommitMessage();
			Activator.setRunning(false);
		} catch (RefactoringException | ReconcileException e) {
			WizardMessageDialog.synchronizeWithUIShowError(e);
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
			try {
				/*
				 * Create refactoring states for all compilation units from
				 * targetCompilationUnits list
				 */
				clearRefactoringPipelineState();
				refactoringPipeline.createRefactoringStates(targetCompilationUnits);
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), e);
				WizardMessageDialog.synchronizeWithUIShowInfo(
						new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
								ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e));
				return;
			}
			refactoringPipeline.updateInitialSourceMap();
			try {
				refactoringPipeline.doRefactoring(monitor);
				this.statisticsSection.updateForSelected();
				if (monitor.isCanceled()) {
					clearRefactoringPipelineState();
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
	@Override
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
	@Override
	public void pressedBack() {
		IWizardContainer container = getContainer();
		if (container == null) {
			return;
		}

		IWizardPage currentPage = container.getCurrentPage();
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

	@Override
	protected boolean needsSummaryPage() {
		return true;
	}

	@Override
	public void showSummaryPage() {
		/*
		 * If summary button is pressed on any page that is not Summary page,
		 * views have to be check for change and updated, and preview control
		 * has to be disposed on current page. If it is already on Summary page,
		 * just refresh.
		 */
		if (getContainer().getCurrentPage() instanceof RenamingRulePreviewWizardPage) {
			updateViewsOnNavigation(getContainer().getCurrentPage());
			((RenamingRulePreviewWizardPage) getContainer().getCurrentPage()).disposeControl();
		}
		getContainer().showPage(getSummaryPage());
	}

	@Override
	protected boolean canFinishWithFreeLicense() {
		return false;
	}
}
