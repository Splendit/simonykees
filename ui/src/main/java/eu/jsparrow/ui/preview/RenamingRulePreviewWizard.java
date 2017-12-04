package eu.jsparrow.ui.preview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.impl.PublicFieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * Wizard that holds {@link RenamingRulePreviewWizardPage} for
 * {@link PublicFieldsRenamingRule}. On Finish it commits all wanted renaming
 * changes to {@link CompilationUnit}s.
 * 
 * @author Andreja Sambolec
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
		RenamingRulePreviewWizardPage page = new RenamingRulePreviewWizardPage(documentChanges, changesPerRule,
				originalDocuments, rule, model);
		addPage(page);
		addSummaryPage(refactoringPipeline, model);
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
	 * Creates job which creates and sets to refactoringPipeline new
	 * RefactoringStates without unchecked Fields. Than calls doRefactoring on
	 * refactoringPipeline to recalculate all changes. At the end it computes
	 * all documentChanges for each field.
	 * 
	 * @return Job for recalculation of changes
	 */
	public Job recalculateForUnselected() {
		return new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				List<RefactoringState> refactoringStates = new ArrayList<>();
				if (!createRefactoringStates(refactoringStates)) {
					return Status.CANCEL_STATUS;
				}

				refactoringPipeline.setRefactoringStates(refactoringStates);
				refactoringPipeline.updateInitialSourceMap();

				try {
					refactoringPipeline.doRefactoring(monitor);
					if (monitor.isCanceled()) {
						refactoringPipeline.clearStates();
						return Status.CANCEL_STATUS;
					}
				} catch (RefactoringException e) {
					WizardMessageDialog.synchronizeWithUIShowInfo(e);
					return Status.CANCEL_STATUS;
				} catch (RuleException e) {
					WizardMessageDialog.synchronizeWithUIShowError(e);
					return Status.CANCEL_STATUS;

				}
				
				return Status.OK_STATUS;
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
			try {
				refactoringStates.add(new RefactoringState(compilationUnit, compilationUnit.getWorkingCopy(null)));
			} catch (JavaModelException e) {
				WizardMessageDialog.synchronizeWithUIShowInfo(
						new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
								ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e));
				return false;
			}
		}
		return true;
	}

	/**
	 * Called from {@link WizardDialog} when Next button is pressed. Triggers
	 * recalculation if needed. Disposes control from current page which wont be
	 * visible any more
	 */
	public void pressedNext() {
		if (null != getContainer()) {
			IWizardPage page = getContainer().getCurrentPage();
			
			if(!(page instanceof RenamingRulePreviewWizardPage)) {
				getNextPage(page);
				return;
			}
			
			RenamingRulePreviewWizardPage previewPage = (RenamingRulePreviewWizardPage) page;
			previewPage.disposeControl();
			
			boolean recalculate = previewPage.isRecalculateNeeded();
			if(!recalculate) {
				getNextPage(page);
				return;
			}

			Job recalculationJob = recalculateForUnselected();
			recalculationJob.setUser(true);
			recalculationJob.schedule();
			
			try {
				recalculationJob.join();
			} catch (InterruptedException e) {
				logger.warn("Recalculation job was interrupted.", e); //$NON-NLS-1$
				Thread.currentThread().interrupt();
			}
			getNextPage(page);

		}
	}

	/**
	 * Called from {@link WizardDialog} when Back button is pressed. Disposes
	 * all controls to be recalculated and created when needed
	 */
	public void pressedBack() {
		if (null != getContainer()) {
			if (getContainer().getCurrentPage() instanceof RefactoringSummaryWizardPage) {
				((RefactoringSummaryWizardPage) getContainer().getCurrentPage()).disposeCompareInputControl();
			}
			getPreviousPage(getContainer().getCurrentPage());
		}
	}

	public void removeMetaData(FieldMetaData fieldData) {
		this.metaData.remove(fieldData);
	}

	public void addMetaData(FieldMetaData fieldData) {
		this.metaData.add(fieldData);
		
	}
}
