package eu.jsparrow.ui.preview;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
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
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.rule.impl.unused.RemoveUnusedFieldsRule;
import eu.jsparrow.core.rule.impl.unused.RemoveUnusedMethodsRule;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.statistics.StatisticsSection;
import eu.jsparrow.ui.preview.statistics.StatisticsSectionFactory;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * A wizard for displaying the changes made by rules that remove unused fields, methods, or classes. 
 * 
 * @since 4.8.0
 *
 */
public class RemoveUnusedCodeRulePreviewWizard extends AbstractPreviewWizard {

	private static final Logger logger = LoggerFactory.getLogger(RemoveUnusedCodeRulePreviewWizard.class);

	private RefactoringPipeline refactoringPipeline;
	private List<UnusedClassMemberWrapper> metaData;

	private Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> documentChanges;
	private RemoveUnusedFieldsRule rule;

	private List<ICompilationUnit> targetCompilationUnits;
	private Map<IPath, Document> originalDocuments;
	private RefactoringSummaryWizardPage summaryPage;
	private StatisticsSection statisticsSection;
	private StandaloneStatisticsMetadata standaloneStatisticsMetadata;

	public RemoveUnusedCodeRulePreviewWizard(RefactoringPipeline refactoringPipeline, 
			StandaloneStatisticsMetadata standaloneStatisticsMetadata, 
			List<UnusedClassMemberWrapper> metadata,
			List<UnusedClassMemberWrapper> unusedMethods,
			Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> documentChanges,
			Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> methodDocumentChanges,
			List<ICompilationUnit> targetCompilationUnits, 
			RemoveUnusedFieldsRule rule,
			RemoveUnusedMethodsRule unusedMethodsRule) {
		this.refactoringPipeline = refactoringPipeline;
		this.metaData = metadata;
		this.documentChanges = documentChanges;
		this.targetCompilationUnits = targetCompilationUnits;
		this.originalDocuments = targetCompilationUnits.stream()
			.map(ICompilationUnit::getPrimary)
			.collect(Collectors.toMap(ICompilationUnit::getPath, this::createDocument));
		this.statisticsSection = StatisticsSectionFactory.createStatisticsSectionForSummaryPage(refactoringPipeline);
		this.standaloneStatisticsMetadata = standaloneStatisticsMetadata;
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

		Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> publicChanges = filterChangesByModifier(
				JavaAccessModifier.PUBLIC);
		Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> protectedChanges = filterChangesByModifier(
				JavaAccessModifier.PROTECTED);
		Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> packagePrivateChanges = filterChangesByModifier(
				JavaAccessModifier.PACKAGE_PRIVATE);
		Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> privateChanges = filterChangesByModifier(
				JavaAccessModifier.PRIVATE);

		model.addRule(rule);
		changesPerRule.keySet()
			.stream()
			.forEach(x -> model.addFileToRule(rule, x.getHandleIdentifier()));
		if (!publicChanges.isEmpty()) {
			addPage(new RemoveUnusedCodeRulePreviewWizardPage(publicChanges, originalDocuments, rule, canFinish()));
		}

		if (!protectedChanges.isEmpty()) {
			addPage(new RemoveUnusedCodeRulePreviewWizardPage(protectedChanges, originalDocuments, rule, canFinish()));
		}

		if (!packagePrivateChanges.isEmpty()) {
			addPage(new RemoveUnusedCodeRulePreviewWizardPage(packagePrivateChanges, originalDocuments, rule, canFinish()));
		}

		if (!privateChanges.isEmpty()) {
			addPage(new RemoveUnusedCodeRulePreviewWizardPage(privateChanges, originalDocuments, rule, canFinish()));
		}
		this.summaryPage = new RefactoringSummaryWizardPage(refactoringPipeline, model, canFinish(), standaloneStatisticsMetadata, statisticsSection); 
		addPage(summaryPage);
	}

	private Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> filterChangesByModifier(
			JavaAccessModifier modifier) {
		return documentChanges.entrySet()
			.stream()
			.filter(e -> e.getKey()
				.getAccessModifier()
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
			commitChanges();
		}
		return true;
	}
	
	@Override
	public boolean performCancel() {
		refactoringPipeline.clearStates();
		return super.performCancel();
	}

	@Override
	public void dispose() {
		refactoringPipeline.clearStates();
		super.dispose();
	}

	/**
	 * Checks if license if valid. If it is, changes are committed, otherwise
	 * shows license expired message dialog. If exception occurred while
	 * committing changes, message about exception is displayed.
	 */
	private void commitChanges() {
		try {
			refactoringPipeline.commitRefactoring();
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
				refactoringPipeline.clearStates();
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

	@Override
	public void updateViewsOnNavigation(IWizardPage page) {
		IWizardContainer container = getContainer();
		if (null == container) {
			return;
		}

		if (page instanceof RemoveUnusedCodeRulePreviewWizardPage) {
			RemoveUnusedCodeRulePreviewWizardPage previewPage = (RemoveUnusedCodeRulePreviewWizardPage) page;
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
		if (currentPage instanceof RemoveUnusedCodeRulePreviewWizardPage) {
			((RemoveUnusedCodeRulePreviewWizardPage) currentPage).disposeControl();
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
		getPreviousPage(currentPage);
	}

	public void removeMetaData(UnusedClassMemberWrapper fieldData) {
		this.metaData.remove(fieldData);
	}

	public void addMetaData(UnusedClassMemberWrapper fieldData) {
		this.metaData.add(fieldData);
	}

	public RefactoringSummaryWizardPage getSummaryPage() {
		return this.summaryPage;
	}

}