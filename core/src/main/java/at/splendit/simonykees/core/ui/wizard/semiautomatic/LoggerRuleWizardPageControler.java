package at.splendit.simonykees.core.ui.wizard.semiautomatic;

/**
 * Wizard page controller for configuring logger rule when applying to selected
 * resources
 * 
 * @author andreja.sambolec
 * @since 1.2
 *
 */
public class LoggerRuleWizardPageControler {

	private LoggerRuleWizardPageModel model;

	public LoggerRuleWizardPageControler(LoggerRuleWizardPageModel model) {
		this.model = model;
	}

	public void selectionChanged(String source, String selection) {
		model.setNewSelection(source, selection);
	}
}
