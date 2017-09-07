package eu.jsparrow.core.ui.dialog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
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

import eu.jsparrow.core.Activator;
import eu.jsparrow.i18n.Messages;

/**
 * This dialog is displayed when a user tries to start jSparrow with an expired
 * license. The dialog provides a link to buy a license and has an area for user
 * feedback. The feedback includes:
 * <ul>
 * <li>Smileys ranging from sad to happy to rate jSparrow.</li>
 * <li>Checkboxes to provide reasons why the customer does not want to buy a
 * license.</li>
 * <li>A text area for additional feedback.</li>
 * </ul>
 * The feedback is sent to a Google forms survey when the OK button is pressed.
 * 
 * @author Andreja Sambolec, Ludwig Werzowa
 * @since 2.0.3
 * 
 */
public class BuyLicenseDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(BuyLicenseDialog.class);

	private static final String LOGO_PATH_INACTIVE = "icons/jSparrow_inactive_icon_100.png"; //$NON-NLS-1$

	private static final String IMG_SAD_ICON = "icons/sad.png"; //$NON-NLS-1$
	private static final String IMG_EVEN_ICON = "icons/even.png"; //$NON-NLS-1$
	private static final String IMG_HAPPY_ICON = "icons/happy.png"; //$NON-NLS-1$
	private static final String IMG_INLOVE_ICON = "icons/extrahappy.png"; //$NON-NLS-1$

	private Bundle bundle;

	private String message;

	private String ratingText = ""; //$NON-NLS-1$
	private ArrayList<String> reasonForNotBuying = new ArrayList<>();
	private String feedbackText = ""; //$NON-NLS-1$

	private CheckboxTableViewer viewer;

	/**
	 * BuyLicenseDialog is shown when license is expired
	 * 
	 * @author andreja.sambolec
	 * 
	 * @since 2.0.2
	 * 
	 */
	public BuyLicenseDialog(Shell parentShell, String message) {
		super(parentShell);
		this.message = message;

		bundle = Platform.getBundle(Activator.PLUGIN_ID);
	}

	@Override
	protected Control createDialogArea(Composite composite) {
		Composite area = (Composite) super.createDialogArea(composite);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 10;
		area.setLayout(gridLayout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		area.setLayoutData(gridData);

		Composite titleContainer = new Composite(area, SWT.NONE);
		GridLayout titleLayout = new GridLayout(2, false);
		titleContainer.setLayout(titleLayout);
		gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		titleContainer.setLayoutData(gridData);

		Dialog.applyDialogFont(composite);
		Font font = composite.getDisplay().getSystemFont();

		FontDescriptor boldFontDescription = FontDescriptor.createFrom(font).setStyle(SWT.BOLD);
		Font boldFont = boldFontDescription.createFont(composite.getDisplay());

		IPath iPathInactive = new Path(LOGO_PATH_INACTIVE);
		URL urlInactive = FileLocator.find(bundle, iPathInactive, new HashMap<>());
		ImageDescriptor imageDescInactive = ImageDescriptor.createFromURL(urlInactive);
		Image jSparrowImageInactive = imageDescInactive.createImage();

		Label logoLabel = new Label(titleContainer, SWT.NONE);
		logoLabel.setImage(jSparrowImageInactive);

		Label titleLabel = new Label(titleContainer, SWT.NONE);
		titleLabel.setFont(boldFont);
		titleLabel.setText(message);

		Link link = new Link(area, SWT.NONE);
		link.setText(Messages.BuyLicenseDialog_purchaseLinkLabel);
		link.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(arg0.text));
				} catch (PartInitException | MalformedURLException e) {
					logger.error(Messages.SimonykeesMessageDialog_open_browser_error_message, e);
				}
			}
		});
		link.setFont(boldFont);

		createRatingForm(area);

		return composite;
	}

	@Override
	protected void okPressed() {
		if (!ratingText.isEmpty() || !reasonForNotBuying.isEmpty() || !feedbackText.isEmpty()) {
			try {
				sendPost();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		super.okPressed();
	}

	private void createRatingForm(Composite parent) {
		Label opinionLabel = new Label(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalIndent = 12;
		opinionLabel.setLayoutData(gridData);
		opinionLabel.setText(Messages.BuyLicenseDialog_opinionLabel);

		Label rateUsLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 8;
		rateUsLabel.setLayoutData(gridData);
		rateUsLabel.setText(Messages.BuyLicenseDialog_rateUsLabel);

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

		Label reasonsLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 8;
		reasonsLabel.setLayoutData(gridData);
		reasonsLabel.setText(Messages.BuyLicenseDialog_reasonsLabel);

		createReasonsView(parent);

		Label feedbackLabel = new Label(parent, SWT.NONE);
		gridData = new GridData();
		gridData.verticalIndent = 8;
		feedbackLabel.setLayoutData(gridData);
		feedbackLabel.setText(Messages.BuyLicenseDialog_feedbackLabel);

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

	}

	private void createReasonsView(Composite parent) {
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		/*
		 * label provider that sets the text displayed in CompilationUnits table
		 * to show the name of the CompilationUnit
		 */
		viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return element.toString();
			}
		});

		viewer.addCheckStateListener(createCheckStateListener());

		populateFileView();

	}

	private ICheckStateListener createCheckStateListener() {
		return new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {

				String reason = (String) event.getElement();
				if (event.getChecked()) {
					reasonForNotBuying.add(reason);
				} else {
					// add in list with unselected classes
					if (reasonForNotBuying.contains(reason)) {
						reasonForNotBuying.remove(reason);
					}
				}
			}
		};
	}

	private void populateFileView() {
		viewer.add(Messages.BuyLicenseDialog_reason1);
		viewer.add(Messages.BuyLicenseDialog_reason2);
		viewer.add(Messages.BuyLicenseDialog_reason3);
		viewer.add(Messages.BuyLicenseDialog_reason4);
		viewer.add(Messages.BuyLicenseDialog_reason5);
		viewer.add(Messages.BuyLicenseDialog_reason6);
		viewer.add(Messages.BuyLicenseDialog_reason7);
	}

	/**
	 * Called when user puts some input in feedback fields of welcome screen to
	 * send feedback in google forms survey.
	 * 
	 * @throws Exception
	 */
	private void sendPost() throws IOException {

		String googleFormUrl = "https://docs.google.com/forms/d/e/1FAIpQLSfkNEtii60RpAySC2mEjtzXhKOjbjlI7iKfBtPapKBp1MOY7g/formResponse"; //$NON-NLS-1$
		URL obj = new URL(googleFormUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST"); //$NON-NLS-1$

		String urlParameters = ""; //$NON-NLS-1$
		if (!ratingText.isEmpty()) {
			urlParameters += "entry.1585752170=" + ratingText; //$NON-NLS-1$
		}
		if (!reasonForNotBuying.isEmpty()) {
			for (String reason : reasonForNotBuying) {
				if (!urlParameters.isEmpty()) {
					urlParameters += "&"; //$NON-NLS-1$
				}
				urlParameters += "entry.808545363=" + reason; //$NON-NLS-1$
			}
		}
		if (!feedbackText.isEmpty()) {
			if (!urlParameters.isEmpty()) {
				urlParameters += "&"; //$NON-NLS-1$
			}
			urlParameters += "entry.1415953295=" + feedbackText; //$NON-NLS-1$
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
}
