package eu.jsparrow.ui.wizard.projects;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class SelectSourcesToRefactorDialog extends Dialog {

	private JavaProjectTreeViewWrapper javaProjectTreeVierWrapper;

	public SelectSourcesToRefactorDialog(Shell parentShell) {
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
		gridData.widthHint = convertWidthInCharsToPixels(40);
		gridData.heightHint = 160;
		treeViewerGroup.setLayoutData(gridData);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		treeViewerGroup.setLayout(gridLayout);

		javaProjectTreeVierWrapper = new JavaProjectTreeViewWrapper(treeViewerGroup);

		Group refactoring = new Group(sourceSelectionComposite, SWT.NONE);
		refactoring.setText("Refactoring with JSparrow"); //$NON-NLS-1$

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = convertWidthInCharsToPixels(40);
		gridData.heightHint = 160;
		refactoring.setLayoutData(gridData);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		refactoring.setLayout(gridLayout);

		return area;
	}

	public void setTreeViewerFilter(ViewerFilter treeviewerFilter) {
		javaProjectTreeVierWrapper.setTreeViewerFilter(null);
	}

}
