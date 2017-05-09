package at.splendit.simonykees.core.ui.wizard.semiautomatic;

public class LoggerRuleWizardPageControler {

	private LoggerRuleWizardPageModel model;

	public LoggerRuleWizardPageControler(LoggerRuleWizardPageModel model) {
		this.model = model;
	}

	public void selectionChanged(String source, String selection) {
		model.setNewSelection(source, selection);
	}
}
