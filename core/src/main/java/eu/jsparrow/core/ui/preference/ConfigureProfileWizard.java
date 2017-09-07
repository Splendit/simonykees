package eu.jsparrow.core.ui.preference;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.wizard.Wizard;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.ui.wizard.impl.AbstractSelectRulesWizardPage;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * Wizard for selecting rules when creating new profile in preferences page
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class ConfigureProfileWizard extends Wizard {

	private AbstractSelectRulesWizardPage page;
	private ConfigureProfileSelectRulesWizardPageControler controler;
	private ConfigureProfileSelectRulesWIzardPageModel model;

	private String profileId;

	private final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules;

	public ConfigureProfileWizard(String profileId) {
		super();
		this.rules = RulesContainer.getAllRules();
		this.profileId = profileId;
	}

	@Override
	public String getWindowTitle() {
		return Messages.SelectRulesWizard_title;
	}

	@Override
	public void addPages() {
		model = new ConfigureProfileSelectRulesWIzardPageModel(rules, profileId);
		controler = new ConfigureProfileSelectRulesWizardPageControler(model);
		page = new ConfigureProfileSelectRulesWizardPage(model, controler, profileId);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		int index = SimonykeesPreferenceManager.getProfiles()
				.indexOf(SimonykeesPreferenceManager.getProfileFromName(profileId));
		String name = ((ConfigureProfileSelectRulesWIzardPageModel) model).getName();
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> ruleIds = model.getSelectionAsList();
		if (index >= 0) {
			SimonykeesPreferenceManager.updateProfile(index, name,
					ruleIds.stream().map(rule -> rule.getId()).collect(Collectors.toList()));
		} else {
			SimonykeesPreferenceManager.addProfile(name,
					ruleIds.stream().map(rule -> rule.getId()).collect(Collectors.toList()));
		}
		return true;
	}

	@Override
	public boolean canFinish() {
		if (model.getSelectionAsList().isEmpty()) {
			return false;
		} else if (model.getName().isEmpty()) {
			// if name already exists check is handled in page with status on
			// field change
			return false;
		} else {
			return super.canFinish();
		}
	}
}
