package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.impl.PublicFieldsRenamingRule;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationASTVisitor;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationVisitorFactory;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preview.RenamingRulePreviewWizard;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * Wizard that holds {@link ConfigureRenameFieldsRuleWizardPage} with
 * configuration options for {@link PublicFieldsRenamingRule}. On Finish it sets
 * chosen options on rule and start refactoring process.
 * 
 * @author Andreja Sambolec
 * @since 2.3.0
 *
 */
public class ConfigureRenameFieldsRuleWizard extends AbstractRuleWizard {

	public static final Logger logger = LoggerFactory.getLogger(ConfigureRenameFieldsRuleWizard.class);
	
	private static final int SUMMARY_BUTTON_ID = 9;

	private ConfigureRenameFieldsRuleWizardPageModel model;

	private IJavaProject selectedJavaProjekt;
	private List<ICompilationUnit> selectedJavaElements;

	private RefactoringPipeline refactoringPipeline;
	private List<FieldMetaData> metadata;
	private PublicFieldsRenamingRule renameFieldsRule;

	private List<ICompilationUnit> targetCompilationUnits = new ArrayList<>();

	private Rectangle rectangle;

	private boolean canRefactor = true;

	public ConfigureRenameFieldsRuleWizard(List<ICompilationUnit> selectedJavaElements) {
		this.selectedJavaElements = selectedJavaElements;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return Messages.ConfigureRenameFieldsRuleWizard_WizardTitle_RenameFields;
	}

	@Override
	public void addPages() {
		model = new ConfigureRenameFieldsRuleWizardPageModel();
		ConfigureRenameFieldsRuleWizardPage page = new ConfigureRenameFieldsRuleWizardPage(model);
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (model.getFieldTypes()
			.isEmpty()) {
			return false;
		}
		return super.canFinish();
	}

	/**
	 * On finish rule applying is started.
	 */
	@Override
	public boolean performFinish() {

		selectedJavaProjekt = selectedJavaElements.get(0)
			.getJavaProject();

		rectangle = Display.getCurrent()
			.getPrimaryMonitor()
			.getBounds();

		String message = NLS.bind(Messages.SelectRulesWizard_start_refactoring, this.getClass()
			.getSimpleName(), selectedJavaProjekt.getElementName());
		logger.info(message);

		Job job = new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				/*
				 * The Refactoring process is considered to be started as soon
				 * as we create the FieldDeclarationASTVisitor which searches
				 * for references of the fields to be renamed.
				 */
				preRefactoring();
				FieldDeclarationASTVisitor visitor = createVisitor();

				SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

				SubMonitor child = subMonitor.split(40);
				child.setWorkRemaining(selectedJavaElements.size());
				child.setTaskName(Messages.RenameFieldsRuleWizard_taskName_collectingUnits);
				int prepareStatus = FieldDeclarationVisitorFactory.prepareRenaming(selectedJavaElements,
						selectedJavaProjekt, visitor, child);

				if (IStatus.INFO == prepareStatus) {
					WizardMessageDialog.synchronizeWithUIShowMultiprojectMessage();
					return Status.CANCEL_STATUS;
				}

				if (IStatus.CANCEL == prepareStatus) {
					return Status.CANCEL_STATUS;
				}

				SubMonitor childSecondPart = subMonitor.split(50);
				childSecondPart.setWorkRemaining(100);

				searchScopeAndPrepareRefactoringStates(childSecondPart, visitor);
				if (!canRefactor) {
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
					.isOK() && canRefactor) {
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
	
	/**
	 * Creates Job which does refactoring and creates preview wizard.
	 * 
	 * @return Job which does refactoring on collected refactoring states.
	 */
	private Job startRefactoringJob() {
		Job refactorJob = new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

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
					if (LicenseUtil.getInstance()
						.isValid()) {
						if (refactoringPipeline.hasChanges()) {
							createAndShowPreviewWizard();

							// when done without interruption
							Activator.setRunning(false);
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
		return refactorJob;
	}
	
	/**
	 * Creates {@link FieldDeclarationASTVisitor} and sets all options selected
	 * by user from {@link ConfigureRenameFieldsRuleWizardPageModel}.
	 * 
	 * @return created and updated {@link FieldDeclarationASTVisitor}
	 */
	private FieldDeclarationASTVisitor createVisitor() {
		Map<String, Boolean> options = model.getOptionsMap();
		String modelSearchScope = model.getSearchScope();
		return FieldDeclarationVisitorFactory.visitorFactory(selectedJavaProjekt, options, modelSearchScope);
	}

	/**
	 * Collects target compilation units and creates refactoring states from
	 * them.
	 * 
	 * @param subMonitor
	 *            progress monitor
	 * @param visitor
	 *            {@link PublicFieldsRenamingRule} visitor
	 */
	private void searchScopeAndPrepareRefactoringStates(SubMonitor subMonitor, FieldDeclarationASTVisitor visitor) {

		targetCompilationUnits = new ArrayList<>(visitor.getTargetIJavaElements());
		if (targetCompilationUnits.isEmpty()) {
			WizardMessageDialog.synchronizeWithUIShowWarningNoRefactoringDialog();
			canRefactor = false;
			return;
		}

		metadata = visitor.getFieldMetaData();
		List<FieldMetaData> todosMetadata = visitor.getUnmodifiableFieldMetaData();

		if (subMonitor.isCanceled()) {
			return;
		}

		renameFieldsRule = new PublicFieldsRenamingRule(metadata, todosMetadata);
		final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = Arrays.asList(renameFieldsRule);

		refactoringPipeline = new RefactoringPipeline();
		refactoringPipeline.setRules(rules);

		SubMonitor child = subMonitor.split(80);
		child.setWorkRemaining(targetCompilationUnits.size());
		child.setTaskName(Messages.RenameFieldsRuleWizard_taskName_collectingUnits);
		List<RefactoringState> refactoringStates = new ArrayList<>();
		for (ICompilationUnit compilationUnit : targetCompilationUnits) {
			try {
				refactoringStates.add(new RefactoringState(compilationUnit, compilationUnit.getWorkingCopy(null)));
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), e);
				WizardMessageDialog.synchronizeWithUIShowInfo(
						new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
								ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e));
				canRefactor = false;
				return;
			}
			if (child.isCanceled()) {
				return;
			} else {
				child.worked(1);
			}
		}

		refactoringPipeline.setRefactoringStates(refactoringStates);
		refactoringPipeline.updateInitialSourceMap();
	}

	/**
	 * Creates DocumentChanges and calls method to show preview wizard.
	 */
	private void createAndShowPreviewWizard() {

		Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> changes = new HashMap<>();
		for (FieldMetaData data : metadata) {
			Map<ICompilationUnit, DocumentChange> docsChanges;
			try {
				docsChanges = renameFieldsRule.computeDocumentChangesPerFiled(data);
				changes.put(data, docsChanges);
			} catch (JavaModelException e) {
				logger.error("Cannot create document for displaying changes - " + e.getMessage(), e); //$NON-NLS-1$
			}
		}

		synchronizeWithUIShowRefactoringPreviewWizard(changes);
	}

	/**
	 * Open preview wizard containing all changes made by this rule.
	 * 
	 * @param changes
	 *            Map containing changes to be displayed
	 */
	private void synchronizeWithUIShowRefactoringPreviewWizard(
			Map<FieldMetaData, Map<ICompilationUnit, DocumentChange>> changes) {

		String message = NLS.bind(Messages.SelectRulesWizard_end_refactoring, this.getClass()
			.getSimpleName(), selectedJavaProjekt.getElementName());
		logger.info(message);
		message = NLS.bind(Messages.SelectRulesWizard_rules_with_changes, selectedJavaProjekt.getElementName(),
				renameFieldsRule.getRuleDescription()
					.getName());
		logger.info(message);

		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				RenamingRulePreviewWizard renamingPreviewWizard = new RenamingRulePreviewWizard(refactoringPipeline,
						metadata, changes, targetCompilationUnits, renameFieldsRule);
				final WizardDialog dialog = new WizardDialog(shell, renamingPreviewWizard ) {
					@Override
					protected void nextPressed() {
						((RenamingRulePreviewWizard) getWizard()).pressedNext();
						super.nextPressed();
					}

					@Override
					protected void backPressed() {
						((RenamingRulePreviewWizard) getWizard()).pressedBack();
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
							nextPressed();
						} else {
							super.buttonPressed(buttonId);
						}
					}
				};

				// maximizes the RefactoringPreviewWizard
				dialog.setPageSize(rectangle.width, rectangle.height);
				dialog.open();
			});

	}
}
