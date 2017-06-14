package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.exception.SimonykeesException;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerRule;
import at.splendit.simonykees.core.ui.LicenseUtil;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
import at.splendit.simonykees.core.ui.preview.RefactoringPreviewWizard;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * Wizard for configuring logger rule when applying to selected resources
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class LoggerRuleWizard extends Wizard {

	private static final Logger logger = LoggerFactory.getLogger(LoggerRuleWizard.class);

	private LoggerRuleWizardPage page;
	private LoggerRuleWizardPageModel model;
	private LoggerRuleWizardPageControler controler;

	private final List<IJavaElement> javaElements;
	private final StandardLoggerRule rule;

	public LoggerRuleWizard(List<IJavaElement> javaElements,
			RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		super();
		this.javaElements = javaElements;
		this.rule = (StandardLoggerRule) rule;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return Messages.LoggerRuleWizard_title;
	}

	@Override
	public void addPages() {
		model = new LoggerRuleWizardPageModel(rule);
		controler = new LoggerRuleWizardPageControler(model);
		page = new LoggerRuleWizardPage(model, controler);
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (model.getSelectionStatus().equals(Messages.LoggerRuleWizardPageModel_err_noTransformation)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean performFinish() {

		logger.info(NLS.bind(Messages.SelectRulesWizard_start_refactoring, this.getClass().getSimpleName(),
				this.javaElements.get(0).getJavaProject().getElementName()));

		final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = Arrays.asList(rule);
		RefactoringPipeline refactorer = new RefactoringPipeline(rules);
		// AbstractRefactorer refactorer = new AbstractRefactorer(javaElements,
		// rules);
		Rectangle rectangle = Display.getCurrent().getPrimaryMonitor().getBounds();
		rule.setSelectedOptions(model.getCurrentSelectionMap());

		Job job = new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				try {
					refactorer.prepareRefactoring(javaElements, monitor);
					if (monitor.isCanceled()) {
						refactorer.clearStates();
						return Status.CANCEL_STATUS;
					}
				} catch (RefactoringException e) {
					synchronizeWithUIShowInfo(e);
					return Status.CANCEL_STATUS;
				}
				try {
					refactorer.doRefactoring(monitor);
					if (monitor.isCanceled()) {
						refactorer.clearStates();
						return Status.CANCEL_STATUS;
					}
				} catch (RefactoringException e) {
					synchronizeWithUIShowInfo(e);
					return Status.CANCEL_STATUS;
				} catch (RuleException e) {
					synchronizeWithUIShowError(e);
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
						if (refactorer.hasChanges()) {

							synchronizeWithUIShowRefactoringPreviewWizard(refactorer, rectangle);
						} else {

							synchronizeWithUIShowWarningNoRefactoringDialog();
						}
					} else {

						synchronizeWithUIShowLicenseError();
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
	private void synchronizeWithUIShowRefactoringPreviewWizard(RefactoringPipeline refactorer, Rectangle rectangle) {

		logger.info(NLS.bind(Messages.SelectRulesWizard_end_refactoring, this.getClass().getSimpleName(),
				this.javaElements.get(0).getJavaProject().getElementName()));
		logger.info(NLS.bind(Messages.SelectRulesWizard_rules_with_changes,
				javaElements.get(0).getJavaProject().getElementName(), rule.getName()));

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				final WizardDialog dialog = new WizardDialog(shell, new RefactoringPreviewWizard(refactorer));

				// maximizes the RefactoringPreviewWizard
				dialog.setPageSize(rectangle.width, rectangle.height);
				dialog.open();
			}

		});
	}

	/**
	 * Method used to open MessageDialog informing the user that no refactorings
	 * are required from non UI thread
	 */
	private void synchronizeWithUIShowWarningNoRefactoringDialog() {

		logger.info(NLS.bind(Messages.SelectRulesWizard_end_refactoring, this.getClass().getSimpleName(),
				this.javaElements.get(0).getJavaProject()));

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				SimonykeesMessageDialog.openMessageDialog(shell, Messages.SelectRulesWizard_warning_no_refactorings,
						MessageDialog.INFORMATION);

				Activator.setRunning(false);
			}

		});
	}

	/**
	 * Method used to open License ErrorDialog from non UI thread
	 */
	private void synchronizeWithUIShowLicenseError() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				LicenseUtil.getInstance().displayLicenseErrorDialog(shell);

				Activator.setRunning(false);
			}
		});
	}

	/**
	 * Method used to open ErrorDialog from non UI thread
	 */
	private void synchronizeWithUIShowError(SimonykeesException exception) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				SimonykeesMessageDialog.openErrorMessageDialog(shell, exception);

				Activator.setRunning(false);
			}
		});
	}

	/**
	 * Method used to open InformationDialog from non UI thread
	 * RefactoringException is thrown if java element does not exist or if an
	 * exception occurs while accessing its corresponding resource, or if no
	 * working copies were found to apply
	 */
	private void synchronizeWithUIShowInfo(SimonykeesException exception) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				SimonykeesMessageDialog.openMessageDialog(shell, exception.getUiMessage(), MessageDialog.INFORMATION);

				Activator.setRunning(false);
			}
		});
	}
}
