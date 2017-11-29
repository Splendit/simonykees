package eu.jsparrow.ui.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.impl.PublicFieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldMetadata;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
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
public class RenamingRulePreviewWizard extends Wizard {

	private RefactoringPipeline refactoringPipeline;
	private List<FieldMetadata> metadata;

	private Map<FieldMetadata, Map<ICompilationUnit, DocumentChange>> documentChanges;
	private PublicFieldsRenamingRule rule;

	private List<ICompilationUnit> targetCompilationUnits;

	public RenamingRulePreviewWizard(RefactoringPipeline refactoringPipeline, List<FieldMetadata> metadata,
			Map<FieldMetadata, Map<ICompilationUnit, DocumentChange>> documentChanges,
			List<ICompilationUnit> targetCompilationUnits, PublicFieldsRenamingRule rule) {
		this.refactoringPipeline = refactoringPipeline;
		this.metadata = metadata;
		this.documentChanges = documentChanges;
		this.targetCompilationUnits = targetCompilationUnits;
		this.rule = rule;
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		addPage(new RenamingRulePreviewWizardPage(documentChanges, rule));
	}

	/**
	 * If page contains unchecked fields, remove uncheckedFields from metadata,
	 * create and set to refactoringPipeline new RefactoringStates without
	 * unchecked Fields -> doRefactoring -> commitRefactoring. Otherwise just
	 * commit refactoring changes.
	 */
	@Override
	public boolean performFinish() {
		if (!((RenamingRulePreviewWizardPage) getPage(rule.getRuleDescription()
			.getName())).getUncheckedFields()
				.isEmpty()) {
			for (FieldMetadata fieldData : ((RenamingRulePreviewWizardPage) getPage(rule.getRuleDescription()
				.getName())).getUncheckedFields()) {
				metadata.remove(fieldData);
			}

			Job recalculationJob = recalculateForUnselected();

			recalculationJob.setUser(true);
			recalculationJob.schedule();

			recalculationJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					commitChanges();
				}
			});
		} else {
			commitChanges();
		}
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
	private Job recalculateForUnselected() {
		return new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				List<RefactoringState> refactoringStates = new ArrayList<>();
				if (!createRefactoringStates(refactoringStates)) {
					return Status.CANCEL_STATUS;
				}

				refactoringPipeline.setRefactoringStates(refactoringStates);

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

				if (refactoringPipeline.hasChanges()) {
					Map<FieldMetadata, Map<ICompilationUnit, DocumentChange>> changes = new HashMap<>();
					Map<String, FieldMetadata> metaDataMap = new HashMap<>();
					for (FieldMetadata data : metadata) {

						String newIdentifier = data.getNewIdentifier();
						data.getCompilationUnit()
							.getJavaElement();
						Map<ICompilationUnit, DocumentChange> docsChanges = rule.computeDocumentChangesPerFiled(data);
						changes.put(data, docsChanges);
						metaDataMap.put(newIdentifier, data);

					}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (!LicenseUtil.getInstance()
			.isFullLicense()) {
			return false;
		}
		return super.canFinish();
	}
}
