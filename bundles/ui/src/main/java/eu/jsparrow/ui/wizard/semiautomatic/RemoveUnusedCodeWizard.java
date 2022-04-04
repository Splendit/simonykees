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
import eu.jsparrow.core.rule.impl.unused.RemoveUnusedTypesRule;
import eu.jsparrow.core.visitor.unused.UnusedFieldWrapper;
import eu.jsparrow.core.visitor.unused.UnusedFieldsEngine;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodWrapper;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodsEngine;
import eu.jsparrow.core.visitor.unused.type.UnusedTypeWrapper;
import eu.jsparrow.core.visitor.unused.type.UnusedTypesEngine;
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
	private RemoveUnusedTypesRule unusedTypesRule;
	private Image windowDefaultImage;
	private Set<ICompilationUnit> allTargetCompilationUnits = new HashSet<>();

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
				UnusedFieldsEngine engine = new UnusedFieldsEngine(scope);
				UnusedMethodsEngine unusedMethodsEngine = new UnusedMethodsEngine(scope);
				UnusedTypesEngine unusedTypeEngine = new UnusedTypesEngine(scope);

				SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

				boolean fieldsChecked = hasClassMemberOptionsChecked(model, "fields"); //$NON-NLS-1$
				boolean methodsChecked = hasClassMemberOptionsChecked(model, "methods"); //$NON-NLS-1$
				boolean typesChecked = hasClassMemberOptionsChecked(model, "class"); //$NON-NLS-1$

				int fieldsSubmonitorSplit = calcFieldsSubMonitorSplit(fieldsChecked, methodsChecked, typesChecked);
				int typesSubmonitorSplit = calcFieldsSubMonitorSplit(typesChecked, methodsChecked, typesChecked);
				int methodSubmonitorSplit = 70 - (fieldsSubmonitorSplit + typesSubmonitorSplit);

				Map<String, Boolean> options = model.getOptionsMap();

				List<UnusedFieldWrapper> unusedFields = findUnusedFields(engine, subMonitor, fieldsSubmonitorSplit,
						options);
				List<UnusedMethodWrapper> unusedMethods = findUnusedMethods(unusedMethodsEngine, subMonitor,
						methodSubmonitorSplit, options);
				List<UnusedTypeWrapper> unusedTypes = findUnusedTypes(unusedTypeEngine, subMonitor, typesSubmonitorSplit, options);

				if (unusedFields.isEmpty() && unusedMethods.isEmpty() && unusedTypes.isEmpty()) {
					WizardMessageDialog.synchronizeWithUIShowWarningNoRefactoringDialog();
					return Status.CANCEL_STATUS;
				}

				SubMonitor childSecondPart = subMonitor.split(30);
				childSecondPart.setTaskName("Collect compilation units"); //$NON-NLS-1$

				rule = new RemoveUnusedFieldsRule(unusedFields);
				unusedMethodsRule = new RemoveUnusedMethodsRule(unusedMethods);
				unusedTypesRule = new RemoveUnusedTypesRule(unusedTypes);
				refactoringPipeline.setRules(Arrays.asList(rule, unusedMethodsRule, unusedTypesRule));
				Set<ICompilationUnit> targetCompilationUnits = engine.getTargetCompilationUnits();
				Set<ICompilationUnit> targetUnusedMethodsCUs = unusedMethodsEngine.getTargetCompilationUnits();
				Set<ICompilationUnit> targetUnusedTypesCUs = unusedTypeEngine.getTargetCompilationUnits();
				allTargetCompilationUnits.addAll(targetCompilationUnits);
				allTargetCompilationUnits.addAll(targetUnusedMethodsCUs);
				allTargetCompilationUnits.addAll(targetUnusedTypesCUs);
				if (allTargetCompilationUnits.isEmpty()) {
					return Status.CANCEL_STATUS;
				}
				try {
					refactoringPipeline.prepareRefactoring(new ArrayList<>(allTargetCompilationUnits), childSecondPart);
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

	private List<UnusedFieldWrapper> findUnusedFields(UnusedFieldsEngine engine, SubMonitor subMonitor,
			int fieldsSubmonitorSplit, Map<String, Boolean> options) {
		boolean fieldsChecked = hasClassMemberOptionsChecked(model, "fields"); //$NON-NLS-1$
		if (!fieldsChecked) {
			return Collections.emptyList();
		}

		SubMonitor removeUnusedFieldsSubMonitor = subMonitor.split(fieldsSubmonitorSplit);
		removeUnusedFieldsSubMonitor.setWorkRemaining(selectedJavaElements.size());
		removeUnusedFieldsSubMonitor.setTaskName(Messages.RemoveUnusedCodeWizard_findingUnusedFields);
		List<UnusedFieldWrapper> unusedFields = engine.findUnusedFields(selectedJavaElements,
				options, removeUnusedFieldsSubMonitor);
		if (removeUnusedFieldsSubMonitor.isCanceled()) {
			return Collections.emptyList();
		}

		return unusedFields;
	}

	private List<UnusedMethodWrapper> findUnusedMethods(UnusedMethodsEngine unusedMethodsEngine,
			SubMonitor subMonitor, int methodsSubmonitorSplit,
			Map<String, Boolean> options) {
		boolean methodsChecked = hasClassMemberOptionsChecked(model, "methods"); //$NON-NLS-1$
		if (!methodsChecked) {
			return Collections.emptyList();
		}
		SubMonitor removeUnusedMethodsSubMonitor = subMonitor.split(methodsSubmonitorSplit);
		removeUnusedMethodsSubMonitor.setWorkRemaining(selectedJavaElements.size());
		removeUnusedMethodsSubMonitor.setTaskName(Messages.RemoveUnusedCodeWizard_findingUnusedMethods);

		List<UnusedMethodWrapper> unusedMethods = unusedMethodsEngine.findUnusedMethods(selectedJavaElements,
				options, removeUnusedMethodsSubMonitor);

		if (removeUnusedMethodsSubMonitor.isCanceled()) {
			return Collections.emptyList();
		}
		return unusedMethods;
	}
	
	private List<UnusedTypeWrapper> findUnusedTypes(UnusedTypesEngine unusedTypeEngine, SubMonitor subMonitor,
			int typesSubmonitorSplit, Map<String, Boolean> options) {
		

		if(!hasClassMemberOptionsChecked(model, "class")) {
			return Collections.emptyList();
		}
		
		SubMonitor removeUnusedTypesSubMonitor = subMonitor.split(typesSubmonitorSplit);
		removeUnusedTypesSubMonitor.setWorkRemaining(selectedJavaElements.size());
		removeUnusedTypesSubMonitor.setTaskName("Finding unused types");
		
		List<UnusedTypeWrapper> unusedTypes = unusedTypeEngine.findUnusedTypes(selectedJavaElements, options, removeUnusedTypesSubMonitor);
		
		if(removeUnusedTypesSubMonitor.isCanceled()) {
			return Collections.emptyList();
		}
		
		return unusedTypes;
	}

	private boolean hasClassMemberOptionsChecked(RemoveUnusedCodeWizardPageModel model, String classMember) {
		Map<String, Boolean> options = model.getOptionsMap();
		return options.entrySet()
			.stream()
			.filter(entry -> entry.getKey()
				.contains(classMember))
			.map(Map.Entry<String, Boolean>::getValue)
			.anyMatch(value -> value);
	}

	private int calcFieldsSubMonitorSplit(boolean fieldsChecked, boolean methodsChecked, boolean typesChecked) {
		if(fieldsChecked) {
			if(methodsChecked && typesChecked) {
				return 20;
			} else if(methodsChecked || typesChecked) {
				return 30;
			} else {
				return 70;
			}
		} else {
			return 0;
		}
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
				List<ICompilationUnit> targetCompilationUnits = new ArrayList<>(allTargetCompilationUnits);
				RemoveUnusedCodeRulePreviewWizard removeUnusedCodePreviewWizard;
				try {
					removeUnusedCodePreviewWizard = new RemoveUnusedCodeRulePreviewWizard(
							refactoringPipeline,
							standaloneStatisticsMetadata, targetCompilationUnits,
							rule, unusedMethodsRule, unusedTypesRule);
				} catch (JavaModelException e) {
					logger.error("Cannot create document for displaying changes - {} ", e.getMessage(), e); //$NON-NLS-1$
					return;
				}

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
