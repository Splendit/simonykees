package eu.jsparrow.ui.preference;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.osgi.util.NLS;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.wizard.impl.AbstractSelectRulesWizardModel;

/**
 * Model for Wizard page for selecting rules when creating new profile in
 * preferences page
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
@SuppressWarnings("restriction") // StatusInfo is internal
public class ConfigureProfileSelectRulesWIzardPageModel extends AbstractSelectRulesWizardModel {

	private String name;
	private String newName;

	public ConfigureProfileSelectRulesWIzardPageModel(
			List<RefactoringRule> rules, String profileId) {
		super(rules);
		this.name = profileId;
		this.newName = name;
	}

	@Override
	public Set<Object> filterPosibilitiesByName() {
		return super.getAllPosibilities();
	}

	@Override
	public void filterPosibilitiesByTags() {
		// do nothing
	}

	public String getName() {
		return newName;
	}

	public IStatus setName(String name) {
		StatusInfo status = new StatusInfo();
		// if name is changed and already exists in profiles list it can not be
		// used, name has to be unique
		String value = StringUtils.trim(name);
		List<String> existingProfiles = SimonykeesPreferenceManager.getAllProfileIds();
		String customProfileLabel = Messages.SelectRulesWizardPage_CustomProfileLabel;
		if (existingProfiles.contains(value) && !value.equals(this.name)) {
			status.setError(Messages.ConfigureProfileSelectRulesWizardPageModel_error_NameExists);
			this.newName = null;
		} else if (StringUtils.equals(value, customProfileLabel)) {
			status.setError(NLS.bind(Messages.ConfigureProfileSelectRulesWIzardPageModel_error_nameReserved, value));
			this.newName = null;
		} else {
			this.newName = value;
		}
		return status;
	}

	@Override
	public String getNameFilter() {
		return ""; //$NON-NLS-1$
	}
}
