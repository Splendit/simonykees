package eu.jsparrow.ui.wizard;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.statistic.StopWatchUtil;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.PreviewWizardDialog;
import eu.jsparrow.ui.preview.RefactoringPreviewWizard;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizardData;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * A parent for all rule wizards.
 * 
 * @author Ardit Ymeri
 * @since 2.3.1
 *
 */
public abstract class AbstractRuleWizard extends Wizard {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRuleWizard.class);
	private StandaloneStatisticsMetadata statisticsMetadata;

	protected void preRefactoring() {
		StopWatchUtil.start();
	}

	protected void postRefactoring() {
		StopWatchUtil.stop();
	}

	protected IStatus doRefactoring(IProgressMonitor monitor, RefactoringPipeline refactoringPipeline) {
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

	protected StandaloneStatisticsMetadata prepareStatisticsMetadata(Collection<IJavaProject> javaProjects) {

		String repoName = javaProjects.stream()
			.map(IJavaProject::getElementName)
			.collect(Collectors.joining(";")); //$NON-NLS-1$

		return new StandaloneStatisticsMetadata(Instant.now()
			.getEpochSecond(), "Splendit-Internal-Measurement", repoName); //$NON-NLS-1$
	}

	protected Job createRefactoringJob(RefactoringPipeline refactoringPipeline, Collection<IJavaProject> javaProjects) {
		return new Job(Messages.ProgressMonitor_calculating_possible_refactorings) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				statisticsMetadata = prepareStatisticsMetadata(javaProjects);

				preRefactoring();
				IStatus refactoringStatus = doRefactoring(monitor, refactoringPipeline);
				postRefactoring();

				return refactoringStatus;
			}
		};
	}

	private void showRefactoringPreviewWizard(RefactoringPipeline refactoringPipeline,
			Collection<IJavaProject> javaProjects, SelectRulesWizardData selectRulesWizardData) {
		String endRefactoringInProject = NLS.bind(Messages.SelectRulesWizard_end_refactoring,
				this.getClass()
					.getSimpleName(),
				javaProjects.stream()
					.map(IJavaProject::getElementName)
					.collect(Collectors.joining(";")));//$NON-NLS-1$
		logger.info(endRefactoringInProject);
		String ruleWithChanges = NLS.bind(Messages.SelectRulesWizard_rules_with_changes,
				javaProjects.stream()
					.map(IJavaProject::getElementName)
					.collect(Collectors.joining(";")), //$NON-NLS-1$
				refactoringPipeline.getRulesWithChangesAsString());
		logger.info(ruleWithChanges);

		Shell shell = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow()
			.getShell();
		RefactoringPreviewWizard previewWizard = new RefactoringPreviewWizard(refactoringPipeline,
				statisticsMetadata, selectRulesWizardData);

		Rectangle rectangle = Display.getCurrent()
			.getPrimaryMonitor()
			.getBounds();

		final PreviewWizardDialog dialog = new PreviewWizardDialog(shell, previewWizard);

		// maximizes the RefactoringPreviewWizard
		dialog.setPageSize(rectangle.width, rectangle.height);
		dialog.open();
	}

	protected JobChangeAdapter createPreviewWizardJobChangeAdapter(RefactoringPipeline refactoringPipeline,
			Collection<IJavaProject> javaProjects, SelectRulesWizardData selectRulesWizardData) {
		return new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult()
					.isOK()) {
					if (refactoringPipeline.hasChanges()) {
						Display.getDefault()
							.asyncExec(() -> showRefactoringPreviewWizard(refactoringPipeline, javaProjects,
									selectRulesWizardData));
					} else {
						Display.getDefault()
							.asyncExec(() -> {
								Shell shell = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow()
									.getShell();
								SimonykeesMessageDialog.openMessageDialog(shell,
										Messages.SelectRulesWizard_warning_no_refactorings,
										MessageDialog.INFORMATION);

								Job job = RefactoringPreviewWizard.createJobToShowSelectRulesWizard(refactoringPipeline,
										selectRulesWizardData, "Opening Select Rules Wizard."); //$NON-NLS-1$

								job.setUser(true);
								job.schedule();
							});
					}
				} else {
					refactoringPipeline.clearStates();
					Activator.setRunning(false);
				}
			}
		};
	}
}
