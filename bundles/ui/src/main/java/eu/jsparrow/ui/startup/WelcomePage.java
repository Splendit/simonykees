package eu.jsparrow.ui.startup;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.dialog.JSparrowPricingLink;
import eu.jsparrow.ui.startup.registration.RegistrationDialog;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * Content displayed in Eclipse editor when the jSparrow plugin is installed.
 * 
 * @since 2.7.0
 *
 */
public class WelcomePage extends FormPage {

	public static final String PAGE_ID = "eu.jsparrow.ui.startup.page.overview"; //$NON-NLS-1$

	public WelcomePage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		Composite content = managedForm.getForm()
			.getBody();
		content.setBackground(Display.getDefault()
			.getSystemColor(SWT.COLOR_WHITE));
		content.setLayout(new GridLayout(1, true));
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite title = toolkit.createComposite(content);
		title.setLayout(new GridLayout(1, false));
		title.setBackground(Display.getDefault()
			.getSystemColor(SWT.COLOR_WHITE));
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, false, false);
		gridData.horizontalIndent = 6;
		title.setLayoutData(gridData);

		createTitleBar(title);

		Composite body = toolkit.createComposite(content);
		body.setBackground(Display.getDefault()
			.getSystemColor(SWT.COLOR_WHITE));
		body.setLayout(new GridLayout(5, true));
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// create left side
		Composite leftComposite = toolkit.createComposite(body);
		leftComposite.setBackground(Display.getDefault()
			.getSystemColor(SWT.COLOR_WHITE));
		leftComposite.setLayout(new GridLayout(1, false));
		leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		// create right side
		Composite rightComposite = toolkit.createComposite(body);
		rightComposite.setBackground(Display.getDefault()
			.getSystemColor(SWT.COLOR_WHITE));
		rightComposite.setLayout(new GridLayout(1, false));
		rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		// fill left side
		createScrolledFormContent(leftComposite);

		// fill right side
		createButtonsSide(rightComposite);

	}

	private void createTitleBar(Composite title) {
		Label titleLabel = new Label(title, SWT.TITLE);
		Font font = title.getDisplay()
			.getSystemFont();
		FontData fontData = font.getFontData()[0];

		FontDescriptor titleFontDescription = FontDescriptor.createFrom(font)
			.setHeight(fontData.getHeight() * 2);
		Font titleFont = titleFontDescription.createFont(title.getDisplay());
		title.addDisposeListener(e -> titleFont.dispose());

		titleLabel.setFont(titleFont);
		titleLabel.setText(Messages.WelcomePage_title);
		titleLabel.setBackground(Display.getDefault()
			.getSystemColor(SWT.COLOR_WHITE));
	}

	protected void createScrolledFormContent(Composite parent) {
		Group browserGroup = new Group(parent, SWT.NONE);
		browserGroup.setBackground(Display.getDefault()
			.getSystemColor(SWT.COLOR_WHITE));
		GridData groupGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		browserGroup.setLayoutData(groupGridData);
		browserGroup.setLayout(new GridLayout(1, false));

		Browser browser = new Browser(browserGroup, SWT.SIMPLE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		browser.setUrl("https://jsparrow.github.io/dashboard/"); //$NON-NLS-1$
	}

	private void createButtonsSide(Composite rightComposite) {
		Group gettingStartedGroup = new Group(rightComposite, SWT.NONE);
		gettingStartedGroup.setText(Messages.WelcomePage_getting_started_group);
		GridData groupGridData = new GridData(GridData.FILL_HORIZONTAL);
		groupGridData.heightHint = 142;
		groupGridData.horizontalIndent = 5;
		groupGridData.verticalIndent = 15;
		gettingStartedGroup.setLayoutData(groupGridData);
		gettingStartedGroup.setLayout(new GridLayout(1, false));

		GridData buttonGridData = new GridData(SWT.CENTER, SWT.TOP, true, false);
		buttonGridData.widthHint = 200;
		buttonGridData.verticalIndent = 5;

		Button guidelinesButton = new Button(gettingStartedGroup, SWT.PUSH);
		guidelinesButton.setLayoutData(buttonGridData);
		guidelinesButton.setText(Messages.WelcomePage_guidelines_button);
		createButtonListenerToOpenWebpage(guidelinesButton, "https://jsparrow.github.io/eclipse/getting-started.html"); //$NON-NLS-1$

		Button licenseButton = new Button(gettingStartedGroup, SWT.PUSH);
		licenseButton.setLayoutData(buttonGridData);
		licenseButton.setText(Messages.WelcomePage_buy_license_button);
		createButtonListenerToOpenWebpage(licenseButton, JSparrowPricingLink.getJSparrowPricingPageAddress());

		Button marketplaceButton = new Button(gettingStartedGroup, SWT.PUSH);
		marketplaceButton.setLayoutData(buttonGridData);
		marketplaceButton.setText(Messages.WelcomePage_market_place_button);
		createButtonListenerToOpenWebpage(marketplaceButton,
				"https://marketplace.eclipse.org/content/jsparrow-automatical-java-code-improvement"); //$NON-NLS-1$

		Group customizationGroup = new Group(rightComposite, SWT.NONE);
		customizationGroup.setText(Messages.WelcomePage_customization_group);
		groupGridData = new GridData(GridData.FILL_HORIZONTAL);
		groupGridData.heightHint = 142;
		groupGridData.horizontalIndent = 5;
		groupGridData.verticalIndent = 65;
		customizationGroup.setLayoutData(groupGridData);
		customizationGroup.setLayout(new GridLayout(1, false));

		Button generalPreferencesButton = new Button(customizationGroup, SWT.PUSH);
		generalPreferencesButton.setLayoutData(buttonGridData);
		generalPreferencesButton.setText(Messages.WelcomePage_preferences_button);
		createButtonListenerToOpenPreferences(generalPreferencesButton,
				"eu.jsparrow.ui.preference.ProfilePreferencePage"); //$NON-NLS-1$

		Button licensePreferencesButton = new Button(customizationGroup, SWT.PUSH);
		licensePreferencesButton.setLayoutData(buttonGridData);
		licensePreferencesButton.setText(Messages.WelcomePage_license_preferences_button);
		createButtonListenerToOpenPreferences(licensePreferencesButton,
				"eu.jsparrow.ui.preference.ProfilePreferencePageLicense"); //$NON-NLS-1$

		Button markerPreferencesButton = new Button(customizationGroup, SWT.PUSH);
		markerPreferencesButton.setLayoutData(buttonGridData);
		markerPreferencesButton.setText("Open marker Preferences"); //$NON-NLS-1$
		createButtonListenerToOpenPreferences(markerPreferencesButton,
				"eu.jsparrow.ui.preference.MarkersPreferencePage"); //$NON-NLS-1$
		markerPreferencesButton.setVisible(true);
	}

	private void createButtonListenerToOpenPreferences(Button openPreferencesButton, String activePreferencePageId) {
		openPreferencesButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(Display.getDefault()
					.getActiveShell(), activePreferencePageId, null, null);
				dialog.open();
			}
		});
	}

	private void createButtonListenerToOpenWebpage(Button webpageOpenButton, String urlString) {
		webpageOpenButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				Program.launch(urlString);
			}
		});
	}
}
