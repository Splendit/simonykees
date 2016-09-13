/**
 * 
 */
package at.splendit.simonykees.core.ui;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import at.splendit.simonykees.core.rule.RefactoringRule;

@SuppressWarnings("restriction")
public class RefactoringPreviewWizardPage extends WizardPage {

	private RefactoringRule<? extends ASTVisitor> refactoringRule;
	private CompilationUnitNode currentCompilationUnitNode;
	private IChangePreviewViewer currentPreviewViewer;

	public RefactoringPreviewWizardPage(RefactoringRule<? extends ASTVisitor> rule) {
		super(rule.getName());
		setTitle(rule.getName()); // FIXME
		setDescription(rule.getDescription());
		this.refactoringRule = rule;

		this.currentCompilationUnitNode = new CompilationUnitNode(
				refactoringRule.getDocumentChanges().keySet().stream().findFirst().orElse(null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.
	 * widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();

		// margin from TextEditChangePreviewViewer to Composite
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// without setting the layout, nothing displays
		container.setLayout(layout);

		setControl(container);

		SashForm sashForm = new SashForm(container, SWT.VERTICAL);

		TreeViewer treeViewer = new TreeViewer(sashForm);
		CompilationUnitContentProvider contentProvider = new CompilationUnitContentProvider();

		treeViewer.setContentProvider(contentProvider);
		treeViewer.addSelectionChangedListener(createSelectionChangedListener());
		treeViewer.setInput(refactoringRule.getDocumentChanges().keySet());

		// GridData works with GridLayout
		GridData gridData = new GridData(GridData.FILL_BOTH);
		sashForm.setLayoutData(gridData);

		currentPreviewViewer = new TextEditChangePreviewViewer();
		currentPreviewViewer.createControl(sashForm);

		populatePreviewViewer();

	}

	private ISelectionChangedListener createSelectionChangedListener() {
		return new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();

				if (sel.size() == 1) {
					CompilationUnitNode newSelection = (CompilationUnitNode) sel.getFirstElement();
					if (!newSelection.equals(currentCompilationUnitNode)) {
						currentCompilationUnitNode = newSelection;
						populatePreviewViewer();
					}
				}
			}
		};
	}
	
	private void populatePreviewViewer() {
		currentPreviewViewer.setInput(TextEditChangePreviewViewer.createInput(getCurrentDocumentChange()));
	}

	private DocumentChange getCurrentDocumentChange() {
		return refactoringRule.getDocumentChanges().get(currentCompilationUnitNode.getCompilationUnit());
	}

}
