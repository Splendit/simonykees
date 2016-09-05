package at.splendit.simonykees.core.dialogs;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
		
		Composite container= new Composite(parent, SWT.NONE);
		
		container.setBounds(10, 10, 548, 296);
		Display display = Display.getCurrent();
		RGB rgb = new RGB(128, 128, 128);
		container.setBackground(new Color(display, rgb));
		
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0; layout.marginWidth= 0;
		container.setLayout(layout);

		SashForm sashForm= new SashForm(container, SWT.VERTICAL);
		
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(80);
		sashForm.setLayoutData(gd);
		
		setControl(container);
		
		TextEditChangePreviewViewer viewer = new TextEditChangePreviewViewer();
		viewer.createControl(sashForm);
//		viewer.setInput(new ChangePreviewViewerInput(textEditBasedChange));
		viewer.setInput(TextEditChangePreviewViewer.createInput(textEditBasedChange));
		
	}
	
}