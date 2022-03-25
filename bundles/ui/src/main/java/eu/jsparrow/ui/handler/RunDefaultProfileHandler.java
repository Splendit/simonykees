package eu.jsparrow.ui.handler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.statistic.StopWatchUtil;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.CompilationErrorsMessageDialog;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.ui.preference.profile.SimonykeesProfile;
import eu.jsparrow.ui.preview.RefactoringPreviewWizard;
import eu.jsparrow.ui.preview.RefactoringPreviewWizardPage;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;
import eu.jsparrow.ui.util.WizardHandlerUtil;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

public class RunDefaultProfileHandler extends AbstractRuleWizardHandler {

	private static final Logger logger = LoggerFactory.getLogger(RunDefaultProfileHandler.class);
	private LicenseUtilService licenseUtil = LicenseUtil.get();
	private StandaloneStatisticsMetadata statisticsMetadata;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		if(Activator.isRunning()) {
			super.openAlreadyRunningDialog();
			return null;
		}
		Activator.setRunning(true);
		final Shell shell = HandlerUtil.getActiveShell(event);
		if (!licenseUtil.checkAtStartUp(shell)) {
			Activator.setRunning(false);
			return null;
		}
		
		Map<IJavaProject, List<IJavaElement>> selectedJavaElements;
		try {
			selectedJavaElements = WizardHandlerUtil.getSelectedJavaElements(event);
		} catch (CoreException e) {
			logger.error(e.getMessage(), e);
			WizardMessageDialog.synchronizeWithUIShowError(new RefactoringException(
					Messages.SelectRulesWizardHandler_getting_selected_resources_failed + e.getMessage(),
					Messages.SelectRulesWizardHandler_user_getting_selected_resources_failed, e));
			return null;
		}
		
		if (selectedJavaElements.isEmpty()) {
			WizardMessageDialog.synchronizedWithUIShowWarningNoCompilationUnitDialog();
			logger.error(Messages.WizardMessageDialog_selectionDidNotContainAnyJavaFiles);
			Activator.setRunning(false);
			return null;
		}
		
		Job job = new Job("Starting jSparrow with the default profile.") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
				return runDefaultProfile(selectedJavaElements, monitor, refactoringPipeline);
			}
		};
		
		job.setUser(true);
		job.schedule();
		
		return true;
	}
	
	private IStatus runDefaultProfile(Map<IJavaProject, List<IJavaElement>> selectedJavaElements,
			IProgressMonitor monitor, RefactoringPipeline refactoringPipeline) {
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		try {
			for(List<IJavaElement> elements : selectedJavaElements.values()) {
				SelectRulesWizard.collectICompilationUnits(compilationUnits, elements, monitor);
			}
			List<ICompilationUnit> containingErrorList = refactoringPipeline
					.prepareRefactoring(compilationUnits, monitor);
			
			if (monitor.isCanceled()) {
				refactoringPipeline.clearStates();
				Activator.setRunning(false);
				return Status.CANCEL_STATUS;
			} else if (null != containingErrorList && !containingErrorList.isEmpty()) {
				synchronizeWithUIShowCompilationErrorMessage(containingErrorList,
						refactoringPipeline, selectedJavaElements);
			} else {
				synchronizeWithUIShowSelectRulesWizard(refactoringPipeline, selectedJavaElements);
			}
			
		} catch (RefactoringException e) {
			logger.error(e.getMessage(), e);
			WizardMessageDialog.synchronizeWithUIShowInfo(e);
			return Status.CANCEL_STATUS;
		} catch (JavaModelException jme) {
			logger.error(jme.getMessage(), jme);
			WizardMessageDialog.synchronizeWithUIShowInfo(new RefactoringException(
					ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
					ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed,
					jme));
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * Method used to open SelectRulesWizard from non UI thread
	 */
	private void synchronizeWithUIShowSelectRulesWizard(RefactoringPipeline refactoringPipeline,
			Map<IJavaProject, List<IJavaElement>> selectedJavaElements) {
		
		List<IJavaProject> javaProjects = new ArrayList<>(selectedJavaElements.keySet());
		String currentProfileName = SimonykeesPreferenceManager.getCurrentProfileId();
		List<RefactoringRule> allRules = RulesContainer.getRulesForProjects(selectedJavaElements.keySet(), false);
		SimonykeesPreferenceManager.loadCurrentProfiles();
		List<String> profileRuleIds = SimonykeesPreferenceManager.getProfileFromName(currentProfileName)
				.map(SimonykeesProfile::getEnabledRuleIds)
				.orElse(Collections.emptyList());
		List<RefactoringRule> rules = allRules.stream()
			.filter(rule -> profileRuleIds.contains(rule.getId()))
			.filter(RefactoringRule::isEnabled)
			.collect(Collectors.toList());
		performFinish(refactoringPipeline, javaProjects, rules);

	}
	
	/**
	 * Method used to open CompilationErrorsMessageDialog from non UI thread to
	 * list all Java files that will be skipped because they contain compilation
	 * errors.
	 */
	private void synchronizeWithUIShowCompilationErrorMessage(List<ICompilationUnit> containingErrorList,
			RefactoringPipeline refactoringPipeline, Map<IJavaProject, List<IJavaElement>> selectedJavaElements) {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				CompilationErrorsMessageDialog dialog = new CompilationErrorsMessageDialog(shell);
				dialog.create();
				dialog.setTableViewerInput(containingErrorList);
				dialog.open();
				if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
					if (refactoringPipeline.hasRefactoringStates()) {
						synchronizeWithUIShowSelectRulesWizard(refactoringPipeline, selectedJavaElements);
					} else {
						WizardMessageDialog.synchronizeWithUIShowWarningNoComlipationUnitWithoutErrorsDialog();
					}
				} else {
					Activator.setRunning(false);
				}
			});
	}
	

	public boolean performFinish(RefactoringPipeline refactoringPipeline, List<IJavaProject> javaProjects, final List<RefactoringRule> selectedRules) {
		String message = NLS.bind(Messages.SelectRulesWizard_start_refactoring, this.getClass()
			.getSimpleName(),
				javaProjects.stream()
					.map(IJavaProject::getElementName)
					.collect(Collectors.joining(";"))); //$NON-NLS-1$
		logger.info(message);

		refactoringPipeline.setRules(selectedRules);
		refactoringPipeline.updateInitialSourceMap();

		Job job = new Job(Messages.ProgressMonitor_calculating_possible_refactorings) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				statisticsMetadata = prepareStatisticsMetadata(javaProjects);

				StopWatchUtil.start();
				IStatus refactoringStatus = doRefactoring(monitor, refactoringPipeline);
				StopWatchUtil.stop();

				return refactoringStatus;
			}
		};

		job.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult()
					.isOK()) {
					if (refactoringPipeline.hasChanges()) {
						synchronizeWithUIShowRefactoringPreviewWizard(refactoringPipeline, javaProjects);
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
	
	protected StandaloneStatisticsMetadata prepareStatisticsMetadata(Collection<IJavaProject> javaProjects) {

		String repoName = javaProjects.stream()
			.map(IJavaProject::getElementName)
			.collect(Collectors.joining(";")); //$NON-NLS-1$

		return new StandaloneStatisticsMetadata(Instant.now()
			.getEpochSecond(), "Splendit-Internal-Measurement", repoName); //$NON-NLS-1$
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
	
	private void synchronizeWithUIShowRefactoringPreviewWizard(RefactoringPipeline refactoringPipeline,
			List<IJavaProject> javaProjects) {

		Display.getDefault()
			.asyncExec(() -> {

				logger.info(NLS.bind(Messages.SelectRulesWizard_end_refactoring, this.getClass()
					.getSimpleName(),
						javaProjects.stream()
							.map(IJavaProject::getElementName)
							.collect(Collectors.joining(";")))); //$NON-NLS-1$
				logger.info(NLS.bind(Messages.SelectRulesWizard_rules_with_changes, javaProjects.stream()
					.map(IJavaProject::getElementName)
					.collect(Collectors.joining(";")), refactoringPipeline.getRulesWithChangesAsString())); //$NON-NLS-1$

				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				RefactoringPreviewWizard previewWizard = new RefactoringPreviewWizard(refactoringPipeline,
						statisticsMetadata);
				final WizardDialog dialog = new WizardDialog(shell, previewWizard) {

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

					@Override
					protected void createButtonsForButtonBar(Composite parent) {
						createButton(parent, 9, Messages.SelectRulesWizard_Summary, false);
						super.createButtonsForButtonBar(parent);
					}

					@Override
					protected void buttonPressed(int buttonId) {
						if (buttonId == 9) {
							summaryButtonPressed();
						} else {
							super.buttonPressed(buttonId);
						}
					}

					@Override
					protected void cancelPressed() {
						previewWizard.performCancel();
						super.cancelPressed();
					}

					private void summaryButtonPressed() {
						if (getCurrentPage() instanceof RefactoringPreviewWizardPage) {
							previewWizard.updateViewsOnNavigation(getCurrentPage());
							((RefactoringPreviewWizardPage) getCurrentPage()).disposeControl();
						}
						showPage(previewWizard.getSummaryPage());
					}
				};

				// maximizes the RefactoringPreviewWizard
				dialog.setPageSize(1200, 800);
				dialog.open();
			});
	}

}
