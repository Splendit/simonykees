package eu.jsparrow.ui.preference;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;

import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.preference.profile.SimonykeesProfile;
import eu.jsparrow.ui.util.ResourceHelper;

/**
 * Wizard for selecting rules when creating new profile in preferences page
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class ConfigureProfileWizard extends Wizard {

	private static final String WINDOW_ICON = "icons/jsparrow-icon-16-003.png"; //$NON-NLS-1$

	private ConfigureProfileSelectRulesWIzardPageModel model;

	private String profileId;

	private boolean isProfileSetAsDefault = false;

	private final List<RefactoringRule> rules;

	public ConfigureProfileWizard(String profileId, boolean isProfileSetAsDefault) {
		super();
		this.rules = RulesContainer.getAllRules(false);
		this.profileId = profileId;
		this.isProfileSetAsDefault = isProfileSetAsDefault;
		Image windowIcon = ResourceHelper.createImage(WINDOW_ICON);
		org.eclipse.jface.window.Window.setDefaultImage(windowIcon);
	}

	@Override
	public String getWindowTitle() {
		return Messages.SelectRulesWizard_title;
	}

	@Override
	public void addPages() {
		model = new ConfigureProfileSelectRulesWIzardPageModel(rules, profileId);
		ConfigureProfileSelectRulesWizardPageControler controler = new ConfigureProfileSelectRulesWizardPageControler(
				model);
		ConfigureProfileSelectRulesWizardPage page = new ConfigureProfileSelectRulesWizardPage(model, controler,
				profileId);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		String name = model.getName();
		List<RefactoringRule> ruleIds = model.getSelectionAsList();

		Optional<SimonykeesProfile> optionalProfile = SimonykeesPreferenceManager.getProfileFromName(profileId);

		if (optionalProfile.isPresent()) {
			int index = SimonykeesPreferenceManager.getProfiles()
				.indexOf(optionalProfile.get());
			SimonykeesPreferenceManager.updateProfile(index, name, ruleIds.stream()
				.map(RefactoringRule::getId)
				.collect(Collectors.toList()), isProfileSetAsDefault);
		} else {
			SimonykeesPreferenceManager.addProfile(name, ruleIds.stream()
				.map(RefactoringRule::getId)
				.collect(Collectors.toList()));
		}

		return true;
	}

	@Override
	public boolean canFinish() {
		if (model.getSelectionAsList()
			.isEmpty()) {
			return false;
		} else if (StringUtils.isEmpty(model.getName())) {
			// if name already exists check is handled in page with status on
			// field change
			return false;
		} else {
			return super.canFinish();
		}
	}
	
	@Override
	public void dispose() {
		org.eclipse.jface.window.Window.getDefaultImage().dispose();
	}
}
