package eu.jsparrow.ui.startup.registration;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;

/**
 * A wrapper for a text field in the registration form. Provides
 * {@link ControlDecoration}s for the validity of the entered value.
 * 
 * @since 3.0.0
 *
 */
public class RegistrationFormField {

	private Text text;

	private ControlDecoration decoValid;
	private ControlDecoration decoInvalid;

	private boolean valid;

	private static final String TICKMARK_GREEN_ICON_PATH = "icons/if_Tick_Mark_12px.png"; //$NON-NLS-1$
	private static final String CLOSE_RED_ICON_PATH = "icons/if_Close_Icon_12px.png"; //$NON-NLS-1$

	private Image scaledTickmarkGreenIconImage;
	private Image scaledCloseRedIconImage;

	public RegistrationFormField(Group parent, String labelText) {
		GridData labelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		labelGridData.verticalIndent = 5;
		Label firstNameLabel = new Label(parent, SWT.NONE);
		firstNameLabel.setLayoutData(labelGridData);
		firstNameLabel.setText(labelText);

		GridData dataTextField = new GridData(SWT.FILL, SWT.CENTER, false, false);
		dataTextField.verticalIndent = 5;
		dataTextField.horizontalIndent = 20;
		dataTextField.widthHint = 180;
		text = new Text(parent, SWT.BORDER);
		text.setLayoutData(dataTextField);

		decoValid = new ControlDecoration(text, SWT.CENTER | SWT.RIGHT);
		decoInvalid = new ControlDecoration(text, SWT.CENTER | SWT.RIGHT);

		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		IPath iPathTickMarkGreen = new Path(TICKMARK_GREEN_ICON_PATH);
		URL urlTickMarkGreen = FileLocator.find(bundle, iPathTickMarkGreen, new HashMap<>());
		ImageDescriptor imageDescTickMarkGreen = ImageDescriptor.createFromURL(urlTickMarkGreen);
		Image tickmarkGreenIconImage = imageDescTickMarkGreen.createImage();
		ImageData imageDataTickmarkGreen = tickmarkGreenIconImage.getImageData();
		scaledTickmarkGreenIconImage = new Image(parent.getDisplay(), imageDataTickmarkGreen);

		IPath iPathCloseRed = new Path(CLOSE_RED_ICON_PATH);
		URL urlCloseRed = FileLocator.find(bundle, iPathCloseRed, new HashMap<>());
		ImageDescriptor imageDescCloseRed = ImageDescriptor.createFromURL(urlCloseRed);
		Image closeRedIconImage = imageDescCloseRed.createImage();
		ImageData imageDataCloseRed = closeRedIconImage.getImageData();
		scaledCloseRedIconImage = new Image(parent.getDisplay(), imageDataCloseRed);
		parent.addDisposeListener(e -> scaledCloseRedIconImage.dispose());
		parent.addDisposeListener(e -> closeRedIconImage.dispose());
		parent.addDisposeListener(e -> tickmarkGreenIconImage.dispose());
		parent.addDisposeListener(e -> scaledTickmarkGreenIconImage.dispose());
		

		// set description and image
		decoValid.setDescriptionText(Messages.RegistrationFormField_validDataText);
		decoValid.setImage(scaledTickmarkGreenIconImage);
		decoValid.hide();

		// set description and image
		decoInvalid.setDescriptionText(Messages.RegistrationFormField_invalidDataText);
		decoInvalid.setImage(scaledCloseRedIconImage);
		decoInvalid.hide();

		text.addModifyListener((ModifyEvent event) -> {
			validate((Text) event.getSource());
			updateDecoVisibility();
		});

		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				validate((Text) event.getSource());
				updateDecoVisibility();
			}
		});
	}

	protected void updateDecoVisibility() {
		if (valid) {
			decoInvalid.hide();
			decoValid.show();
		} else {
			decoValid.hide();
			decoInvalid.show();
		}
	}

	protected void validate(Text text) {
		setValid(text.getText()
			.length() >= 1);
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean isValid) {
		this.valid = isValid;
	}

	public Text getText() {
		return text;
	}
	
	public String getValue() {
		return text.getText();
	}
}
