package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.refactorer.RefactoringState;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.impl.PublicFieldsRenamingRule;
import at.splendit.simonykees.core.ui.LicenseUtil;
import at.splendit.simonykees.core.ui.dialog.CompilationErrorsMessageDialog;
import at.splendit.simonykees.core.ui.preview.RenamingRulePreviewWizard;
import at.splendit.simonykees.core.ui.wizard.impl.WizardMessageDialog;
import at.splendit.simonykees.core.util.RefactoringUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.visitor.renaming.FieldDeclarationASTVisitor;
import at.splendit.simonykees.core.visitor.renaming.FieldMetadata;
import at.splendit.simonykees.core.visitor.renaming.PublicFieldsRenamingASTVisitor;
import at.splendit.simonykees.i18n.ExceptionMessages;
import at.splendit.simonykees.i18n.Messages;

public class RenameFieldsRuleWizard extends Wizard {

	private static final Logger logger = LoggerFactory.getLogger(RenameFieldsRuleWizard.class);

	private RenameFieldsRuleWizardPageModel model;

	private IJavaProject selectedJavaProjekt;
	private List<IJavaElement> selectedJavaElements;

	private RefactoringPipeline refactoringPipeline;
	private List<FieldMetadata> metadata;
	private PublicFieldsRenamingRule renameFieldsRule;

	private boolean canRefactor = true;

	public RenameFieldsRuleWizard(List<IJavaElement> selectedJavaElements) {
		this.selectedJavaElements = selectedJavaElements;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return Messages.LoggerRuleWizard_title;
	}

	@Override
	public void addPages() {
		model = new RenameFieldsRuleWizardPageModel();
		RenameFieldsRuleWizardPage page = new RenameFieldsRuleWizardPage(model);
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (model.getFieldTypes().isEmpty()) {
			return false;
		}
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {

		selectedJavaProjekt = selectedJavaElements.get(0).getJavaProject();

		logger.info(NLS.bind(Messages.SelectRulesWizard_start_refactoring, this.getClass().getSimpleName(),
				selectedJavaProjekt.getElementName()));

		Job job = new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				FieldDeclarationASTVisitor visitor = createVisitor();

				SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

				List<ICompilationUnit> result = new ArrayList<>();
				if (!getCompilationUnits(result, selectedJavaElements, subMonitor.split(10))) {
					return Status.CANCEL_STATUS;
				}

				/*
				 * list with compilation units with compilation error, which
				 * should be excluded from scope
				 */
				List<ICompilationUnit> containingErrorList = new ArrayList<>();

				SubMonitor child = subMonitor.split(40);
				child.setWorkRemaining(result.size());
				child.setTaskName(Messages.RenameFieldsRuleWizard_taskName_collectingUnits);
				for (ICompilationUnit compilationUnit : result) {
					if (!compilationUnit.getJavaProject().equals(selectedJavaProjekt)) {
						WizardMessageDialog.synchronizeWithUIShowMultiprojectMessage();
						return Status.CANCEL_STATUS;

					}
					if (RefactoringUtil.checkForSyntaxErrors(compilationUnit)) {
						containingErrorList.add(compilationUnit);
					} else {
						CompilationUnit cu = RefactoringUtil.parse(compilationUnit);
						cu.accept(visitor);
					}
					if (child.isCanceled()) {
						return Status.CANCEL_STATUS;
					} else {
						child.worked(1);
					}
				}

				SubMonitor childSecondPart = subMonitor.split(50);
				childSecondPart.setWorkRemaining(100);
				if (!containingErrorList.isEmpty()) {
					synchronizeWithUIShowCompilationErrorMessage(containingErrorList);
				}
				if (canRefactor) {
					searchScopeAndPrepareRefactoringStates(childSecondPart, visitor);
				} else {
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

				if (event.getResult().isOK() && canRefactor) {
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
	 * Method used to open CompilationErrorsMessageDialog from non UI thread to
	 * list all Java files that will be skipped because they contain compilation
	 * errors.
	 */
	private void synchronizeWithUIShowCompilationErrorMessage(List<ICompilationUnit> containingErrorList) {
		Display.getDefault().syncExec(() -> {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			CompilationErrorsMessageDialog dialog = new CompilationErrorsMessageDialog(shell);
			dialog.create();
			dialog.setTableViewerInput(containingErrorList);
			dialog.open();
			if (dialog.getReturnCode() != IDialogConstants.OK_ID) {
				canRefactor = false;
				Activator.setRunning(false);
			}
		});
	}

	private Job startRefactoringJob() {
		Job refactorJob = new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

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

		refactorJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult().isOK()) {
					if (LicenseUtil.getInstance().isValid()) {
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

	private FieldDeclarationASTVisitor createVisitor() {
		FieldDeclarationASTVisitor visitor;
		if (RenameFieldsRuleWizardPageConstants.SCOPE_PROJECT.equals(model.getSearchScope())) {
			IJavaElement[] scope = { selectedJavaProjekt };
			visitor = new FieldDeclarationASTVisitor(scope);
		} else {
			List<IJavaProject> projectList = new LinkedList<>();
			try {
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				IProject[] projects = workspaceRoot.getProjects();
				for (int i = 0; i < projects.length; i++) {
					IProject project = projects[i];
					if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
						projectList.add(JavaCore.create(project));
					}
				}
			} catch (CoreException e) {
				logger.error(e.getMessage(), e);
			}
			IJavaElement[] scope = projectList.toArray(new IJavaElement[0]);
			visitor = new FieldDeclarationASTVisitor(scope);
		}
		visitor.setRenamePrivateField(model.getFieldTypes().contains(RenameFieldsRuleWizardPageConstants.TYPE_PRIVATE));
		visitor.setRenameProtectedField(
				model.getFieldTypes().contains(RenameFieldsRuleWizardPageConstants.TYPE_PROTECTED));
		visitor.setRenamePackageProtectedField(
				model.getFieldTypes().contains(RenameFieldsRuleWizardPageConstants.TYPE_PACKAGEPROTECTED));
		visitor.setRenamePublicField(model.getFieldTypes().contains(RenameFieldsRuleWizardPageConstants.TYPE_PUBLIC));
		visitor.setUppercaseAfterUnderscore(model.setUpperCaseForUnderscoreReplacementOption());
		visitor.setUppercaseAfterDollar(model.setUpperCaseForDollarReplacementOption());
		visitor.setAddTodo(model.isAddTodoComments());
		return visitor;
	}

	private boolean getCompilationUnits(List<ICompilationUnit> resultCompilationUnitsList,
			List<IJavaElement> sourceJavaElementsList, IProgressMonitor monitor) {

		try {
			RefactoringUtil.collectICompilationUnits(resultCompilationUnitsList, sourceJavaElementsList, monitor);
			if (resultCompilationUnitsList.isEmpty()) {
				logger.warn(ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found);
				WizardMessageDialog.synchronizeWithUIShowInfo(
						new RefactoringException(ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found,
								ExceptionMessages.RefactoringPipeline_user_warn_no_compilation_units_found));
				return false;

			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
			WizardMessageDialog.synchronizeWithUIShowInfo(
					new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
							ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e));
			return false;
		}

		return true;
	}

	private void searchScopeAndPrepareRefactoringStates(SubMonitor subMonitor, FieldDeclarationASTVisitor visitor) {

		Set<IJavaElement> targetJavaElements = visitor.getTargetIJavaElements();
		if (targetJavaElements.isEmpty()) {
			WizardMessageDialog.synchronizeWithUIShowWarningNoComlipationUnitDialog();
			canRefactor = false;
			return;
		}

		List<ICompilationUnit> targetCompilationUnits = new ArrayList<>();

		metadata = visitor.getFieldMetadata();
		List<FieldMetadata> todosMetadata = visitor.getUnmodifiableFieldMetadata();

		if (!getCompilationUnits(targetCompilationUnits, targetJavaElements.stream().collect(Collectors.toList()),
				subMonitor.split(20))) {
			canRefactor = false;
			return;
		}
		if (subMonitor.isCanceled()) {
			return;
		}

		renameFieldsRule = new PublicFieldsRenamingRule(PublicFieldsRenamingASTVisitor.class, metadata, todosMetadata);
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
	}

	private void createAndShowPreviewWizard() {

		// TODO create and show preview wizard
		// for multi file changing rule
		// TODO use this below to display
		// changes onpreview

		Map<String, List<DocumentChange>> changes = new HashMap<>();
		Map<String, FieldMetadata> metaDataMap = new HashMap<>();
		for (FieldMetadata data : metadata) {

			try {
				String newIdentifier = data.getNewIdentifier();
				SimpleName oldName = data.getFieldDeclaration().getName();
				String oldIdentifier = oldName.getIdentifier();
				data.getCompilationUnit().getJavaElement();
				List<DocumentChange> docsChanges = renameFieldsRule.computeDocumentChangesPerFiled(data);
				changes.put(newIdentifier, docsChanges);
				metaDataMap.put(newIdentifier, data);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch
				// block
				e.printStackTrace();
			}
		}

//		Rectangle rectangle = Display.getCurrent().getPrimaryMonitor().getBounds();
		synchronizeWithUIShowRefactoringPreviewWizard(changes, metaDataMap);
	}

	private void synchronizeWithUIShowRefactoringPreviewWizard(
			Map<String, List<DocumentChange>> changes, Map<String, FieldMetadata> dataMap) {

		logger.info(NLS.bind(Messages.SelectRulesWizard_end_refactoring, this.getClass().getSimpleName(),
				selectedJavaProjekt.getElementName()));
		logger.info(NLS.bind(Messages.SelectRulesWizard_rules_with_changes, selectedJavaProjekt.getElementName(),
				renameFieldsRule.getName()));

		Display.getDefault().asyncExec(() -> {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			final WizardDialog dialog = new WizardDialog(shell,
					new RenamingRulePreviewWizard(changes, dataMap, renameFieldsRule));

			// maximizes the RefactoringPreviewWizard
//			dialog.setPageSize(rectangle.width, rectangle.height);
			dialog.setPageSize(1000, 800);
			dialog.open();
		});

	}
}
