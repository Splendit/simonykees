package at.splendit.simonykees.core.dialogs;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ChangePreviewWizardPage extends WizardPage {
	
	private TextEditBasedChange textEditBasedChange;
	
	/**
	 * Create the wizard.
	 */
	public ChangePreviewWizardPage(TextEditBasedChange textEditBasedChange) {
		super("wizardPage");
		setTitle("Wizard Page title");
		setDescription("Wizard Page description");
		this.textEditBasedChange = textEditBasedChange;
	}

	@Override
	public void createControl(Composite parent) {
//		Composite container = new Composite(parent, SWT.NULL);
		
		ViewForm viewForm = new ViewForm(parent, SWT.BORDER_SOLID);
//		PageBook viewForm = new PageBook(parent, SWT.BORDER_SOLID);
		viewForm.setBounds(10, 10, 548, 296);
		Display display = Display.getCurrent();
		RGB rgb = new RGB(128, 128, 128);
		viewForm.setBackground(new Color(display, rgb));
		

		
//		ViewForm viewForm = new ViewForm(parent, INFORMATION);
		TextEditChangePreviewViewer viewer = new TextEditChangePreviewViewer();
		viewer.createControl(viewForm);
//		viewer.setInput(new ChangePreviewViewerInput(textEditBasedChange));
		viewer.setInput(TextEditChangePreviewViewer.createInput(textEditBasedChange));

		setControl(viewForm);
		
	}
	
}