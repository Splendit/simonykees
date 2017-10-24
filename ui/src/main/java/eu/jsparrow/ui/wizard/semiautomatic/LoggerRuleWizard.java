package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preview.RefactoringPreviewWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

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

	private IJavaProject selectedJavaProjekt;
	private final StandardLoggerRule rule;

	private RefactoringPipeline refactoringPipeline;

	public LoggerRuleWizard(IJavaProject selectedJavaProjekt,
			RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule, RefactoringPipeline refactoringPipeline) {
		super();
		this.selectedJavaProjekt = selectedJavaProjekt;
		this.refactoringPipeline = refactoringPipeline;
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
		if (model.getSelectionStatus()
			.equals(Messages.LoggerRuleWizardPageModel_err_noTransformation)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean performFinish() {

		logger.info(NLS.bind(Messages.SelectRulesWizard_start_refactoring, this.getClass()
			.getSimpleName(), selectedJavaProjekt.getElementName()));

		final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = Arrays.asList(rule);
		refactoringPipeline.setRules(rules);
		/*
		 * AbstractRefactorer refactorer = new AbstractRefactorer(javaElements,
		 * rules)
		 */
		Rectangle rectangle = Display.getCurrent()
			.getPrimaryMonitor()
			.getBounds();
		rule.activateOptions(model.getCurrentSelectionMap());

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

				if (event.getResult()
					.isOK()) {
					if (refactoringPipeline.hasChanges()) {

						synchronizeWithUIShowRefactoringPreviewWizard(refactoringPipeline, rectangle);
					} else {

						WizardMessageDialog.synchronizeWithUIShowWarningNoRefactoringDialog();
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

		logger.info(NLS.bind(Messages.SelectRulesWizard_end_refactoring, this.getClass()
			.getSimpleName(), selectedJavaProjekt.getElementName()));
		logger.info(NLS.bind(Messages.SelectRulesWizard_rules_with_changes, selectedJavaProjekt.getElementName(),
				rule.getName()));

		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				final WizardDialog dialog = new WizardDialog(shell, new RefactoringPreviewWizard(refactorer));

				// maximizes the RefactoringPreviewWizard
				dialog.setPageSize(rectangle.width, rectangle.height);
				dialog.open();
			});
	}
}
