package eu.jsparrow.ui.preference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.JSparrowPricingLink;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtil.LicenseUpdateResult;

/**
 * Dialog for updating license key.
 * 
 * @author Ardit Ymeri, Andreja Sambolec, Matthias Webhofer
 * @since 1.0
 *
 */
public class SimonykeesUpdateLicenseDialog extends TrayDialog {

	private static final String LOGO_ACTIVE_LICENSE_PATH = SimonykeesPreferencePageLicense.LOGO_ACTIVE_LICENSE_PATH;
	private static final String TICKMARK_GREEN_ICON_PATH = "icons/if_Tick_Mark_20px.png"; //$NON-NLS-1$
	private static final String CLOSE_RED_ICON_PATH = "icons/if_Close_Icon_20px.png"; //$NON-NLS-1$
	private Text licenseKeyText;
	private String licenseKey = ""; //$NON-NLS-1$
	private CLabel updatedLabel;
	private Label updatedIconLabel;
	private Image scaledJSparrowImageActive;
	private Image scaledTickmarkGreenIconImage;
	private Image scaledCloseRedIconImage;
	private final List<Runnable> afterLicenseUpdateListeners = new ArrayList<>();

	public SimonykeesUpdateLicenseDialog(Shell parentShell, List<Runnable> afterLicenseUpdateListeners) {
		this(parentShell);
		this.afterLicenseUpdateListeners.addAll(afterLicenseUpdateListeners);
	}

	public SimonykeesUpdateLicenseDialog(Shell parentShell) {
		super(parentShell);
		ContextInjectionFactory.inject(this, Activator.getEclipseContext());

	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Obtain License"); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		/*
		 * Setting help listener to question mark help button Open default help
		 * dialog
		 */
		area.addHelpListener((HelpEvent e) -> SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell()));

		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.WRAP, true, true));

		GridLayout licenceMainLayout = new GridLayout(1, false);
		container.setLayout(licenceMainLayout);

		updatedIconLabel = new Label(container, SWT.NONE);

		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		IPath iPathActive = new Path(LOGO_ACTIVE_LICENSE_PATH);
		URL urlActive = FileLocator.find(bundle, iPathActive, new HashMap<>());
		ImageDescriptor imageDescActive = ImageDescriptor.createFromURL(urlActive);
		Image jSparrowImageActive = imageDescActive.createImage();
		ImageData imageDataActive = jSparrowImageActive.getImageData();
		scaledJSparrowImageActive = new Image(container.getDisplay(), imageDataActive);
		scaledJSparrowImageActive = createScaledJSparrowImage(container, bundle);

		updatedIconLabel.setImage(scaledJSparrowImageActive);
		updatedIconLabel.setVisible(true);
		addEmptyLineLabel(container);
		addLinkToJSparrowPricingPage(container, JSparrowPricingLink.VISIT_US_TO_OBTAIN_A_LICENSE);
		addEmptyLineLabel(container);

		// enter new key group
		Group newKeyGroup = new Group(container, SWT.NONE);
		newKeyGroup.setText(Messages.SimonykeesUpdateLicenseDialog_update_license_dialog_group_title);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 1;
		newKeyGroup.setLayoutData(gridData);

		GridLayout groupGridLayout = new GridLayout(3, false);
		newKeyGroup.setLayout(groupGridLayout);

		GridData groupGridData = new GridData(SWT.CENTER, SWT.FILL, false, false);
		groupGridData.horizontalIndent = 10;
		groupGridData.verticalIndent = 5;

		Label licenseKeyLabel = new Label(newKeyGroup, SWT.NONE);
		licenseKeyLabel.setText(Messages.SimonykeesUpdateLicenseDialog_update_license_dialog_label);
		licenseKeyLabel.setLayoutData(groupGridData);

		groupGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		groupGridData.horizontalIndent = 20;
		licenseKeyText = new Text(newKeyGroup, SWT.BORDER);
		licenseKeyText.setLayoutData(groupGridData);
		licenseKeyText.addModifyListener((ModifyEvent event) -> {
			Text textWidget = (Text) event.getSource();
			licenseKey = textWidget.getText();
		});

		groupGridData = new GridData(SWT.CENTER, SWT.FILL, false, false);
		groupGridData.horizontalIndent = 20;

		Button updateButton = new Button(newKeyGroup, SWT.PUSH);
		updateButton.setLayoutData(groupGridData);
		updateButton.setText(Messages.SimonykeesUpdateLicenseDialog_update_llicense_dialog_button);
		updateButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				BusyIndicator.showWhile(Display.getDefault(), () -> {

					LicenseUtil licenseUtil = LicenseUtil.get();
					LicenseUpdateResult result = licenseUtil.update(licenseKey);
					if (!result.wasSuccessful()) {
						updatedLabel.setImage(scaledCloseRedIconImage);

					} else {
						updatedLabel.setImage(scaledTickmarkGreenIconImage);
						licenseUtil.updateValidationResult();
						afterLicenseUpdateListeners.forEach(Runnable::run);
					}
					updatedLabel.setText(result.getDetailMessage());
					updatedLabel.setVisible(true);
					updatedIconLabel.setVisible(true);

					updatedIconLabel.getParent()
						.layout();
				});
			}
		});

		updatedLabel = new CLabel(container, SWT.NONE);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		gridData.verticalIndent = 5;
		gridData.horizontalIndent = 10;
		updatedLabel.setLayoutData(gridData);

		IPath iPathTickMarkGreen = new Path(TICKMARK_GREEN_ICON_PATH);
		URL urlTickMarkGreen = FileLocator.find(bundle, iPathTickMarkGreen, new HashMap<>());
		ImageDescriptor imageDescTickMarkGreen = ImageDescriptor.createFromURL(urlTickMarkGreen);
		Image tickmarkGreenIconImage = imageDescTickMarkGreen.createImage();
		ImageData imageDataTickmarkGreen = tickmarkGreenIconImage.getImageData();
		scaledTickmarkGreenIconImage = new Image(container.getDisplay(), imageDataTickmarkGreen);

		IPath iPathCloseRed = new Path(CLOSE_RED_ICON_PATH);
		URL urlCloseRed = FileLocator.find(bundle, iPathCloseRed, new HashMap<>());
		ImageDescriptor imageDescCloseRed = ImageDescriptor.createFromURL(urlCloseRed);
		Image closeRedIconImage = imageDescCloseRed.createImage();
		ImageData imageDataCloseRed = closeRedIconImage.getImageData();
		scaledCloseRedIconImage = new Image(container.getDisplay(), imageDataCloseRed);

		updatedLabel.setImage(scaledTickmarkGreenIconImage);
		updatedLabel.setText(Messages.SimonykeesUpdateLicenseDialog_license_updated_successfully);
		updatedLabel.setVisible(false);

		/*
		 * Automatic release does not work for Image, so we do it manually when
		 * container is disposed
		 */
		container.addDisposeListener((DisposeEvent e) -> {
			tickmarkGreenIconImage.dispose();
			closeRedIconImage.dispose();
			// jSparrowImageInactive.dispose();
			jSparrowImageActive.dispose();
			scaledJSparrowImageActive.dispose();
			// scaledJSparrowImageInactive.dispose();
			scaledTickmarkGreenIconImage.dispose();
			scaledCloseRedIconImage.dispose();
		});

		return container;
	}

	protected void addEmptyLineLabel(Composite container) {
		Label emptyLine = new Label(container, SWT.NONE);
		emptyLine.setText(""); //$NON-NLS-1$
	}

	private Image createScaledJSparrowImage(Composite container, Bundle bundle) {
		IPath iPathActive = new Path(LOGO_ACTIVE_LICENSE_PATH);
		URL urlActive = FileLocator.find(bundle, iPathActive, new HashMap<>());
		ImageDescriptor imageDescActive = ImageDescriptor.createFromURL(urlActive);
		Image jSparrowImageActive = imageDescActive.createImage();
		ImageData imageDataActive = jSparrowImageActive.getImageData();
		return new Image(container.getDisplay(), imageDataActive);

	}

	private void addLinkToJSparrowPricingPage(Composite parent, JSparrowPricingLink jSparrowPricingLink) {
		Link linkToUnlockRules = new Link(parent, SWT.NONE);
		linkToUnlockRules
			.setText(jSparrowPricingLink.getText());
		linkToUnlockRules.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					PlatformUI.getWorkbench()
						.getBrowserSupport()
						.getExternalBrowser()
						.openURL(new URL(arg0.text));
				} catch (PartInitException | MalformedURLException e) {
					// nothing...
				}
			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(550, 450);
	}

	@Override
	protected void okPressed() {
		this.licenseKey = licenseKeyText.getText();
		super.okPressed();
	}

	public String getLicenseKey() {
		return licenseKey;
	}

}
