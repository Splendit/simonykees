package at.splendit.simonykees.core.ui.wizard.impl;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.LicenseUtil;
import at.splendit.simonykees.core.ui.preview.RefactoringPreviewWizard;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * {@link Wizard} holding the {@link AbstractSelectRulesWizardPage}, which
 * contains a list of all selectable rules.
 * 
 * Clicking the OK button either calls the {@link RefactoringPreviewWizard} (if
 * there are changes within the code for the selected rules), or a
 * {@link MessageDialog} informing the user that there are no changes.
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa, Martin Huter, Andreja Sambolec
 * @since 0.9
 */
public class SelectRulesWizard extends Wizard {

	private AbstractSelectRulesWizardPage page;
	private SelectRulesWizardPageControler controler;
	private SelectRulesWizardPageModel model;

	private final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules;
	
	private RefactoringPipeline refactoringPipeline;

	public SelectRulesWizard(RefactoringPipeline refactoringPipeline, 
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) {
		super();
		this.refactoringPipeline = refactoringPipeline;
		this.rules = rules;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return Messages.SelectRulesWizard_title;
	}

	@Override
	public void addPages() {
		model = new SelectRulesWizardPageModel(rules);
		controler = new SelectRulesWizardPageControler(model);
		page = new SelectRulesWizardPage(model, controler);
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (model.getSelectionAsList().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean performFinish() {

		final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = model.getSelectionAsList();

		refactoringPipeline.setRules(rules);
		
		Rectangle rectangle = Display.getCurrent().getPrimaryMonitor().getBounds();

		Job job = new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
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

				} finally {
					monitor.done();
				}

				return Status.OK_STATUS;
			}
		};

		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult().isOK()) {
					if (LicenseUtil.getInstance().isValid()) {
						if (refactoringPipeline.hasChanges()) {
							synchronizeWithUIShowRefactoringPreviewWizard(refactoringPipeline, rectangle);
						} else {
							WizardMessageDialog.synchronizeWithUIShowWarningNoRefactoringDialog();
						}
					} else {

						WizardMessageDialog.synchronizeWithUIShowLicenseError();
					}
				} else {
					// do nothing if status is canceled, close
					Activator.setRunning(false);
				}
			}
		});

		job.setUser(true);
		job.schedule();

		return true;
	}

	/**
	 * Method used to open RefactoringPreviewWizard from non UI thread
	 */
	private void synchronizeWithUIShowRefactoringPreviewWizard(RefactoringPipeline refactoringPipeline,
			Rectangle rectangle) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				final WizardDialog dialog = new WizardDialog(shell, new RefactoringPreviewWizard(refactoringPipeline)) {

					@Override
					protected void nextPressed() {
						((RefactoringPreviewWizard) getWizard()).pressedNext();
						super.nextPressed();
					}

					@Override
					protected void backPressed() {
						((RefactoringPreviewWizard) getWizard()).pressedBack();
						super.backPressed();
					}

				};

				// maximizes the RefactoringPreviewWizard
				dialog.setPageSize(rectangle.width, rectangle.height);
				dialog.open();
			}

		});
	}
}
