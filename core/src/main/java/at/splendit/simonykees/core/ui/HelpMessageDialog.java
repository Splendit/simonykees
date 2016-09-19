package at.splendit.simonykees.core.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import at.splendit.simonykees.core.i18n.Messages;

/**
 * Simple help dialog that gets populated with default values
 */
public class HelpMessageDialog extends MessageDialog {

	private static final String dialogTitle = Messages.aa_codename;
	private static final Image dialogTitleImage = null;
	private static final String dialogMessage = Messages.HelpMessageDialog_default_message;
	private static final int dialogImageType = MessageDialog.INFORMATION;
	private static final int defaultIndex = 1;
	private static final String[] dialogButtonLabels = { Messages.ui_ok };
	private static final String splenditUrl = Messages.HelpMessageDialog_homepage_url;

	public static void openDefaultHelpMessageDialog(Shell parentShell) {
		new HelpMessageDialog(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType,
				defaultIndex, dialogButtonLabels).open();
	}

	private HelpMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
			int dialogImageType, int defaultIndex, String... dialogButtonLabels) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, defaultIndex,
				dialogButtonLabels);
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Link link = new Link(parent, SWT.NONE | SWT.RIGHT);
		link.setText(splenditUrl);
		return link;
	}
	
}
