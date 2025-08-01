package eu.jsparrow.ui.preference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.wizard.impl.AbstractSelectRulesWizardPage;

/**
 * Wizard page for selecting rules when creating new profile in preferences page
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
@SuppressWarnings("restriction") // StatusInfo is internal
public class ConfigureProfileSelectRulesWizardPage extends AbstractSelectRulesWizardPage {

	private Label nameInputLabel;
	private Text nameInputText;

	private String profileId;

	protected IStatus fTypeNameStatus;

	public ConfigureProfileSelectRulesWizardPage(ConfigureProfileSelectRulesWIzardPageModel model,
			ConfigureProfileSelectRulesWizardPageControler controler, String profileId) {
		super(model, controler);
		this.profileId = profileId;
		controler.profileChanged(this.profileId);
		fTypeNameStatus = new StatusInfo();
	}

	@Override
	protected void createFilteringPart(Composite composite) {
		nameInputLabel = new Label(composite, SWT.NONE);
		nameInputLabel.setText(Messages.ConfigureProfileSelectRulesWizardPage_nameInputLabel);

		nameInputText = new Text(composite, SWT.NONE);
		nameInputText.setMessage(Messages.ConfigureProfileSelectRulesWizardPage_nameInputText);
		if (!StringUtils.isEmpty(profileId)) {
			nameInputText.setText(profileId);
		}
		if (Messages.Profile_DefaultProfile_profileName.equals(profileId)) {
			nameInputText.setEnabled(false);
		}
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		gridData.widthHint = 200;
		nameInputText.setLayoutData(gridData);
		nameInputText.addModifyListener((ModifyEvent e) -> {
			Text source = (Text) e.getSource();
			fTypeNameStatus = ((ConfigureProfileSelectRulesWizardPageControler) controler)
				.nameTextChanged(source.getText());
			doStatusUpdate();
			getContainer().updateButtons();
		});
	}

	@Override
	protected void doStatusUpdate() {
		// if name is changed and already exists in profiles list it can not be
		// used, name has to be unique
		if (fTypeNameStatus.isOK()) {
			if (StringUtils.isEmpty(((ConfigureProfileSelectRulesWIzardPageModel) model).getName())) {
				((StatusInfo) fTypeNameStatus).setError(Messages.ConfigureProfileSelectRulesWizardPage_error_EmptyName);
			} else {
				fTypeNameStatus = new StatusInfo();
			}
		} else {
			// if status already contains exception, do nothing
		}

		super.doStatusUpdate(fTypeNameStatus);
	}

}
