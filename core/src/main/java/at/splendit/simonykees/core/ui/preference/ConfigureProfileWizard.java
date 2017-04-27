package at.splendit.simonykees.core.ui.preference;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.wizard.Wizard;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.ui.wizard.impl.AbstractSelectRulesWizardPage;
import at.splendit.simonykees.core.ui.wizard.impl.SelectRulesWizardPageControler;
import at.splendit.simonykees.core.ui.wizard.impl.SelectRulesWizardPageModel;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

public class ConfigureProfileWizard extends Wizard {

	private AbstractSelectRulesWizardPage page;
	private SelectRulesWizardPageControler controler;
	private SelectRulesWizardPageModel model;

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
		model = new SelectRulesWizardPageModel(rules);
		controler = new SelectRulesWizardPageControler(model);
		page = new ConfigureProfileSelectRulesWizardPage(model, controler, profileId);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		int index = SimonykeesPreferenceManager.getProfiles().indexOf(SimonykeesPreferenceManager.getProfileFromName(profileId));
		String name = "Profile 1";//model.getNameText();
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> ruleIds = model.getSelectionAsList();
		if(index >= 0) {
			SimonykeesPreferenceManager.updateProfile(index, name, ruleIds.stream().map(rule -> rule.getId()).collect(Collectors.toList()));
		} else {
			SimonykeesPreferenceManager.addProfile(name, ruleIds.stream().map(rule -> rule.getId()).collect(Collectors.toList()));
		}
		return true;
	}

}
