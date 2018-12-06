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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.osgi.framework.Bundle;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;

public class RegistrationFormCheckBox {

	private Button checkBox;

	private ControlDecoration decoInvalid;

	private boolean selected = false;

	private static final String CLOSE_RED_ICON_PATH = "icons/if_Close_Icon_12px.png"; //$NON-NLS-1$

	private Image scaledCloseRedIconImage;

	public RegistrationFormCheckBox(Group parent, String text) {
		GridData checkBoxTextGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		checkBoxTextGridData.widthHint = 402;
		checkBox = new Button(parent, SWT.CHECK | SWT.WRAP);
		checkBox.setText(text);
		checkBox.setLayoutData(checkBoxTextGridData);
		addCheckBoxChangeListener();

		decoInvalid = new ControlDecoration(checkBox, SWT.TOP | SWT.RIGHT);

		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		IPath iPathCloseRed = new Path(CLOSE_RED_ICON_PATH);
		URL urlCloseRed = FileLocator.find(bundle, iPathCloseRed, new HashMap<>());
		ImageDescriptor imageDescCloseRed = ImageDescriptor.createFromURL(urlCloseRed);
		Image closeRedIconImage = imageDescCloseRed.createImage();
		ImageData imageDataCloseRed = closeRedIconImage.getImageData();
		scaledCloseRedIconImage = new Image(parent.getDisplay(), imageDataCloseRed);

		decoInvalid.setDescriptionText(Messages.RegistrationFormCheckBox_invalidDataText);
		decoInvalid.setImage(scaledCloseRedIconImage);
		decoInvalid.hide();
	}

	public Button getCheckBox() {
		return checkBox;
	}

	public void setLayoutData(GridData data) {
		checkBox.setLayoutData(data);
	}

	public void setSelection(boolean isSelected) {
		checkBox.setSelection(isSelected);
	}

	public boolean getSelection() {
		return checkBox.getSelection();
	}

	protected void addCheckBoxChangeListener() {
		checkBox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selected = ((Button) e.getSource()).getSelection();
				updateDecoVisibility(false);
			}
		});
	}

	public void updateDecoVisibility(boolean showDecoInvalid) {
		if (selected) {
			decoInvalid.hide();
		} else if (showDecoInvalid) {
			decoInvalid.show();
		}
	}

	public void resetDecoVisibility() {
		decoInvalid.hide();
	}
}
