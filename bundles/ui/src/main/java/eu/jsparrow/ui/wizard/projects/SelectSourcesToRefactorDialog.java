package eu.jsparrow.ui.wizard.projects;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.ui.wizard.projects.javaelement.AbstractJavaElementWrapper;

public class SelectSourcesToRefactorDialog extends Dialog {

	private JavaProjectTreeViewWrapper javaProjectTreeVierWrapper;
	private Map<IJavaProject, List<IJavaElement>> selectedJavaElementsMapping;
	private Button buttonRefactorWithDefaultRule;
	private Button buttonSelectRulesToRefactor;
	private Button buttonRefactorWithLoggingRule;
	private Button buttonRenameFields;
	private Button buttonRemoveUnusedCode;
	private boolean flagRefactorWithDefaultRule;
	private boolean flagSelectRulesToRefactor;
	private boolean flagRefactorWithLoggingRule;
	private boolean flagRenameFields;
	private boolean flagRemoveUnusedCode;

	public static void selectJavaSourcesToRefactor(Shell parentShell) {
		SelectSourcesToRefactorDialog selectSourcesDialog = new SelectSourcesToRefactorDialog(
				Display.getDefault()
					.getActiveShell());
		selectSourcesDialog.open();

		Set<AbstractJavaElementWrapper> selectedWrappers = selectSourcesDialog.getSelectedWrappers();
		SelectedJavaElementsCollector collector = new SelectedJavaElementsCollector(selectedWrappers);
		if (selectSourcesDialog.isFlagRefactorWithDefaultRule()) {
			refactorWithDefaultRule(collector);
		} else if (selectSourcesDialog.isFlagSelectRulesToRefactor()) {
			selectRulesToRefactor(collector);
		} else if (selectSourcesDialog.isFlagRefactorWithLoggingRule()) {
			useLoggingRule(collector);
		} else if (selectSourcesDialog.isFlagRenameFields()) {
			useRenameFieldsRule(collector);
		} else if (selectSourcesDialog.isFlagRemoveUnusedCode()) {
			removeUnusedCode(collector);
		}
	}

	public static void refactorWithDefaultRule(SelectedJavaElementsCollector collector) {
		Map<IJavaProject, List<IJavaElement>> selectedJavaElements = collector.getSelectedJavaElements();
		selectedJavaElements.size();
	}

	public static void selectRulesToRefactor(SelectedJavaElementsCollector collector) {
		Map<IJavaProject, List<IJavaElement>> selectedJavaElements = collector.getSelectedJavaElements();
		selectedJavaElements.size();
	}

	public static void useLoggingRule(SelectedJavaElementsCollector collector) {
		Map<IJavaProject, List<IJavaElement>> selectedJavaElements = collector.getSelectedJavaElements();
		selectedJavaElements.size();
	}

	public static void useRenameFieldsRule(SelectedJavaElementsCollector collector) {
		Map<IJavaProject, List<IJavaElement>> selectedJavaElements = collector.getSelectedJavaElements();
		selectedJavaElements.size();
	}

	public static void removeUnusedCode(SelectedJavaElementsCollector collector) {
		Map<IJavaProject, List<IJavaElement>> selectedJavaElements = collector.getSelectedJavaElements();
		selectedJavaElements.size();
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

		buttonRefactorWithDefaultRule = createRefactoringRadioButton(refactoring, "Refactor with Default Profile"); //$NON-NLS-1$
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

		Set<AbstractJavaElementWrapper> selectedWrappers = getSelectedWrappers();
		SelectedJavaElementsCollector collector = new SelectedJavaElementsCollector(selectedWrappers);

		selectedJavaElementsMapping = collector.getSelectedJavaElements();

		flagRefactorWithDefaultRule = buttonRefactorWithDefaultRule.getSelection();
		flagSelectRulesToRefactor = buttonSelectRulesToRefactor.getSelection();
		flagRefactorWithLoggingRule = buttonRefactorWithLoggingRule.getSelection();
		flagRenameFields = buttonRenameFields.getSelection();
		flagRemoveUnusedCode = buttonRemoveUnusedCode.getSelection();

		super.okPressed();
	}

	private Set<AbstractJavaElementWrapper> getSelectedWrappers() {
		return javaProjectTreeVierWrapper.getSelectedWrappers();
	}

	public Map<IJavaProject, List<IJavaElement>> getSelectedJavaElementsMapping() {
		return selectedJavaElementsMapping;
	}

	public boolean isFlagRefactorWithDefaultRule() {
		return flagRefactorWithDefaultRule;
	}

	public boolean isFlagSelectRulesToRefactor() {
		return flagSelectRulesToRefactor;
	}

	public boolean isFlagRefactorWithLoggingRule() {
		return flagRefactorWithLoggingRule;
	}

	public boolean isFlagRenameFields() {
		return flagRenameFields;
	}

	public boolean isFlagRemoveUnusedCode() {
		return flagRemoveUnusedCode;
	}
}
