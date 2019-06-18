package eu.jsparrow.ui.dialog;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.osgi.util.NLS;
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
	private static final String DIALOG_TITLE = Messages.aa_codename;
	private static final Image DIALOG_TITLE_IMAGE = null;
	private static final String DIALOG_INFORMATION_MESSAGE = Messages.HelpMessageDialog_default_message;
	private static final String DIALOG_ERROR_MESSAGE = Messages.SimonykeesMessageDialog_default_error_message;
	private static final int DEFAULT_INDEX = 1;
	private static final String[] DIALOG_BUTTON_LABELS = { Messages.ui_ok };
	private static final String SPLENDIT_URL = Messages.HelpMessageDialog_homepage_url;
	private static final String DOCUMENTATION_URL = Messages.HelpMessageDialog_documentation_url;
	private static final String SUPPORT_MAIL = Messages.HelpMessageDialog_support_mail;
	private static final String SUPPORT_URL = Messages.HelpMessageDialog_support_url;

	private String messageText;

	private SimonykeesMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
			int dialogImageType, int defaultIndex, String... dialogButtonLabels) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
				defaultIndex);
		messageText = dialogMessage;
	}

	public static boolean openDefaultHelpMessageDialog(Shell parentShell) {
		String messageText = NLS.bind(DIALOG_INFORMATION_MESSAGE,
				new String[] { DOCUMENTATION_URL, SPLENDIT_URL, SUPPORT_MAIL, SUPPORT_URL });

		return new SimonykeesMessageDialog(parentShell, DIALOG_TITLE, DIALOG_TITLE_IMAGE, messageText,
				MessageDialog.INFORMATION, DEFAULT_INDEX, DIALOG_BUTTON_LABELS).open() == 0;
	}

	public static boolean openMessageDialog(Shell parentShell, String message, int dialogImage) {
		return openMessageDialog(parentShell, message, dialogImage, DIALOG_TITLE);
	}

	public static boolean openMessageDialog(Shell parentShell, String message, int dialogImage, String title) {
		String messageText = message;
		return new SimonykeesMessageDialog(parentShell, title, DIALOG_TITLE_IMAGE, messageText, dialogImage, DEFAULT_INDEX,
				DIALOG_BUTTON_LABELS).open() == 0;
	}

	public static boolean openConfirmDialog(Shell parentShell, String message) {
		return openConfirmDialog(parentShell, message, DIALOG_TITLE);
	}

	public static boolean openConfirmDialog(Shell parentShell, String message, String title) {
		String messageText = message;
		return new SimonykeesMessageDialog(parentShell, title, DIALOG_TITLE_IMAGE, messageText, MessageDialog.CONFIRM,
				DEFAULT_INDEX, new String[] { Messages.ui_cancel, Messages.ui_ok }).open() == 1;
	}

	public static boolean openErrorMessageDialog(Shell parentShell, SimonykeesException simonykeesException) {
		return openErrorMessageDialog(parentShell, simonykeesException, DIALOG_TITLE);
	}

	public static boolean openErrorMessageDialog(Shell parentShell, SimonykeesException simonykeesException,
			String title) {
		String messageText = ((simonykeesException != null) ? simonykeesException.getUiMessage() : DIALOG_ERROR_MESSAGE)
				+ System.lineSeparator() + MAIL_BUGREPORT;
		return new SimonykeesMessageDialog(parentShell, title, DIALOG_TITLE_IMAGE, messageText, MessageDialog.ERROR,
				DEFAULT_INDEX, DIALOG_BUTTON_LABELS).open() == 0;
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
		return openQuestionWithCancelDialog(parentShell, question, dialogButtons, DIALOG_TITLE);
	}

	public static int openQuestionWithCancelDialog(Shell parentShell, String question, String[] dialogButtons,
			String title) {
		return new SimonykeesMessageDialog(parentShell, title, DIALOG_TITLE_IMAGE, question,
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
				public void widgetSelected(SelectionEvent event) {
					try {
						String urlString = event.text;
						URL url = new URL(urlString);
						PlatformUI.getWorkbench()
							.getBrowserSupport()
							.getExternalBrowser()
							.openURL(url);
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
