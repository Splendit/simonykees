package at.splendit.simonykees.core.ui.dialog;

import java.util.function.Function;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import at.splendit.simonykees.core.exception.SimonykeesException;
import at.splendit.simonykees.core.i18n.Messages;

/**
 * Simple help dialog that gets populated with default values
 * 
 * @author Martin Huter
 * @since 0.9
 */
public class SimonykeesMessageDialog extends MessageDialog {

	private static final String MAIL_BUGREPORT = Messages.SimonykeesMessageDialog_bugreport_email;
	private static final String dialogTitle = Messages.aa_codename;
	private static final Image dialogTitleImage = null;
	private static final String dialogInformationMessage = Messages.HelpMessageDialog_default_message;
	private static final String dialogErrorMessage = Messages.SimonykeesMessageDialog_default_error_message;
	private static final int defaultIndex = 1;
	private static final String[] dialogButtonLabels = { Messages.ui_ok };
	private static final String splenditUrl = Messages.HelpMessageDialog_homepage_url;
	private Function<Composite, Control> customAreaFunction = parent -> {
		Link link = new Link(parent, SWT.NONE | SWT.RIGHT);
		link.setText(splenditUrl);
		return link;
	};

	public static boolean openDefaultHelpMessageDialog(Shell parentShell) {
		return new SimonykeesMessageDialog(parentShell, dialogTitle, dialogTitleImage, dialogInformationMessage,
				MessageDialog.INFORMATION, defaultIndex, dialogButtonLabels).open() == 0;
	}

	public static boolean openErrorMessageDialog(Shell parentShell, SimonykeesException simonykeesException) {
		Function<Composite, Control> customAreaFunction = parent -> {
			Link link = new Link(parent, SWT.NONE | SWT.RIGHT);
			link.setText(MAIL_BUGREPORT);
			return link;
		};
		return new SimonykeesMessageDialog(customAreaFunction, parentShell, dialogTitle, dialogTitleImage,
				(simonykeesException != null) ? simonykeesException.getUiMessage() : dialogErrorMessage,
				MessageDialog.ERROR, defaultIndex, dialogButtonLabels).open() == 0;
	}

	private SimonykeesMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
			int dialogImageType, int defaultIndex, String... dialogButtonLabels) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, defaultIndex,
				dialogButtonLabels);
	}

	private SimonykeesMessageDialog(Function<Composite, Control> customAreaFunction, Shell parentShell,
			String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, int defaultIndex,
			String... dialogButtonLabels) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, defaultIndex,
				dialogButtonLabels);
		// This function injection works because this.open() is used to create
		// the dialog, where createCustomArea is invoked
		this.customAreaFunction = customAreaFunction;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		return customAreaFunction.apply(parent);
	}

}
