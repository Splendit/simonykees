package eu.jsparrow.ui.wizard.projects;

import java.util.Optional;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.handler.AbstractRuleWizardHandler;
import eu.jsparrow.ui.handler.LoggerRuleWizardHandler;
import eu.jsparrow.ui.handler.RemoveUnusedCodeWizardHandler;
import eu.jsparrow.ui.handler.RenameFieldsRuleWizardHandler;
import eu.jsparrow.ui.handler.RunDefaultProfileHandler;
import eu.jsparrow.ui.handler.SelectRulesWizardHandler;
import eu.jsparrow.ui.wizard.projects.javaelement.AbstractJavaElementWrapper;

public class SelectSourcesToRefactorDialog extends Dialog {

	private JavaProjectTreeViewWrapper javaProjectTreeVierWrapper;
	private Button buttonRefactorWithDefaultProfile;
	private Button buttonSelectRulesToRefactor;
	private Button buttonRefactorWithLoggingRule;
	private Button buttonRenameFields;
	private Button buttonRemoveUnusedCode;
	private AbstractRuleWizardHandler abstractRuleWizardHandler;

	public static void selectJavaSourcesToRefactor(Shell parentShell) {
		if (Activator.isRunning()) {
			AbstractRuleWizardHandler.openAlreadyRunningDialog();
			return;
		}

		Activator.setRunning(true);
		SelectSourcesToRefactorDialog selectSourcesDialog = new SelectSourcesToRefactorDialog(parentShell);
		selectSourcesDialog.open();

		AbstractRuleWizardHandler ruleWizardHandler = selectSourcesDialog.getAbstractRuleWizardHandler()
			.orElse(null);
		if (ruleWizardHandler != null) {
			Set<AbstractJavaElementWrapper> selectedWrappers = selectSourcesDialog.getSelectedWrappers();
			SelectedJavaElementsCollector collector = new SelectedJavaElementsCollector(selectedWrappers);
			ruleWizardHandler.execute(collector);
		} else {
			Activator.setRunning(false);
		}
	}

	private static Button createRefactoringRadioButton(Composite parent, String text) {
		Button radioButton = new Button(parent, SWT.RADIO);
		radioButton.setText(text);
		return radioButton;
	}

	private SelectSourcesToRefactorDialog(Shell parentShell) {
		super(parentShell);
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
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		sourceSelectionComposite.setLayout(gridLayout);

		Group treeViewerGroup = new Group(sourceSelectionComposite, SWT.NONE);
		treeViewerGroup.setText("Java Sources"); //$NON-NLS-1$

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = convertWidthInCharsToPixels(65);
		gridData.heightHint = 200;
		treeViewerGroup.setLayoutData(gridData);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		treeViewerGroup.setLayout(gridLayout);

		javaProjectTreeVierWrapper = new JavaProjectTreeViewWrapper(treeViewerGroup);

		Group refactoring = new Group(sourceSelectionComposite, SWT.NONE);
		refactoring.setText("JSparrow"); //$NON-NLS-1$

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = convertWidthInCharsToPixels(35);
		gridData.heightHint = 200;
		refactoring.setLayoutData(gridData);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		refactoring.setLayout(gridLayout);

		buttonRefactorWithDefaultProfile = createRefactoringRadioButton(refactoring, "Refactor with Default Profile"); //$NON-NLS-1$
		buttonSelectRulesToRefactor = createRefactoringRadioButton(refactoring, "Select Rules to Refactor"); //$NON-NLS-1$
		buttonRefactorWithLoggingRule = createRefactoringRadioButton(refactoring, "Refactor with Logging Rule"); //$NON-NLS-1$
		buttonRenameFields = createRefactoringRadioButton(refactoring, "Rename Fields"); //$NON-NLS-1$
		buttonRemoveUnusedCode = createRefactoringRadioButton(refactoring, "Remove Unused Code"); //$NON-NLS-1$

		buttonSelectRulesToRefactor.setSelection(true);

		return area;
	}

	public void setTreeViewerFilter(ViewerFilter treeviewerFilter) {
		javaProjectTreeVierWrapper.setTreeViewerFilter(treeviewerFilter);
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

	private Set<AbstractJavaElementWrapper> getSelectedWrappers() {
		return javaProjectTreeVierWrapper.getSelectedWrappers();
	}

	public Optional<AbstractRuleWizardHandler> getAbstractRuleWizardHandler() {
		return Optional.ofNullable(abstractRuleWizardHandler);
	}
}
