/**
 * 
 */
package at.splendit.simonykees.core.ui;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import at.splendit.simonykees.core.rule.RefactoringRule;

@SuppressWarnings("restriction")
public class RefactoringPreviewWizardPage extends WizardPage {
	
	private RefactoringRule<? extends ASTVisitor> refactoringRule;

	public RefactoringPreviewWizardPage(RefactoringRule<? extends ASTVisitor> rule) {
		super(rule.getName());
		setTitle(rule.getName()); // FIXME
		setDescription(rule.getDescription());
		this.refactoringRule = rule;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0; // margin from TextEditChangePreviewViewer to Composite
		layout.marginWidth = 0;
		container.setLayout(layout); // without setting the layout, nothing displays

		setControl(container);
		
		SashForm sashForm = new SashForm(container, SWT.VERTICAL);
		
//		ViewerPane
		TreeViewer treeViewer = new TreeViewer(sashForm);
		CompilationUnitContentProvider contentProvider = new CompilationUnitContentProvider();
		
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setInput(refactoringRule.getDocumentChanges().keySet()); 
		
		GridData gridData = new GridData(GridData.FILL_BOTH); // GridData works with GridLayout
//		gridData.widthHint = convertWidthInCharsToPixels(80);
		sashForm.setLayoutData(gridData);

		TextEditChangePreviewViewer viewer = new TextEditChangePreviewViewer();
		viewer.createControl(sashForm);
		
		// FIXME show more than just the first element
		// FIXME handle empty changes for a rule (changes only get created if there is a change)
		viewer.setInput(TextEditChangePreviewViewer.createInput(refactoringRule.getDocumentChanges().values().stream().findFirst().get()));

	}

}
