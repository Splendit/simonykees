package at.splendit.simonykees.core.ui.preference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import at.splendit.simonykees.core.ui.wizard.impl.AbstractSelectRulesWizardPage;
import at.splendit.simonykees.core.ui.wizard.impl.SelectRulesWizardPageControler;
import at.splendit.simonykees.core.ui.wizard.impl.SelectRulesWizardPageModel;

public class ConfigureProfileSelectRulesWizardPage extends AbstractSelectRulesWizardPage {

	private Label nameInputLabel;
	private Text nameInputText;

	private String profileId;

	public ConfigureProfileSelectRulesWizardPage(SelectRulesWizardPageModel model,
			SelectRulesWizardPageControler controler, String profileId) {
		super(model, controler);
		this.profileId = profileId;
		if (!this.profileId.isEmpty()) {
			controler.profileChanged(this.profileId);
		}
	}

	@Override
	protected void createFilteringPart(Composite composite) {
		nameInputLabel = new Label(composite, SWT.NONE);
		nameInputLabel.setText("Enter new profile name:");

		nameInputText = new Text(composite, SWT.NONE);
		nameInputText.setMessage("New name");
		if (!profileId.isEmpty()) {
			nameInputText.setText(profileId);
		}
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		gridData.widthHint = 200;
		nameInputText.setLayoutData(gridData);
		nameInputText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text source = (Text) e.getSource();
				// controler.nameFilterTextChanged(source.getText());
			}
		});

	}

}
