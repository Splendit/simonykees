package eu.jsparrow.ui.startup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormText;

public class WelcomePage extends FormPage {

	public static final String PAGE_ID = "eu.jsparrow.ui.startup.page.overview";

	
	public WelcomePage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		Composite body = managedForm.getForm().getBody();
		body.setLayout(new GridLayout(1,true));
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	 
		FormText text = new FormText(body, SWT.WRAP | SWT.MULTI | SWT.NO_BACKGROUND | SWT.NO_FOCUS);
		text.setText("Hello from jSparrow", false, false);
		text.setVisible(true);		
	}

}
