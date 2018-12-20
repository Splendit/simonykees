package eu.jsparrow.ui.dialog;

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

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.SimonykeesException;

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

	private String messageText;

	private SimonykeesMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
			int dialogImageType, int defaultIndex, String... dialogButtonLabels) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
				defaultIndex);
		messageText = dialogMessage;
	}

	public static boolean openDefaultHelpMessageDialog(Shell parentShell) {
		String messageText = dialogInformationMessage + System.lineSeparator() + splenditUrl;
		return new SimonykeesMessageDialog(parentShell, dialogTitle, dialogTitleImage, messageText,
				MessageDialog.INFORMATION, defaultIndex, dialogButtonLabels).open() == 0;
	}

	public static boolean openMessageDialog(Shell parentShell, String message, int dialogImage) {
		return openMessageDialog(parentShell, message, dialogImage, dialogTitle);
	}

	public static boolean openMessageDialog(Shell parentShell, String message, int dialogImage, String title) {
		String messageText = message;
		return new SimonykeesMessageDialog(parentShell, title, dialogTitleImage, messageText, dialogImage, defaultIndex,
				dialogButtonLabels).open() == 0;
	}

	public static boolean openConfirmDialog(Shell parentShell, String message) {
		return openConfirmDialog(parentShell, message, dialogTitle);
	}

	public static boolean openConfirmDialog(Shell parentShell, String message, String title) {
		String messageText = message;
		return new SimonykeesMessageDialog(parentShell, title, dialogTitleImage, messageText, MessageDialog.CONFIRM,
				defaultIndex, new String[] { Messages.ui_cancel, Messages.ui_ok }).open() == 1;
	}

	public static boolean openErrorMessageDialog(Shell parentShell, SimonykeesException simonykeesException) {
		return openErrorMessageDialog(parentShell, simonykeesException, dialogTitle);
	}

	public static boolean openErrorMessageDialog(Shell parentShell, SimonykeesException simonykeesException,
			String title) {
		String messageText = ((simonykeesException != null) ? simonykeesException.getUiMessage() : dialogErrorMessage)
				+ System.lineSeparator() + MAIL_BUGREPORT;
		return new SimonykeesMessageDialog(parentShell, title, dialogTitleImage, messageText, MessageDialog.ERROR,
				defaultIndex, dialogButtonLabels).open() == 0;
	}

	/**
	 * opens a dialog of the form {@link MessageDialog#QUESTION_WITH_CANCEL}
	 * 
	 * @param parentShell
	 * @param question
	 *            message to be shown in the dialog
	 * @param dialogButtons
	 *            a list with button labels, which should be displayed in the
	 *            dialog. The last element in the list will be the default
	 *            button.
	 * @return the index of the clicked button according to the dialogButtons
	 *         parameter
	 */
	public static int openQuestionWithCancelDialog(Shell parentShell, String question, String[] dialogButtons) {
		return openQuestionWithCancelDialog(parentShell, question, dialogButtons, dialogTitle);
	}

	public static int openQuestionWithCancelDialog(Shell parentShell, String question, String[] dialogButtons,
			String title) {
		return new SimonykeesMessageDialog(parentShell, title, dialogTitleImage, question,
				MessageDialog.QUESTION_WITH_CANCEL, dialogButtons.length, dialogButtons).open();
	}

	@Override
	protected Control createMessageArea(Composite composite) {
		Image image = getImage();
		if (image != null) {
			imageLabel = new Label(composite, SWT.NULL);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
			GridDataFactory.fillDefaults()
				.align(SWT.CENTER, SWT.BEGINNING)
				.applyTo(imageLabel);
		}
		if (message != null) {
			Link link = new Link(composite, getMessageLabelStyle());
			link.setText(messageText);
			link.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					try {
						PlatformUI.getWorkbench()
							.getBrowserSupport()
							.getExternalBrowser()
							.openURL(new URL(arg0.text));
					} catch (PartInitException | MalformedURLException e) {
						logger.error(Messages.SimonykeesMessageDialog_open_browser_error_message, e);
					}
				}
			});
			GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.CENTER)
				.grab(true, false)
				.hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
				.applyTo(link);
		}
		return composite;
	}

}
