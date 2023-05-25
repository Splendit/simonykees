package eu.jsparrow.ui.wizard.projects;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.handler.AbstractRuleWizardHandler;
import eu.jsparrow.ui.handler.LoggerRuleWizardHandler;
import eu.jsparrow.ui.handler.RemoveUnusedCodeWizardHandler;
import eu.jsparrow.ui.handler.RenameFieldsRuleWizardHandler;
import eu.jsparrow.ui.handler.RunDefaultProfileHandler;
import eu.jsparrow.ui.handler.SelectRulesWizardHandler;
import eu.jsparrow.ui.wizard.projects.javaelement.IJavaElementWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectsCollector;

public class SelectSourcesToRefactorDialog extends Dialog {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private JavaProjectTreeViewWrapper javaProjectTreeVierWrapper;
	private Text textFilterProjects;
	private Text textFilterPackageRoots;
	private Text textFilterPackages;
	private Text textFilterCompilationUnits;
	private Button buttonRefactorWithDefaultProfile;
	private Button buttonSelectRulesToRefactor;
	private Button buttonRefactorWithLoggingRule;
	private Button buttonRenameFields;
	private Button buttonRemoveUnusedCode;
	private JavaProjectFilter javaProjectFilter = new JavaProjectFilter(EMPTY_STRING);
	private JavaPackageRootFilter javaPackageRootFilter = new JavaPackageRootFilter(EMPTY_STRING);
	private JavaPackageFilter javaPackageFilter = new JavaPackageFilter(EMPTY_STRING);
	private JavaFileFilter javaFileFilter = new JavaFileFilter(EMPTY_STRING);
	private AbstractRuleWizardHandler abstractRuleWizardHandler;
	private List<JavaProjectWrapper> javaProjects;

	private static GridLayout createDefaultGridLayout(int numColumns) {
		GridLayout gridLayout = new GridLayout(numColumns, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 5;
		gridLayout.horizontalSpacing = 5;
		return gridLayout;
	}

	public static void selectJavaSourcesToRefactor(Shell parentShell) {
		if (Activator.isRunning()) {
			AbstractRuleWizardHandler.openAlreadyRunningDialog();
			return;
		}

		Activator.setRunning(true);

		JavaProjectsCollector javaProjectsCollector = new JavaProjectsCollector();
		Job job = new Job("Collecting Java Sources to refactor") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				javaProjectsCollector.collectJavaProjects(monitor);
				return Status.OK_STATUS;
			}
		};

		JobChangeAdapter jobChangeAdapter = new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {

				Display.getDefault()
					.asyncExec(() -> {
						SelectSourcesToRefactorDialog selectSourcesDialog = new SelectSourcesToRefactorDialog(
								parentShell,
								javaProjectsCollector.getJavaProjectWrapperList());
						selectSourcesDialog.open();

						AbstractRuleWizardHandler ruleWizardHandler = selectSourcesDialog.getAbstractRuleWizardHandler()
							.orElse(null);
						if (ruleWizardHandler != null) {
							Set<IJavaElementWrapper> selectedWrappers = selectSourcesDialog.getSelectedWrappers();
							SelectedJavaElementsCollector collector = new SelectedJavaElementsCollector(
									selectedWrappers);
							ruleWizardHandler.execute(collector);
						} else {
							Activator.setRunning(false);
						}
					});
			}
		};
		job.addJobChangeListener(jobChangeAdapter);

		job.setUser(true);
		job.schedule();

	}

	private static Button createRefactoringRadioButton(Composite parent, String text) {
		Button radioButton = new Button(parent, SWT.RADIO);
		radioButton.setText(text);
		return radioButton;
	}

	private SelectSourcesToRefactorDialog(Shell parentShell, List<JavaProjectWrapper> javaProjects) {
		super(parentShell);
		this.javaProjects = javaProjects;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Select Java Sources To Refactor"); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite sourceSelectionComposite = new Composite(area, SWT.NONE);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = convertHeightInCharsToPixels(20);
		sourceSelectionComposite.setLayoutData(gridData);
		GridLayout gridLayout = createDefaultGridLayout(2);
		sourceSelectionComposite.setLayout(gridLayout);

		Group treeViewerGroup = new Group(sourceSelectionComposite, SWT.NONE);
		treeViewerGroup.setText("Java Sources"); //$NON-NLS-1$

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = convertWidthInCharsToPixels(65);
		gridData.heightHint = 270;
		treeViewerGroup.setLayoutData(gridData);
		gridLayout = createDefaultGridLayout(1);
		treeViewerGroup.setLayout(gridLayout);

		javaProjectTreeVierWrapper = new JavaProjectTreeViewWrapper(treeViewerGroup, javaProjects);

		Composite rightComposite = new Composite(sourceSelectionComposite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		rightComposite.setLayoutData(gridData);
		gridLayout = createDefaultGridLayout(1);
		rightComposite.setLayout(gridLayout);

		Group filter = new Group(rightComposite, SWT.NONE);
		filter.setText("Filter"); //$NON-NLS-1$

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = convertWidthInCharsToPixels(50);
		gridData.heightHint = 140;
		filter.setLayoutData(gridData);
		gridLayout = createDefaultGridLayout(1);

		filter.setLayout(gridLayout);
		textFilterProjects = createFilterTextField(filter, "Projects"); //$NON-NLS-1$
		textFilterPackageRoots = createFilterTextField(filter, "Package Roots"); //$NON-NLS-1$
		textFilterPackages = createFilterTextField(filter, "Packages"); //$NON-NLS-1$
		textFilterCompilationUnits = createFilterTextField(filter, "Java Files"); //$NON-NLS-1$

		Group refactoring = new Group(rightComposite, SWT.NONE);
		refactoring.setText("JSparrow"); //$NON-NLS-1$

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = convertWidthInCharsToPixels(50);
		gridData.heightHint = 130;
		refactoring.setLayoutData(gridData);
		gridLayout = createDefaultGridLayout(1);
		refactoring.setLayout(gridLayout);

		buttonRefactorWithDefaultProfile = createRefactoringRadioButton(refactoring, "Refactor with Default Profile"); //$NON-NLS-1$
		buttonSelectRulesToRefactor = createRefactoringRadioButton(refactoring, "Select Rules to Refactor"); //$NON-NLS-1$
		buttonRefactorWithLoggingRule = createRefactoringRadioButton(refactoring, "Refactor with Logging Rule"); //$NON-NLS-1$
		buttonRenameFields = createRefactoringRadioButton(refactoring, "Rename Fields"); //$NON-NLS-1$
		buttonRemoveUnusedCode = createRefactoringRadioButton(refactoring, "Remove Unused Code"); //$NON-NLS-1$

		buttonSelectRulesToRefactor.setSelection(true);

		return area;
	}

	protected Text createFilterTextField(Group group, String message) {
		Composite searchComposite = new Composite(group, SWT.NONE);
		searchComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		GridLayout searchCompositeGridLayout = createDefaultGridLayout(2);
		searchComposite.setLayout(searchCompositeGridLayout);
		Label label = new Label(searchComposite, SWT.NONE);
		label.setText(message);
		GridData labelGridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		labelGridData.widthHint = convertWidthInCharsToPixels(15);
		label.setLayoutData(labelGridData);
		Text filterText = new Text(searchComposite, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		filterText.setMessage("Filter " + message); //$NON-NLS-1$
		GridData searchFieldGridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		searchFieldGridData.widthHint = convertWidthInCharsToPixels(27);
		filterText.setLayoutData(searchFieldGridData);
		filterText.addModifyListener(this::textRetrievalModified);
		return filterText;
	}

	protected void textRetrievalModified(ModifyEvent modifyEvent) {
		Text source = (Text) modifyEvent.getSource();
		String searchText = source.getText();
		if (source == textFilterProjects) {
			javaProjectFilter = new JavaProjectFilter(searchText);
		} else if (source == textFilterPackageRoots) {
			javaPackageRootFilter = new JavaPackageRootFilter(searchText);
		} else if (source == textFilterPackages) {
			javaPackageFilter = new JavaPackageFilter(searchText);
		} else if (source == textFilterCompilationUnits) {
			javaFileFilter = new JavaFileFilter(searchText);
		}
		javaProjectTreeVierWrapper.setTreeViewerFilters(javaProjectFilter, javaPackageRootFilter, javaPackageFilter,
				javaFileFilter);
	}

	@Override
	protected void okPressed() {
		if (buttonRefactorWithDefaultProfile.getSelection()) {
			abstractRuleWizardHandler = new RunDefaultProfileHandler();
		} else if (buttonSelectRulesToRefactor.getSelection()) {
			abstractRuleWizardHandler = new SelectRulesWizardHandler();
		} else if (buttonRefactorWithLoggingRule.getSelection()) {
			abstractRuleWizardHandler = new LoggerRuleWizardHandler();
		} else if (buttonRenameFields.getSelection()) {
			abstractRuleWizardHandler = new RenameFieldsRuleWizardHandler();
		} else if (buttonRemoveUnusedCode.getSelection()) {
			abstractRuleWizardHandler = new RemoveUnusedCodeWizardHandler();
		}
		super.okPressed();
	}

	private Set<IJavaElementWrapper> getSelectedWrappers() {
		return javaProjectTreeVierWrapper.getSelectedWrappers();
	}

	public Optional<AbstractRuleWizardHandler> getAbstractRuleWizardHandler() {
		return Optional.ofNullable(abstractRuleWizardHandler);
	}
}
