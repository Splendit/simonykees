package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IWorkspace;
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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.Wizard;
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
	private List<FieldMetadata> todosMetadata;
	private PublicFieldsRenamingRule renameFieldsRule;

	public RenameFieldsRuleWizard(List<IJavaElement> selectedJavaElements) {
		super();
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
				
				// TODO get scope from model, if project leave as is, if
				// workspace, get
				// all projects
				FieldDeclarationASTVisitor visitor;
				if (Messages.RenameFieldsRuleWizardPageModel_scopeOption_project.equals(model.getSearchScope())) {
					IJavaElement[] scope = { selectedJavaProjekt };
					visitor = new FieldDeclarationASTVisitor(scope);
				} else {
					IWorkspace workspace = selectedJavaProjekt.getProject().getWorkspace();
					IJavaElement[] scope = (IJavaElement[]) workspace.getRoot().getProjects();
					visitor = new FieldDeclarationASTVisitor(scope);
				}
				visitor.setRenamePrivateField(model.getFieldTypes()
						.contains(Messages.RenameFieldsRuleWizardPageModel_typeOption_privateFields));
				visitor.setRenameProtectedField(model.getFieldTypes()
						.contains(Messages.RenameFieldsRuleWizardPageModel_typeOption_protectedFields));
				visitor.setRenamePackageProtectedField(model.getFieldTypes()
						.contains(Messages.RenameFieldsRuleWizardPageModel_typeOption_packageProtectedFields));
				visitor.setRenamePublicField(model.getFieldTypes()
						.contains(Messages.RenameFieldsRuleWizardPageModel_typeOption_publicFields));
				visitor.setUppercaseAfterUnderscore(model.setUpperCaseForUnderscoreReplacementOption());
				visitor.setUppercaseAfterDollar(model.setUpperCaseForDollarReplacementOption());
				visitor.setAddTodo(model.isAddTodoComments());

				List<ICompilationUnit> result = new ArrayList<>();

				try {
					RefactoringUtil.collectICompilationUnits(result, selectedJavaElements, monitor);
					if (result.isEmpty()) {
						logger.warn(ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found);
						WizardMessageDialog.synchronizeWithUIShowInfo(new RefactoringException(
								ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found,
								ExceptionMessages.RefactoringPipeline_user_warn_no_compilation_units_found));
						return Status.CANCEL_STATUS;

					}
				} catch (JavaModelException e) {
					logger.error(e.getMessage(), e);
					WizardMessageDialog.synchronizeWithUIShowInfo(new RefactoringException(
							ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
							ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e));
					return Status.CANCEL_STATUS;
				}

				/*
				 * list with compilation units with compilation error, which
				 * should be excluded from scope
				 */
				List<ICompilationUnit> containingErrorList = new ArrayList<>();
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
				}
				if (!containingErrorList.isEmpty()) {
					synchronizeWithUIShowCompilationErrorMessage(containingErrorList, monitor, visitor);
				} else {
					searchScopeAndPrepareRefactoringStates(monitor, visitor);
				}

				return Status.OK_STATUS;
			}
		};

		job.setUser(true);
		job.schedule();

		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult().isOK() && canRefactor) {
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

										// TODO create and show preview wizard
										// for
										// multi file changing rule
										// TODO use this below to display
										// changes on
										// preview

										for (FieldMetadata data : metadata) {

											try {
												String newIdentifier = data.getNewIdentifier();
												SimpleName oldName = data.getFieldDeclaration().getName();
												String oldIdentifier = oldName.getIdentifier();
												data.getCompilationUnit().getJavaElement();
												List<DocumentChange> docsChanges = renameFieldsRule
														.computeDocumentChangesPerFiled(data);
											} catch (JavaModelException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										}

										// Rectangle rectangle =
										// Display.getCurrent().getPrimaryMonitor().getBounds();
										// synchronizeWithUIShowRefactoringPreviewWizard(refactoringPipeline,
										// rectangle);

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

					refactorJob.setUser(true);
					refactorJob.schedule();
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
	private void synchronizeWithUIShowCompilationErrorMessage(List<ICompilationUnit> containingErrorList,
			IProgressMonitor monitor, FieldDeclarationASTVisitor visitor) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				CompilationErrorsMessageDialog dialog = new CompilationErrorsMessageDialog(shell);
				dialog.create();
				dialog.setTableViewerInput(containingErrorList);
				dialog.open();
				if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
					searchScopeAndPrepareRefactoringStates(monitor, visitor);
				} else {
					canRefactor = false;
					Activator.setRunning(false);
				}
			}
		});
	}

	boolean canRefactor = true;

	private void searchScopeAndPrepareRefactoringStates(IProgressMonitor monitor, FieldDeclarationASTVisitor visitor) {

		Set<IJavaElement> targetJavaElements = visitor.getTargetIJavaElements();
		if (targetJavaElements.isEmpty()) {
			WizardMessageDialog.synchronizeWithUIShowWarningNoComlipationUnitDialog();
			canRefactor = false;
			return;
		}

		List<ICompilationUnit> targetCompilationUnits = new ArrayList<>();

		metadata = visitor.getFieldMetadata();
		todosMetadata = visitor.getUnmodifiableFieldMetadata();

		try {
			RefactoringUtil.collectICompilationUnits(targetCompilationUnits,
					targetJavaElements.stream().collect(Collectors.toList()), monitor);
			if (targetCompilationUnits.isEmpty()) {
				logger.warn(ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found);
				WizardMessageDialog.synchronizeWithUIShowInfo(
						new RefactoringException(ExceptionMessages.RefactoringPipeline_warn_no_compilation_units_found,
								ExceptionMessages.RefactoringPipeline_user_warn_no_compilation_units_found));
				canRefactor = false;
				return;
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
			WizardMessageDialog.synchronizeWithUIShowInfo(
					new RefactoringException(ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
							ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed, e));
			canRefactor = false;
			return;
		}

		renameFieldsRule = new PublicFieldsRenamingRule(PublicFieldsRenamingASTVisitor.class, metadata, todosMetadata);
		final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = Arrays.asList(renameFieldsRule);

		refactoringPipeline = new RefactoringPipeline();
		refactoringPipeline.setRules(rules);

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
		}

		refactoringPipeline.setRefactoringStates(refactoringStates);
	}
}
