package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.unused.RemoveUnusedFieldsRule;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.core.visitor.unused.UnusedFieldWrapper;
import eu.jsparrow.core.visitor.unused.UnusedFieldsEngine;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preview.RemoveDeadCodeRulePreviewWizard;
import eu.jsparrow.ui.preview.RemoveDeadCodeRulePreviewWizardPage;
import eu.jsparrow.ui.preview.RenamingRulePreviewWizard;
import eu.jsparrow.ui.preview.RenamingRulePreviewWizardPage;
import eu.jsparrow.ui.util.ResourceHelper;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

public class RemoveDeadCodeWizard extends AbstractRuleWizard {
	
	private static final Logger logger = LoggerFactory.getLogger(RemoveDeadCodeWizard.class);
	
	private static final String WINDOW_ICON = "icons/jsparrow-icon-16-003.png"; //$NON-NLS-1$
	private static final int SUMMARY_BUTTON_ID = 9;
	
	private RemoveDeadCodeWizardPageModel model;

	private IJavaProject selectedJavaProject;
	private List<ICompilationUnit> selectedJavaElements;
	private Rectangle rectangle;
	
	private boolean canRefactor = true;
	private RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
	private RemoveUnusedFieldsRule rule;
	private UnusedFieldsEngine engine;
	
	public RemoveDeadCodeWizard(List<ICompilationUnit> selectedJavaElements) {
		this.selectedJavaElements = selectedJavaElements;
		setNeedsProgressMonitor(true);
		Window.setDefaultImage(ResourceHelper.createImage(WINDOW_ICON)); // FIXME dispose
		
	}
	
	@Override
	public String getWindowTitle() {
		return "Remove Redundant Code";
	}

	@Override
	public void addPages() {
		model = new RemoveDeadCodeWizardPageModel();
		RemoveDeadCodeWizardPage page = new RemoveDeadCodeWizardPage(model);
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
		selectedJavaProject = selectedJavaElements.get(0).getJavaProject();
		rectangle = Display.getCurrent()
				.getPrimaryMonitor()
				.getBounds();
		
		Job job = new Job("Analysing Field References") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String scope = model.getSearchScope();
				preRefactoring();
				engine = new UnusedFieldsEngine(scope);

				SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

				SubMonitor child = subMonitor.split(70);
				child.setWorkRemaining(selectedJavaElements.size());
				child.setTaskName(Messages.RenameFieldsRuleWizard_taskName_collectingUnits);

				List<UnusedFieldWrapper> unusedFields = engine.findUnusedFields(selectedJavaElements, model.getOptionsMap(), child);
				
				if(child.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				
				SubMonitor childSecondPart = subMonitor.split(30);
				childSecondPart.setTaskName("Collect compilation units");
				
				rule = new RemoveUnusedFieldsRule(unusedFields);
				refactoringPipeline.setRules(Collections.singletonList(rule));
				Set<ICompilationUnit> targetCompilationUnits = engine.getTargetCompilationUnits();
				try {
					refactoringPipeline.prepareRefactoring(new ArrayList<>(targetCompilationUnits), childSecondPart);
					// why not refactoringPipeline.createRefactoringStates(targetCompilationUnits);???
					// refactoringPipeline.updateInitialSourceMap(); FIXME: what is this thing doing? 
				} catch (RefactoringException e) {
					e.printStackTrace();
				}
				
				
				
				return null;
			}

		};
		job.setUser(true);
		job.schedule();
		
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult()
					.isOK() && canRefactor) { // this flag is very bad!
					Job refactorJob = startRefactoringJob();

					refactorJob.setUser(true);
					refactorJob.schedule();
				} else {
					// do nothing if status is canceled, close
					Activator.setRunning(false);
				}
			}
		});
		
		return false;
	}
	
	private Job startRefactoringJob() {
		Job refactorJob = new Job(Messages.ProgressMonitor_calculating_possible_refactorings) {

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

		Map<UnusedFieldWrapper, Map<ICompilationUnit, DocumentChange>> changes =  rule.computeDocumentChangesPerField();

		synchronizeWithUIShowRefactoringPreviewWizard(changes);
	}
	
	private void synchronizeWithUIShowRefactoringPreviewWizard(
			Map<UnusedFieldWrapper, Map<ICompilationUnit, DocumentChange>> changes) {

//		String message = NLS.bind(Messages.SelectRulesWizard_end_refactoring, this.getClass()
//			.getSimpleName(), selectedJavaProjekt.getElementName());
//		logger.info(message);
//		message = NLS.bind(Messages.SelectRulesWizard_rules_with_changes, selectedJavaProjekt.getElementName(),
//				renameFieldsRule.getRuleDescription()
//					.getName());
//		logger.info(message);

		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				List<ICompilationUnit> targetCompilationUnits = new ArrayList<>(engine.getTargetCompilationUnits());
				List<UnusedFieldWrapper> unusedFields = rule.getUnusedFieldWrapperList();
				RemoveDeadCodeRulePreviewWizard removeDeadCodePreviewWizard = new RemoveDeadCodeRulePreviewWizard(refactoringPipeline,
						unusedFields, changes, targetCompilationUnits, rule);
				final WizardDialog dialog = new WizardDialog(shell, removeDeadCodePreviewWizard) {
					@Override
					protected void nextPressed() {
						((RemoveDeadCodeRulePreviewWizard) getWizard()).pressedNext();
						super.nextPressed();
					}

					@Override
					protected void backPressed() {
						((RemoveDeadCodeRulePreviewWizard) getWizard()).pressedBack();
						super.backPressed();
					}

					@Override
					protected void createButtonsForButtonBar(Composite parent) {
						createButton(parent, SUMMARY_BUTTON_ID, Messages.SelectRulesWizard_Summary, false);
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
						if (getCurrentPage() instanceof RemoveDeadCodeRulePreviewWizardPage) {
							removeDeadCodePreviewWizard.updateViewsOnNavigation(getCurrentPage());
							((RemoveDeadCodeRulePreviewWizardPage) getCurrentPage()).disposeControl();
						}
						showPage(removeDeadCodePreviewWizard.getSummaryPage());
					}
				};

				// maximizes the RefactoringPreviewWizard
				dialog.setPageSize(rectangle.width, rectangle.height);
				dialog.open();
			});

	}

}
