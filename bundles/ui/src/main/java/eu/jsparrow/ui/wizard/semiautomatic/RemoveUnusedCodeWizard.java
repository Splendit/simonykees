package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.rule.impl.unused.RemoveUnusedFieldsRule;
import eu.jsparrow.core.rule.impl.unused.RemoveUnusedMethodsRule;
import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;
import eu.jsparrow.core.visitor.unused.UnusedFieldWrapper;
import eu.jsparrow.core.visitor.unused.UnusedFieldsEngine;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodWrapper;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodsEngine;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preview.RemoveUnusedCodeRulePreviewWizard;
import eu.jsparrow.ui.preview.RemoveUnusedCodeRulePreviewWizardPage;
import eu.jsparrow.ui.util.ResourceHelper;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * Logger for configuring rules that remove unused fields, methods, and classes.
 * 
 * @since 4.8.0
 *
 */
public class RemoveUnusedCodeWizard extends AbstractRuleWizard {

	private static final Logger logger = LoggerFactory.getLogger(RemoveUnusedCodeWizard.class);

	private static final String WINDOW_ICON = "icons/jsparrow-icon-16-003.png"; //$NON-NLS-1$
	private static final int SUMMARY_BUTTON_ID = 9;

	private RemoveUnusedCodeWizardPageModel model;

	private IJavaProject selectedJavaProject;
	private List<ICompilationUnit> selectedJavaElements;
	private Rectangle rectangle;

	private RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
	private RemoveUnusedFieldsRule rule;
	private RemoveUnusedMethodsRule unusedMethodsRule;
	private UnusedFieldsEngine engine;
	private UnusedMethodsEngine unusedMethodsEngine;
	private Image windowDefaultImage;

	public RemoveUnusedCodeWizard(List<ICompilationUnit> selectedJavaElements) {
		this.selectedJavaElements = selectedJavaElements;
		setNeedsProgressMonitor(true);
		windowDefaultImage = ResourceHelper.createImage(WINDOW_ICON);
		Window.setDefaultImage(windowDefaultImage);

	}

	@Override
	public void dispose() {
		windowDefaultImage.dispose();
		super.dispose();
	}

	@Override
	public String getWindowTitle() {
		return Messages.RemoveUnusedCodeWizard_removeUnusedCodeWindowTitle;
	}

	@Override
	public void addPages() {
		model = new RemoveUnusedCodeWizardPageModel();
		RemoveUnusedCodeWizardPage page = new RemoveUnusedCodeWizardPage(model);
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (model.getClassMemberTypes()
			.isEmpty()) {
			return false;
		}
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		selectedJavaProject = selectedJavaElements.get(0)
			.getJavaProject();
		rectangle = Display.getCurrent()
			.getPrimaryMonitor()
			.getBounds();

		String message = NLS.bind(Messages.RemoveUnusedCodeWizard_startRefactoringInProjectMessage, this.getClass()
			.getSimpleName(), selectedJavaProject.getElementName());
		logger.info(message);

		Job job = new Job(Messages.RemoveUnusedCodeWizard_analysingFieldReferencesJobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String scope = model.getSearchScope();
				preRefactoring();
				engine = new UnusedFieldsEngine(scope);
				unusedMethodsEngine = new UnusedMethodsEngine(scope);

				SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

				SubMonitor child = subMonitor.split(70);
				child.setWorkRemaining(selectedJavaElements.size());
				child.setTaskName(Messages.RemoveUnusedCodeWizard_collectingTheSelectedCompilationUnitsTaskName);

				Map<String, Boolean> options = model.getOptionsMap();
				List<UnusedFieldWrapper> unusedFields = engine.findUnusedFields(selectedJavaElements,
						options, child);
				List<UnusedMethodWrapper> unusedMethods = unusedMethodsEngine.findUnusedMethods(selectedJavaElements, 
						options, child);
				

				if (unusedFields.isEmpty() && unusedMethods.isEmpty()) {
					WizardMessageDialog.synchronizeWithUIShowWarningNoRefactoringDialog();
					return Status.CANCEL_STATUS;
				}

				if (child.isCanceled()) {
					return Status.CANCEL_STATUS;
				}

				SubMonitor childSecondPart = subMonitor.split(30);
				childSecondPart.setTaskName("Collect compilation units"); //$NON-NLS-1$

				rule = new RemoveUnusedFieldsRule(unusedFields);
				unusedMethodsRule = new RemoveUnusedMethodsRule(unusedMethods);
				refactoringPipeline.setRules(Arrays.asList(rule, unusedMethodsRule));
				Set<ICompilationUnit> targetCompilationUnits = engine.getTargetCompilationUnits();
				Set<ICompilationUnit> targetUnusedMethodsCUs = unusedMethodsEngine.getTargetCompilationUnits();
				Set<ICompilationUnit>allTargetCUs = new HashSet<>(targetCompilationUnits);
				allTargetCUs.addAll(targetUnusedMethodsCUs);
				if (allTargetCUs.isEmpty()) {
					return Status.CANCEL_STATUS;
				}
				try {
					refactoringPipeline.prepareRefactoring(new ArrayList<>(allTargetCUs), childSecondPart);
					refactoringPipeline.updateInitialSourceMap();
				} catch (RefactoringException e) {
					logger.error("Cannot create working copies of the target compilation units.", e); //$NON-NLS-1$
					WizardMessageDialog.synchronizeWithUIShowInfo(
							new RefactoringException(
									ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
									ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e));
					return Status.CANCEL_STATUS;
				}

				if (childSecondPart.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				monitor.done();
				return Status.OK_STATUS;
			}

		};
		job.setUser(true);
		job.schedule();

		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult()
					.isOK()) {
					Job refactorJob = startRefactoringJob();

					refactorJob.setUser(true);
					refactorJob.schedule();
				} else {
					// do nothing if status is canceled, close
					Activator.setRunning(false);
				}
			}
		});

		return true;
	}

	private Job startRefactoringJob() {
		Job refactorJob = new Job(Messages.RemoveUnusedCodeWizard_calculateChangesJobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IStatus status = doRefactoring(monitor, refactoringPipeline);
				postRefactoring();

				return status;
			}
		};

		refactorJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult()
					.isOK()) {
					if (refactoringPipeline.hasChanges()) {
						createAndShowPreviewWizard();

						// when done without interruption
						Activator.setRunning(false);
					} else {
						WizardMessageDialog.synchronizeWithUIShowWarningNoRefactoringDialog();
					}
				} else {
					// do nothing if status is canceled, close
					Activator.setRunning(false);
				}
			}

		});
		return refactorJob;
	}

	private void createAndShowPreviewWizard() {
		Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> changes;
		Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> unusedMethodChanges;
		try {
			changes = rule.computeDocumentChangesPerField();
			unusedMethodChanges = unusedMethodsRule.computeDocumentChangesPerMethod();
			synchronizeWithUIShowRefactoringPreviewWizard(changes, unusedMethodChanges);
		} catch (JavaModelException e) {
			logger.error("Cannot create document for displaying changes - {} ", e.getMessage(), e); //$NON-NLS-1$
		}

	}

	private void synchronizeWithUIShowRefactoringPreviewWizard(
			Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> unusedFieldChanges, 
			Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> unusedMethodChanges) {

		String message = NLS.bind(Messages.RemoveUnusedCodeWizard_endRefactoringInProjectMessage, this.getClass()
			.getSimpleName(), selectedJavaProject.getElementName());
		logger.info(message);
		message = NLS.bind(Messages.RemoveUnusedCodeWizard_rulesWithChangesForProjectMessage,
				selectedJavaProject.getElementName(),
				rule.getRuleDescription()
					.getName());
		logger.info(message);

		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				StandaloneStatisticsMetadata standaloneStatisticsMetadata = prepareStatisticsMetadata(
						Collections.singletonList(selectedJavaProject));
				List<ICompilationUnit> targetCompilationUnits = new ArrayList<>(engine.getTargetCompilationUnits());
				List<UnusedClassMemberWrapper> unusedFields = rule.getUnusedFieldWrapperList();
				List<UnusedClassMemberWrapper> unusedMethods = unusedMethodsRule.getUnusedMethodWrapperList();
				RemoveUnusedCodeRulePreviewWizard removeUnusedCodePreviewWizard = new RemoveUnusedCodeRulePreviewWizard(
						refactoringPipeline,
						standaloneStatisticsMetadata, unusedFields, unusedMethods, unusedFieldChanges, unusedMethodChanges, targetCompilationUnits, rule, unusedMethodsRule);
				final WizardDialog dialog = new WizardDialog(shell, removeUnusedCodePreviewWizard) {
					@Override
					protected void nextPressed() {
						((RemoveUnusedCodeRulePreviewWizard) getWizard()).pressedNext();
						super.nextPressed();
					}

					@Override
					protected void backPressed() {
						((RemoveUnusedCodeRulePreviewWizard) getWizard()).pressedBack();
						super.backPressed();
					}

					@Override
					protected void createButtonsForButtonBar(Composite parent) {
						createButton(parent, SUMMARY_BUTTON_ID, Messages.RemoveUnusedCodeWizard_summaryButtonName,
								false);
						super.createButtonsForButtonBar(parent);
					}

					@Override
					protected void buttonPressed(int buttonId) {
						if (buttonId == SUMMARY_BUTTON_ID) {
							summaryButtonPressed();
						} else {
							super.buttonPressed(buttonId);
						}
					}

					private void summaryButtonPressed() {
						/*
						 * If summary button is pressed on any page that is not
						 * Summary page, views have to be check for change and
						 * updated, and preview control has to be disposed on
						 * current page. If it is already on Summary page, just
						 * refresh.
						 */
						if (getCurrentPage() instanceof RemoveUnusedCodeRulePreviewWizardPage) {
							removeUnusedCodePreviewWizard.updateViewsOnNavigation(getCurrentPage());
							((RemoveUnusedCodeRulePreviewWizardPage) getCurrentPage()).disposeControl();
						}
						showPage(removeUnusedCodePreviewWizard.getSummaryPage());
					}
				};

				// maximizes the RefactoringPreviewWizard
				dialog.setPageSize(rectangle.width, rectangle.height);
				dialog.open();
			});
	}
}
