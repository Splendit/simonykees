package at.splendit.simonykees.core.ui.preview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import at.splendit.simonykees.core.rule.impl.PublicFieldsRenamingRule;

@SuppressWarnings("restriction")
public class RenamingRulePreviewWizardPage extends WizardPage {

	Map<String, List<DocumentChange>> changes;

	private CheckboxTreeViewer viewer;
	private IChangePreviewViewer currentPreviewViewer;
	
	private List<DocumentChangeWrapper> changesWrapperList;
	private DocumentChangeWrapper selectedDocWrapper;

	public RenamingRulePreviewWizardPage(Map<String, List<DocumentChange>> changes, PublicFieldsRenamingRule rule) {
		super(rule.getName());
		setTitle(rule.getName());
		setDescription(rule.getDescription());
		this.changes = changes;

		convertChangesToDocumentChangeWrappers();

	}

	private void convertChangesToDocumentChangeWrappers() {
		changesWrapperList = new ArrayList<>();
		for (Map.Entry<String, List<DocumentChange>> entry : changes.entrySet()) {
			String declaration = entry.getKey();
			List<DocumentChange> changesForField = changes.get(declaration);
			DocumentChange parent = null;
			parent = changesForField.get(0);
			DocumentChangeWrapper dcw = new DocumentChangeWrapper(parent, null);
			for (int i = 1; i<changesForField.size(); i++) {
				DocumentChange document = changesForField.get(i);
				dcw.addChild(document);
				
			}
			
			changesWrapperList.add(dcw);
		}
		if(!changesWrapperList.isEmpty()) {
			this.selectedDocWrapper = changesWrapperList.get(0);
		}
	}

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

		createFileView(sashForm);
		createPreviewViewer(sashForm);

		/*
		 * sets height relation between children to be 1:3 when it has two
		 * children
		 */
		sashForm.setWeights(new int[] { 1, 3 });
	}

	private void createFileView(SashForm parent) {
		viewer = new CheckboxTreeViewer(parent);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ChangeElementContentProvider());
		viewer.setLabelProvider(new ChangeElementLabelProvider());
		viewer.setInput("root"); // pass a non-null that will be ignored

		// When user checks a checkbox in the tree, check all its children
		viewer.addCheckStateListener(event -> {
			// If the item is checked . . .
			if (event.getChecked()) {
				// . . . check all its children
				viewer.setSubtreeChecked(event.getElement(), true);
			}
		});
		
		viewer.addSelectionChangedListener(event -> {
			IStructuredSelection sel = (IStructuredSelection) event.getSelection();
			if(sel.size() == 1) {
				DocumentChangeWrapper newSelection = (DocumentChangeWrapper) sel.getFirstElement();
				if (!newSelection.equals(selectedDocWrapper)) {
					selectedDocWrapper = newSelection;
					populatePreviewViewer();
				}
			}
		});
		
		populateFileView();
	}

	private void populateFileView() {
		DocumentChangeWrapper[] changesArray = changesWrapperList.toArray(new DocumentChangeWrapper[]{});
		viewer.setInput(changesArray);
	}

	private void createPreviewViewer(SashForm parent) {

		// GridData works with GridLayout
		GridData gridData = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gridData);

		currentPreviewViewer = new TextEditChangePreviewViewer();
		currentPreviewViewer.createControl(parent);

		populatePreviewViewer();
	}

	private void populatePreviewViewer() {
		if (this.selectedDocWrapper != null) {
			DocumentChange docChange = selectedDocWrapper.getDocumentChange();
			ChangePreviewViewerInput viewerInput = TextEditChangePreviewViewer.createInput(docChange);
			currentPreviewViewer.setInput(viewerInput);
		}
	}

}
