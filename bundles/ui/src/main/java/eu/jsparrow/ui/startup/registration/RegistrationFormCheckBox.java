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

import eu.jsparrow.ui.Activator;

public class RegistrationFormCheckBox {

	private Button checkBox;

//	private ControlDecoration decoValid;
	private ControlDecoration decoInvalid;

	private boolean selected = false;

//	private static final String TICKMARK_GREEN_ICON_PATH = "icons/if_Tick_Mark_12px.png"; //$NON-NLS-1$
	private static final String CLOSE_RED_ICON_PATH = "icons/if_Close_Icon_12px.png"; //$NON-NLS-1$

//	private Image scaledTickmarkGreenIconImage;
	private Image scaledCloseRedIconImage;

	public RegistrationFormCheckBox(Group parent, String text) {
		GridData checkBoxTextGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		checkBoxTextGridData.widthHint = 405;
		checkBox = new Button(parent, SWT.CHECK | SWT.WRAP);
		checkBox.setText(text);
		checkBox.setLayoutData(checkBoxTextGridData);
		addCheckBoxChangeListener();

//		decoValid = new ControlDecoration(checkBox, SWT.TOP | SWT.RIGHT);
		decoInvalid = new ControlDecoration(checkBox, SWT.TOP | SWT.RIGHT);

		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

//		IPath iPathTickMarkGreen = new Path(TICKMARK_GREEN_ICON_PATH);
//		URL urlTickMarkGreen = FileLocator.find(bundle, iPathTickMarkGreen, new HashMap<>());
//		ImageDescriptor imageDescTickMarkGreen = ImageDescriptor.createFromURL(urlTickMarkGreen);
//		Image tickmarkGreenIconImage = imageDescTickMarkGreen.createImage();
//		ImageData imageDataTickmarkGreen = tickmarkGreenIconImage.getImageData();
//		scaledTickmarkGreenIconImage = new Image(parent.getDisplay(), imageDataTickmarkGreen);

		IPath iPathCloseRed = new Path(CLOSE_RED_ICON_PATH);
		URL urlCloseRed = FileLocator.find(bundle, iPathCloseRed, new HashMap<>());
		ImageDescriptor imageDescCloseRed = ImageDescriptor.createFromURL(urlCloseRed);
		Image closeRedIconImage = imageDescCloseRed.createImage();
		ImageData imageDataCloseRed = closeRedIconImage.getImageData();
		scaledCloseRedIconImage = new Image(parent.getDisplay(), imageDataCloseRed);

//		decoValid.setDescriptionText("Valid name");
//		decoValid.setImage(scaledTickmarkGreenIconImage);
//		decoValid.hide();

		decoInvalid.setDescriptionText("Please enter valid name");
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
//			decoValid.show();
		} else if(showDecoInvalid) {
//			decoValid.hide();
			decoInvalid.show();
		}
	}
}
