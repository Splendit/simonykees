package eu.jsparrow.core.ui.preference;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.ui.wizard.impl.AbstractSelectRulesWizardModel;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;

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
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules, String profileId) {
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
		if (SimonykeesPreferenceManager.getAllProfileIds().contains(name.trim()) && !name.trim().equals(this.name)) {
			status.setError(Messages.ConfigureProfileSelectRulesWIzardPageModel_error_NameExists);
		} else {
			this.newName = name.trim();
		}
		return status;
	}

	@Override
	public String getNameFilter() {
		return ""; //$NON-NLS-1$
	}
}
