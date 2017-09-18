package eu.jsparrow.ui.startup;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;

/**
 * StartDialog is welcome screen with quick start information, shown immediately
 * when Eclipse is started every time as long as it is not disabled
 * 
 * @author andreja.sambolec
 * 
 * @since 2.0.2
 *
 */
public class StartDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(StartDialog.class);

	private static final String LOGO_PATH_ACTIVE = "icons/jSparrow_active_icon_100.png"; //$NON-NLS-1$
	private static final String IMG_PATH_SCREENSHOT = "icons/context_menu.png"; //$NON-NLS-1$
	private static final String IMG_PATH_PROJECT_EXPLORER = "icons/project_explorer.png"; //$NON-NLS-1$
	private static final String IMG_PATH_PACKAGE_EXPLORER = "icons/package_explorer.png"; //$NON-NLS-1$
	private static final String IMG_PATH_CLASS_EDITOR = "icons/class_editor.png"; //$NON-NLS-1$

	private static final String IMG_SAD_ICON = "icons/sad.png"; //$NON-NLS-1$
	private static final String IMG_EVEN_ICON = "icons/even.png"; //$NON-NLS-1$
	private static final String IMG_HAPPY_ICON = "icons/happy.png"; //$NON-NLS-1$
	private static final String IMG_INLOVE_ICON = "icons/extrahappy.png"; //$NON-NLS-1$

	private Composite leftComposite;
	private Composite rightComposite;

	private Bundle bundle;

	private Image jSparrowImageActive;

	private Image jSparrowImageScreenshot;
	private Label screenshotLabel;

	private Font titleFont;
	private Font paragraphTitleFont;
	private Font paragraphTextFont;
	private Font instructionsFont;

	private String feedbackText = ""; //$NON-NLS-1$
	private String ratingText = ""; //$NON-NLS-1$

	public StartDialog(Shell parent) {
		super(parent);

		bundle = Platform.getBundle(Activator.PLUGIN_ID);
		IPath iPathActive = new Path(LOGO_PATH_ACTIVE);
		URL urlActive = FileLocator.find(bundle, iPathActive, new HashMap<>());
		ImageDescriptor imageDescActive = ImageDescriptor.createFromURL(urlActive);
		jSparrowImageActive = imageDescActive.createImage();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		Composite titleContainer = new Composite(area, SWT.NONE);
		GridLayout titleLayout = new GridLayout(2, false);
		titleContainer.setLayout(titleLayout);
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		titleContainer.setLayoutData(gridData);

		Dialog.applyDialogFont(parent);
		Font font = parent.getDisplay().getSystemFont();
		FontData fontData = font.getFontData()[0];

		FontDescriptor titleFontDescription = FontDescriptor.createFrom(font).setHeight(fontData.getHeight() * 2);
		titleFont = titleFontDescription.createFont(parent.getDisplay());
		FontDescriptor paragraphTitleFontDescription = FontDescriptor.createFrom(font)
				.setHeight(fontData.getHeight() * 3 / 2);
		paragraphTitleFont = paragraphTitleFontDescription.createFont(parent.getDisplay());
		FontDescriptor paragraphTextFontDescription = FontDescriptor.createFrom(font)
				.setHeight(fontData.getHeight() * 6 / 5);
		paragraphTextFont = paragraphTextFontDescription.createFont(parent.getDisplay());
		FontDescriptor instructionsFontDescription = FontDescriptor.createFrom(font).setStyle(SWT.BOLD);
		instructionsFont = instructionsFontDescription.createFont(parent.getDisplay());

		Label logoLabel = new Label(titleContainer, SWT.NONE);
		logoLabel.setImage(jSparrowImageActive);
		gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, true);
		logoLabel.setLayoutData(gridData);

		Label titleLabel = new Label(titleContainer, SWT.NONE);
		titleLabel.setText(Messages.StartDialog_titleLabel);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		titleLabel.setLayoutData(gridData);
		titleLabel.setFont(titleFont);

		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		leftComposite = new Composite(container, SWT.NONE);
		rightComposite = new Composite(container, SWT.NONE);

		createLeftComposite(leftComposite);
		createRightComposite(rightComposite);

		return container;
	}

	/**
	 * Create left side of welcome screen with welcome and quick start steps.
	 * 
	 * @param parent
	 */
	private void createLeftComposite(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gridData.widthHint = 470;
		parent.setLayoutData(gridData);

		IPath iPathScreenshot = new Path(IMG_PATH_SCREENSHOT);
		URL urlScreenshot = FileLocator.find(bundle, iPathScreenshot, new HashMap<>());
		ImageDescriptor imageDescScreenshot = ImageDescriptor.createFromURL(urlScreenshot);
		jSparrowImageScreenshot = imageDescScreenshot.createImage();

		Label welcomeLabel = new Label(parent, SWT.NONE);
		welcomeLabel.setText(Messages.StartDialog_welcomeLabel);

		Label thankyouLabel = new Label(parent, SWT.WRAP);
		gridData = new GridData();
		gridData.widthHint = 470;
		thankyouLabel.setLayoutData(gridData);
		thankyouLabel.setText(Messages.StartDialog_thankyouLabel);

		Label appreciationLabel = new Label(parent, SWT.WRAP);
		gridData = new GridData();
		gridData.widthHint = 470;
		appreciationLabel.setLayoutData(gridData);
		appreciationLabel.setText(Messages.StartDialog_appreciationLabel);

		Label quickStartLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 8;
		gridData.widthHint = 470;
		quickStartLabel.setLayoutData(gridData);
		quickStartLabel.setText(Messages.StartDialog_quickStartLabel);

		Label quickStartGoToLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 4;
		gridData.widthHint = 470;
		quickStartGoToLabel.setLayoutData(gridData);
		quickStartGoToLabel.setText(Messages.StartDialog_quickStartGoToLabel);

		Composite buttonsComposite = new Composite(parent, SWT.NONE);
		RowLayout buttonsLayout = new RowLayout();
		buttonsLayout.marginBottom = 0;
		buttonsLayout.marginTop = 0;
		buttonsComposite.setLayout(buttonsLayout);
		gridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
		gridData.verticalIndent = 4;
		buttonsComposite.setLayoutData(gridData);

		IPath iPathProjectExplorer = new Path(IMG_PATH_PROJECT_EXPLORER);
		URL urlProjectExplorer = FileLocator.find(bundle, iPathProjectExplorer, new HashMap<>());
		ImageDescriptor imageDescProjectExplorer = ImageDescriptor.createFromURL(urlProjectExplorer);
		Image projectExplorerImage = imageDescProjectExplorer.createImage();
		Label projectExplorerLabel = new Label(buttonsComposite, SWT.NONE);
		projectExplorerLabel.setImage(projectExplorerImage);

		IPath iPathPackageExplorer = new Path(IMG_PATH_PACKAGE_EXPLORER);
		URL urlPackageExplorer = FileLocator.find(bundle, iPathPackageExplorer, new HashMap<>());
		ImageDescriptor imageDescPackageExplorer = ImageDescriptor.createFromURL(urlPackageExplorer);
		Image packageExplorerImage = imageDescPackageExplorer.createImage();
		Label packageExplorerLabel = new Label(buttonsComposite, SWT.NONE);
		packageExplorerLabel.setImage(packageExplorerImage);

		IPath iPathClassEditor = new Path(IMG_PATH_CLASS_EDITOR);
		URL urlClassEditor = FileLocator.find(bundle, iPathClassEditor, new HashMap<>());
		ImageDescriptor imageDescClassEditor = ImageDescriptor.createFromURL(urlClassEditor);
		Image classEditorImage = imageDescClassEditor.createImage();
		Label classEditorLabel = new Label(buttonsComposite, SWT.NONE);
		classEditorLabel.setImage(classEditorImage);

		Label quickStartClickLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 4;
		gridData.widthHint = 470;
		quickStartClickLabel.setLayoutData(gridData);
		quickStartClickLabel.setText(Messages.StartDialog_quickStartClickLabel);

		screenshotLabel = new Label(parent, SWT.NONE);
		screenshotLabel.setImage(jSparrowImageScreenshot);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gridData.verticalIndent = 4;
		screenshotLabel.setLayoutData(gridData);

		Label goodToKnowLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 12;
		gridData.widthHint = 470;
		goodToKnowLabel.setLayoutData(gridData);
		goodToKnowLabel.setText(Messages.StartDialog_goodToKnowLabel);

		Label changesLabel = new Label(parent, SWT.WRAP);
		gridData = new GridData();
		gridData.widthHint = 470;
		changesLabel.setLayoutData(gridData);
		changesLabel.setText(Messages.StartDialog_changesLabel);

		Label customizeLabel = new Label(parent, SWT.WRAP);
		gridData = new GridData();
		gridData.widthHint = 470;
		customizeLabel.setLayoutData(gridData);
		customizeLabel.setText(Messages.StartDialog_customizeLabel);

		welcomeLabel.setFont(titleFont);
		thankyouLabel.setFont(paragraphTextFont);
		appreciationLabel.setFont(paragraphTextFont);
		quickStartLabel.setFont(paragraphTitleFont);
		quickStartGoToLabel.setFont(instructionsFont);
		quickStartClickLabel.setFont(instructionsFont);
		goodToKnowLabel.setFont(paragraphTitleFont);
		changesLabel.setFont(paragraphTextFont);
		customizeLabel.setFont(paragraphTextFont);

	}

	/**
	 * Create right side of welcome screen with links and rating.
	 * 
	 * @param parent
	 */
	private void createRightComposite(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gridData.verticalIndent = 82;
		parent.setLayoutData(gridData);

		Label wantMoreLabel = new Label(parent, SWT.NONE);
		wantMoreLabel.setText(Messages.StartDialog_wantMoreLabel);

		Link guidlinesLinkLabel = new Link(parent, SWT.NONE);
		guidlinesLinkLabel.setText(Messages.StartDialog_guidlinesLinkLabel);
		addLinkSelectionListener(guidlinesLinkLabel);

		Label buyLicenseLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 16;
		buyLicenseLabel.setLayoutData(gridData);
		buyLicenseLabel.setText(Messages.StartDialog_buyLicenseLabel);

		Link licenseLinkLabel = new Link(parent, SWT.NONE);
		licenseLinkLabel.setText(Messages.StartDialog_licenseLinkLabel);
		addLinkSelectionListener(licenseLinkLabel);

		Label likeItLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 12;
		likeItLabel.setLayoutData(gridData);
		likeItLabel.setText(Messages.StartDialog_likeItLabel);

		Link marketplaceLinkLabel = new Link(parent, SWT.NONE);
		marketplaceLinkLabel.setText(Messages.StartDialog_marketplaceLinkLabel);
		addLinkSelectionListener(marketplaceLinkLabel);

		Label rateUsLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 12;
		rateUsLabel.setLayoutData(gridData);
		rateUsLabel.setText(Messages.StartDialog_rateUsLabel);

		Composite buttonsComposite = new Composite(parent, SWT.NONE);
		RowLayout buttonsLayout = new RowLayout();
		buttonsLayout.marginBottom = 0;
		buttonsLayout.marginTop = 0;
		buttonsComposite.setLayout(buttonsLayout);
		gridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
		buttonsComposite.setLayoutData(gridData);

		IPath iPathSad = new Path(IMG_SAD_ICON);
		URL urlSad = FileLocator.find(bundle, iPathSad, new HashMap<>());
		ImageDescriptor imageDescSad = ImageDescriptor.createFromURL(urlSad);
		Image sadImage = imageDescSad.createImage();
		Button sadButton = new Button(buttonsComposite, SWT.RADIO);
		sadButton.setImage(sadImage);
		sadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				ratingText = "Bad"; //$NON-NLS-1$
			}
		});

		IPath iPathEven = new Path(IMG_EVEN_ICON);
		URL urlEven = FileLocator.find(bundle, iPathEven, new HashMap<>());
		ImageDescriptor imageDescEven = ImageDescriptor.createFromURL(urlEven);
		Image evenImage = imageDescEven.createImage();
		Button evenButton = new Button(buttonsComposite, SWT.RADIO);
		evenButton.setImage(evenImage);
		evenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				ratingText = "Average"; //$NON-NLS-1$
			}
		});

		IPath iPathHappy = new Path(IMG_HAPPY_ICON);
		URL urlHappy = FileLocator.find(bundle, iPathHappy, new HashMap<>());
		ImageDescriptor imageDescHappy = ImageDescriptor.createFromURL(urlHappy);
		Image happyImage = imageDescHappy.createImage();
		Button happyButton = new Button(buttonsComposite, SWT.RADIO);
		happyButton.setImage(happyImage);
		happyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				ratingText = "Good"; //$NON-NLS-1$
			}
		});

		IPath iPathInLove = new Path(IMG_INLOVE_ICON);
		URL urlInLove = FileLocator.find(bundle, iPathInLove, new HashMap<>());
		ImageDescriptor imageDescInLove = ImageDescriptor.createFromURL(urlInLove);
		Image inLoveImage = imageDescInLove.createImage();
		Button inLoveButton = new Button(buttonsComposite, SWT.RADIO);
		inLoveButton.setImage(inLoveImage);
		inLoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				ratingText = "Awesome"; //$NON-NLS-1$
			}
		});

		Label feedbackLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 8;
		feedbackLabel.setLayoutData(gridData);
		feedbackLabel.setText(Messages.StartDialog_feedbackLabel);

		Text feedback = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 70;
		feedback.setLayoutData(gridData);
		feedback.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				feedbackText = ((Text) e.getSource()).getText();
			}
		});
		Listener scrollBarListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				Text t = (Text) event.widget;
				Rectangle r1 = t.getClientArea();
				Rectangle r2 = t.computeTrim(r1.x, r1.y, r1.width, r1.height);
				Point p = t.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				// t.getHorizontalBar().setVisible(r2.width <= p.x);
				t.getVerticalBar().setVisible(r2.height <= p.y);
				if (event.type == SWT.Modify) {
					t.getParent().layout(true);
					t.showSelection();
				}
			}
		};
		feedback.addListener(SWT.Resize, scrollBarListener);
		feedback.addListener(SWT.Modify, scrollBarListener);

		wantMoreLabel.setFont(paragraphTitleFont);
		guidlinesLinkLabel.setFont(paragraphTextFont);
		buyLicenseLabel.setFont(paragraphTitleFont);
		licenseLinkLabel.setFont(paragraphTextFont);
		likeItLabel.setFont(paragraphTitleFont);
		marketplaceLinkLabel.setFont(paragraphTextFont);
		rateUsLabel.setFont(paragraphTextFont);
		feedbackLabel.setFont(paragraphTextFont);

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button disableIntro = new Button(parent, SWT.CHECK);
		disableIntro.setText(Messages.StartDialog_alwaysShowIntroText);
		disableIntro.setFont(JFaceResources.getDialogFont());
		disableIntro.setData(Integer.valueOf(5));
		disableIntro.setSelection(SimonykeesPreferenceManager.getEnableIntro());
		disableIntro.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// if button is selected, intro dialog should be shown
				Button btn = (Button) e.getSource();
				SimonykeesPreferenceManager.setEnableIntro(btn.getSelection());
			}
		});
		setButtonLayoutData(disableIntro);

		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected void okPressed() {
		if (!ratingText.isEmpty() || !feedbackText.isEmpty()) {
			try {
				sendPost();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

		}
		super.okPressed();
	}

	/**
	 * Called when user puts some input in feedback fields of welcome screen to
	 * send feedback in google forms survey.
	 * 
	 * @throws Exception
	 */
	private void sendPost() throws IOException {

		String googleFormUrl = "https://docs.google.com/forms/d/e/1FAIpQLSfu0RgpPC40rgPi6A0e92JaALDF5TsC7hkSW0_zK2aDhgLSJQ/formResponse"; //$NON-NLS-1$
		URL obj = new URL(googleFormUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST"); //$NON-NLS-1$

		String urlParameters = ""; //$NON-NLS-1$
		if (!ratingText.isEmpty()) {
			urlParameters += "entry.1293318463=" + ratingText; //$NON-NLS-1$
		}
		if (!feedbackText.isEmpty()) {
			if (!ratingText.isEmpty()) {
				urlParameters += "&"; //$NON-NLS-1$
			}
			urlParameters += "entry.112902755=" + feedbackText; //$NON-NLS-1$
		}

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

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
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(arg0.text));
				} catch (PartInitException | MalformedURLException e) {
					logger.error(e.getMessage(), e);
				}
			}
		});

	}
}
