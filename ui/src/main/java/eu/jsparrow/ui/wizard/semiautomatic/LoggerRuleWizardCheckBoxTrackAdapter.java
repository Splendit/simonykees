package eu.jsparrow.ui.wizard.semiautomatic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.i18n.Messages;

/**
 * A {@link MouseTrackAdapter} for showing a popup on hover. 
 *  
 * @author Ardit Ymeri
 * @since 2.3.0
 *
 */
public class LoggerRuleWizardCheckBoxTrackAdapter extends MouseTrackAdapter {
	private Shell popup;
	private String popupDescription;
	private Control parent;
	private String before;
	private String after;
	
	/**
	 * Creates a {@link MouseTrackAdapter} for showing a popup on hover.
	 * 
	 * @param description
	 *            the description on the popup.
	 * @param before
	 *            code example before applying the rule
	 * @param after
	 *            code example after applying the rule
	 * @param parent
	 *            parent control of the popup
	 */
	public LoggerRuleWizardCheckBoxTrackAdapter(String description, String before, String after, Control parent) {
		this.popupDescription = description;
		this.parent = parent;
		this.before = before;
		this.after = after;
	}
	
	@Override
	public void mouseEnter(MouseEvent e) {
		showPopup(popupDescription, before, after, parent);
	}
	
	@Override
	public void mouseExit(MouseEvent e) {
		closePopup();
	}
	
	private void showPopup(String popupDescription, String before, String after, Control parent) {
		if (popup != null) {
			return;
		}

		popup = new Shell(parent.getShell()
			.getDisplay(), SWT.RESIZE);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 1;
		gridData.widthHint = 400;

		popup.setLayoutData(gridData);
		popup.setLayout(new GridLayout(1, false));

		Label description = new Label(popup, SWT.WRAP | SWT.LEFT);
		final GridData data = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
		data.horizontalSpan = 1;
		data.widthHint = 380;
		description.setLayoutData(data);
		description.setText(popupDescription);

		RGB rgbWhite = new RGB(252, 252, 252);
		RGB rgbBlack = new RGB(60, 60, 60);

		FontData monospaceFontData = new FontData("Monospace", 9, SWT.NONE); //$NON-NLS-1$
		Text codeExampleBefore = new Text(popup, SWT.MULTI | SWT.BORDER);
		codeExampleBefore.setFont(new Font(codeExampleBefore.getDisplay(), monospaceFontData));
		codeExampleBefore.setEditable(false);
		codeExampleBefore.setText(before);
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, true);
		gridData.horizontalSpan = 1;
		gridData.widthHint = 310;
		codeExampleBefore.setLayoutData(gridData);
		// a work around for disabling the cursor
		codeExampleBefore.setEnabled(false);
		codeExampleBefore.setBackground(new Color(Display.getCurrent(), rgbWhite));
		codeExampleBefore.setForeground(new Color(Display.getCurrent(), rgbBlack));

		Label willBeTransformedToLabel = new Label(popup, SWT.NONE);
		willBeTransformedToLabel.setText(Messages.LoggerRuleWizardPage_will_be_transformed_to);
		willBeTransformedToLabel.setFocus();

		Text codeExampleAfter = new Text(popup, SWT.MULTI | SWT.BORDER);
		codeExampleAfter.setEditable(false);
		codeExampleAfter.setText(after);
		codeExampleAfter.setFont(new Font(codeExampleAfter.getDisplay(), monospaceFontData));
		gridData = new GridData(SWT.FILL, SWT.CENTER, false, true);
		gridData.horizontalSpan = 1;
		gridData.widthHint = 310;
		codeExampleAfter.setLayoutData(gridData);
		// a work around for disabling the cursor
		codeExampleAfter.setEnabled(false);
		codeExampleAfter.setBackground(new Color(Display.getCurrent(), rgbWhite));
		codeExampleAfter.setForeground(new Color(Display.getCurrent(), rgbBlack));

		popup.setFocus();
		popup.pack();
		popup.open();

	}
	
	private void closePopup() {
		if(popup != null && !popup.isDisposed()) {
			popup.close();
			popup = null;
		}
	}
}