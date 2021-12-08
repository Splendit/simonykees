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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.osgi.framework.Bundle;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;

/**
 * A wrapper for a check box in the registration form. Provides
 * {@link ControlDecoration} for cases where the check is required.
 * 
 * @since 3.0.0
 *
 */
public class RegistrationFormCheckBox {

	private Button checkBox;
	private Link checkBoxText;

	private ControlDecoration decoInvalid;

	private static final String CLOSE_RED_ICON_PATH = "icons/if_Close_Icon_12px.png"; //$NON-NLS-1$

	private Image scaledCloseRedIconImage;

	public RegistrationFormCheckBox(Group parent, String text) {
		Composite checkBoxContainer = new Composite(parent, SWT.NONE);
		GridLayout checkBoxLayout = new GridLayout(2, false);
		checkBoxContainer.setLayout(checkBoxLayout);
		GridData checkBoxGridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
		checkBoxGridData.widthHint = 408;
		checkBoxContainer.setLayoutData(checkBoxGridData);

		checkBox = new Button(checkBoxContainer, SWT.CHECK | SWT.WRAP);
		GridData checkBoxButtonGridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
		checkBoxButtonGridData.verticalIndent = 2;
		checkBox.setLayoutData(checkBoxButtonGridData);
		addCheckBoxChangeListener();

		checkBoxText = new Link(checkBoxContainer, SWT.WRAP);
		checkBoxText.setText(text);
		GridData checkBoxTextGridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
		checkBoxTextGridData.widthHint = 372;
		checkBoxText.setLayoutData(checkBoxTextGridData);
		addLinkSelectionListener(checkBoxText);

		decoInvalid = new ControlDecoration(checkBoxText, SWT.TOP | SWT.RIGHT);

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
		parent.addDisposeListener(e -> scaledCloseRedIconImage.dispose());
		parent.addDisposeListener(e -> closeRedIconImage.dispose());
		
	}

	public Button getCheckBox() {
		return checkBox;
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
				updateDecoVisibility(false);
			}
		});
	}

	public void updateDecoVisibility(boolean showDecoInvalid) {
		if (getSelection()) {
			decoInvalid.hide();
		} else if (showDecoInvalid) {
			decoInvalid.show();
		}
	}

	public void resetDecoVisibility() {
		decoInvalid.hide();
	}

	/**
	 * Helper method to add selection listener on links to open link in external
	 * browser.
	 * 
	 * @param link
	 *            that should be opened
	 */
	private void addLinkSelectionListener(Link link) {
		link.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Program.launch(arg0.text);
			}
		});

	}
}
