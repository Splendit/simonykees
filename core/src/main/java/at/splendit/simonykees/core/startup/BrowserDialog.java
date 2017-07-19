package at.splendit.simonykees.core.startup;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceManager;
import at.splendit.simonykees.i18n.Messages;

public class BrowserDialog extends Dialog {

	protected BrowserDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 750;
		data.heightHint = 650;
		composite.setLayoutData(data);

		Browser browser = new Browser(composite, SWT.NONE);

		try {
			browser.setText(getValue(BrowserDialog.class.getResourceAsStream("/welcome-intro.html"))); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
		}
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return composite;
	}

	private String getValue(InputStream inputStream) throws IOException {
		final int bufferSize = 1024;
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(inputStream, "UTF-8"); //$NON-NLS-1$
		for (;;) {
			int rsz = in.read(buffer, 0, buffer.length);
			if (rsz < 0)
				break;
			out.append(buffer, 0, rsz);
		}
		return out.toString();

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button disableIntro = new Button(parent, SWT.CHECK);
		disableIntro.setText(Messages.BrowserDialog_dontShowIntroText);
		disableIntro.setFont(JFaceResources.getDialogFont());
		disableIntro.setData(Integer.valueOf(5));
		disableIntro.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// if button is selected, intro dialog should not be shown again
				Button btn = (Button) e.getSource();
				SimonykeesPreferenceManager.setEnableIntro(!btn.getSelection());
			}
		});
		setButtonLayoutData(disableIntro);

		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.BrowserDialog_introTitle);
	}

	@Override
	public void okPressed() {
		close();
	}

}
