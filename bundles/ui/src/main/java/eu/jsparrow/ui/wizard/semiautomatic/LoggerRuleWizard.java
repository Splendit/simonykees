package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preview.RefactoringPreviewWizard;
import eu.jsparrow.ui.util.ResourceHelper;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * Wizard for configuring logger rule when applying to selected resources
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class LoggerRuleWizard extends AbstractRuleWizard {

	private static final Logger logger = LoggerFactory.getLogger(LoggerRuleWizard.class);

	private static final String WINDOW_ICON = "icons/jsparrow-marker-003.png"; //$NON-NLS-1$

	private LoggerRuleWizardPageModel model;

	private IJavaProject selectedJavaProjekt;
	private final StandardLoggerRule rule;

	private RefactoringPipeline refactoringPipeline;

	public LoggerRuleWizard(IJavaProject selectedJavaProjekt, RefactoringRule rule,
			RefactoringPipeline refactoringPipeline) {
		super();
		this.selectedJavaProjekt = selectedJavaProjekt;
		this.refactoringPipeline = refactoringPipeline;
		this.rule = (StandardLoggerRule) rule;
		setNeedsProgressMonitor(true);
		WizardDialog.setDefaultImage(ResourceHelper.createImage(WINDOW_ICON));
	}

	@Override
	public String getWindowTitle() {
		return Messages.LoggerRuleWizard_title;
	}

	@Override
	public void addPages() {
		model = new LoggerRuleWizardPageModel(rule);
		addPage(new LoggerRuleWizardPage(model));
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		return (!model.getSelectionStatus()
			.equals(Messages.LoggerRuleWizardPageModel_err_noTransformation));
	}

	@Override
	public boolean performFinish() {

		String bind = NLS.bind(Messages.SelectRulesWizard_start_refactoring, this.getClass()
			.getSimpleName(), selectedJavaProjekt.getElementName());
		logger.info(bind);

		final List<RefactoringRule> rules = Arrays.asList(rule);
		refactoringPipeline.setRules(rules);

		Rectangle rectangle = Display.getCurrent()
			.getPrimaryMonitor()
			.getBounds();
		rule.activateOptions(model.getCurrentSelectionMap());

		Job job = new Job(Messages.ProgressMonitor_calculating_possible_refactorings) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				preRefactoring();
				IStatus refactoringStatus = doRefactoring(monitor, refactoringPipeline);
				postRefactoring();

				return refactoringStatus;
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
		String messageEndRefactoring = NLS.bind(Messages.SelectRulesWizard_end_refactoring, this.getClass()
			.getSimpleName(), selectedJavaProjekt.getElementName());
		logger.info(messageEndRefactoring);

		String messageRulesWithChanges = NLS.bind(Messages.SelectRulesWizard_rules_with_changes,
				selectedJavaProjekt.getElementName(), rule.getRuleDescription()
					.getName());
		logger.info(messageRulesWithChanges);

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
