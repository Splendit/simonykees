package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.eclipse.jface.dialogs.MessageDialog;
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
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
import at.splendit.simonykees.core.ui.wizard.impl.WizardMessageDialog;
import at.splendit.simonykees.core.util.RefactoringUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.visitor.renaming.FieldDeclarationASTVisitor;
import at.splendit.simonykees.core.visitor.renaming.FieldMetadata;
import at.splendit.simonykees.core.visitor.renaming.PublicFieldsRenamingASTVisitor;
import at.splendit.simonykees.i18n.Messages;

public class RenameFieldsRuleWizard extends Wizard {

	private static final Logger logger = LoggerFactory.getLogger(RenameFieldsRuleWizard.class);

	private RenameFieldsRuleWizardPage page;
	private RenameFieldsRuleWizardPageModel model;
	private RenameFieldsRuleWizardPageControler controler;

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
		controler = new RenameFieldsRuleWizardPageControler(model);
		page = new RenameFieldsRuleWizardPage(model, controler);
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
		} else {
			return true;
		}
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
				IJavaElement[] scope = { selectedJavaProjekt };
				FieldDeclarationASTVisitor visitor = new FieldDeclarationASTVisitor(scope);
				List<ICompilationUnit> result = new ArrayList<>();

				// TODO check for compilation errors and display message
				try {
					RefactoringUtil.collectICompilationUnits(result, selectedJavaElements, monitor);
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				for (ICompilationUnit iCu : result) {
					if (!iCu.getJavaProject().equals(selectedJavaProjekt)) {
						synchronizeWithUIShowMultiprojectMessage();
						Activator.setRunning(false);
						return Status.CANCEL_STATUS;

					}
					CompilationUnit cu = RefactoringUtil.parse(iCu);
					cu.accept(visitor);
				}

				metadata = visitor.getFieldMetadata();
				todosMetadata = visitor.getUnmodifiableFieldMetadata();

				Set<IJavaElement> targetJavaElements = visitor.getTargetIJavaElements();
				List<ICompilationUnit> targetCompilationUnits = new ArrayList<>();

				try {
					RefactoringUtil.collectICompilationUnits(targetCompilationUnits,
							targetJavaElements.stream().collect(Collectors.toList()), monitor);
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				renameFieldsRule = new PublicFieldsRenamingRule(PublicFieldsRenamingASTVisitor.class, metadata,
						todosMetadata);
				final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = Arrays
						.asList(renameFieldsRule);

				refactoringPipeline = new RefactoringPipeline();
				refactoringPipeline.setRules(rules);

				List<RefactoringState> refactoringStates = new ArrayList<>();
				for (ICompilationUnit compilationUnit : targetCompilationUnits) {
					try {
						refactoringStates
								.add(new RefactoringState(compilationUnit, compilationUnit.getWorkingCopy(null)));
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				refactoringPipeline.setRefactoringStates(refactoringStates);

				return Status.OK_STATUS;
			}
		};

		job.setUser(true);
		job.schedule();

		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult().isOK()) {
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
			RefactoringPipeline refactoringPipeline, List<IJavaElement> selectedJavaElements,
			PublicFieldsRenamingRule renamingRule, IJavaProject selectedJavaProjekt) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				CompilationErrorsMessageDialog dialog = new CompilationErrorsMessageDialog(shell);
				dialog.create();
				dialog.setTableViewerInput(containingErrorList);
				dialog.open();
				if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
					if (!refactoringPipeline.hasRefactoringStates()) {
						synchronizeWithUIShowWarningNoComlipationUnitDialog();
					}
				} else {
					Activator.setRunning(false);
				}
			}
		});
	}

	/**
	 * Method used to open MessageDialog informing the user that selection
	 * contains no Java files without compilation error from non UI thread
	 */
	private void synchronizeWithUIShowWarningNoComlipationUnitDialog() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				SimonykeesMessageDialog.openMessageDialog(shell, Messages.SelectRulesWizardHandler_noFileWithoutError,
						MessageDialog.INFORMATION);

				Activator.setRunning(false);
			}

		});
	}

	private void synchronizeWithUIShowMultiprojectMessage() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				SimonykeesMessageDialog.openMessageDialog(shell,
						Messages.SelectRulesWizardHandler_multipleProjectsWarning, MessageDialog.WARNING);
			}
		});
	}
}
