package at.splendit.simonykees.core.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction") // XXX TextEditChangePreviewViewer is internal, hence the warning
public class ChangePreviewWizardPage extends WizardPage {

	private DocumentChange documentChange;

	/**
	 * Create the wizard.
	 */
	public ChangePreviewWizardPage(DocumentChange documentChange) {
		super("wizardPage");
		setTitle("Wizard Page title");
		setDescription("Wizard Page description");
		this.documentChange = documentChange;
	}

	@Override
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0; // margin from TextEditChangePreviewViewer to Composite
		layout.marginWidth = 0;
		container.setLayout(layout); // without setting the layout, nothing displays

		setControl(container);

		SashForm sashForm = new SashForm(container, SWT.VERTICAL);

		GridData gridData = new GridData(GridData.FILL_BOTH); // GridData works with GridLayout
//		gridData.widthHint = convertWidthInCharsToPixels(80);
		sashForm.setLayoutData(gridData);

		TextEditChangePreviewViewer viewer = new TextEditChangePreviewViewer();
		viewer.createControl(sashForm);
		// viewer.setInput(new ChangePreviewViewerInput(textEditBasedChange));
		viewer.setInput(TextEditChangePreviewViewer.createInput(documentChange));

	}

}