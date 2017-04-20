package at.splendit.simonykees.core.ui.dialog;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.exception.SimonykeesException;
import at.splendit.simonykees.i18n.Messages;

/**
 * Simple help dialog that gets populated with default values
 * 
 * @author Ludwig Werzowa, Martin Huter, Andreja Sambolec
 * @since 0.9
 */
public class SimonykeesMessageDialog extends MessageDialog {

	private static final Logger logger = LoggerFactory.getLogger(SimonykeesMessageDialog.class);
	
	private static final String MAIL_BUGREPORT = Messages.SimonykeesMessageDialog_bugreport_email;
	private static final String dialogTitle = Messages.aa_codename;
	private static final Image dialogTitleImage = null;
	private static final String dialogInformationMessage = Messages.HelpMessageDialog_default_message;
	private static final String dialogErrorMessage = Messages.SimonykeesMessageDialog_default_error_message;
	private static final int defaultIndex = 1;
	private static final String[] dialogButtonLabels = { Messages.ui_ok };
	private static final String splenditUrl = Messages.HelpMessageDialog_homepage_url;

	private static String messageText;

	public static boolean openDefaultHelpMessageDialog(Shell parentShell) {
		messageText = dialogInformationMessage + System.lineSeparator() + splenditUrl;
		return new SimonykeesMessageDialog(parentShell, dialogTitle, dialogTitleImage, messageText,
				MessageDialog.INFORMATION, defaultIndex, dialogButtonLabels).open() == 0;
	}

	public static boolean openMessageDialog(Shell parentShell, String message, int dialogImage) {
		messageText = message;
		return new SimonykeesMessageDialog(parentShell, dialogTitle, dialogTitleImage, messageText, dialogImage,
				defaultIndex, dialogButtonLabels).open() == 0;
	}

	public static boolean openErrorMessageDialog(Shell parentShell, SimonykeesException simonykeesException) {
		messageText = ((simonykeesException != null) ? simonykeesException.getUiMessage() : dialogErrorMessage)
				+ System.lineSeparator() + "<a>" + MAIL_BUGREPORT + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$
		return new SimonykeesMessageDialog(parentShell, dialogTitle, dialogTitleImage, messageText, MessageDialog.ERROR,
				defaultIndex, dialogButtonLabels).open() == 0;
	}

	private SimonykeesMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
			int dialogImageType, int defaultIndex, String... dialogButtonLabels) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
				defaultIndex);
		messageText = dialogMessage;
	}

	@Override
	protected Control createMessageArea(Composite composite) {
		Image image = getImage();
		if (image != null) {
			imageLabel = new Label(composite, SWT.NULL);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imageLabel);
		}
		if (message != null) {
			Link link = new Link(composite, getMessageLabelStyle());
			link.setText(messageText);
			link.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					try {
						PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(arg0.text));
					} catch (PartInitException | MalformedURLException e) {
						logger.error(Messages.SimonykeesMessageDialog_open_browser_error_message, e);
					}
				}
			});
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
					.hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
					.applyTo(link);
		}
		return composite;
	}

}
