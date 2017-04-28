package at.splendit.simonykees.core.ui.preference;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.wizard.impl.AbstractSelectRulesWizardModel;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

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

	@SuppressWarnings("restriction")
	public IStatus setName(String name) {
		StatusInfo status = new StatusInfo();
		// if name is changed and already exists in profiles list it can not be
		// used, name has to be unique
		if (SimonykeesPreferenceManager.getAllProfileIds().contains(name.trim())
				&& !name.trim().equals(this.name)) {
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
