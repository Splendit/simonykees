package at.splendit.simonykees.core.ui.preference;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import at.splendit.simonykees.core.ui.wizard.impl.AbstractSelectRulesWizardPage;
import at.splendit.simonykees.i18n.Messages;

public class ConfigureProfileSelectRulesWizardPage extends AbstractSelectRulesWizardPage {

	private Label nameInputLabel;
	private Text nameInputText;

	private String profileId;

	protected IStatus fTypeNameStatus;

	private ConfigureProfileSelectRulesWizardPageControler controler;

	@SuppressWarnings("restriction")
	public ConfigureProfileSelectRulesWizardPage(ConfigureProfileSelectRulesWIzardPageModel model,
			ConfigureProfileSelectRulesWizardPageControler controler, String profileId) {
		super(model, controler);
		this.controler = controler;
		this.profileId = profileId;
		if (!this.profileId.isEmpty()) {
			controler.profileChanged(this.profileId);
		}
		fTypeNameStatus = new StatusInfo();
	}

	@Override
	protected void createFilteringPart(Composite composite) {
		nameInputLabel = new Label(composite, SWT.NONE);
		nameInputLabel.setText(Messages.ConfigureProfileSelectRulesWizardPage_nameInputLabel);

		nameInputText = new Text(composite, SWT.NONE);
		nameInputText.setMessage(Messages.ConfigureProfileSelectRulesWizardPage_nameInputText);
		if (!profileId.isEmpty()) {
			nameInputText.setText(profileId);
		}
		if (profileId.equals(Messages.Profile_DefaultProfile_profileName)) {
			nameInputText.setEnabled(false);
		}
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		gridData.widthHint = 200;
		nameInputText.setLayoutData(gridData);
		nameInputText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text source = (Text) e.getSource();
				fTypeNameStatus = controler.nameTextChanged(source.getText());
				doStatusUpdate();
				getContainer().updateButtons();
			}
		});
	}

	private void doStatusUpdate() {
		// status of all used components
		IStatus[] status = new IStatus[] {fTypeNameStatus};

		// the mode severe status will be displayed and the OK button
		// enabled/disabled.
		updateStatus(status);
	}

}
