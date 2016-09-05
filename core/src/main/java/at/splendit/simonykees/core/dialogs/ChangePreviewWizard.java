package at.splendit.simonykees.core.dialogs;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;

public class ChangePreviewWizard extends Wizard {
	
	private TextEditBasedChange textEditBasedChange;
	
	public ChangePreviewWizard(TextEditBasedChange textEditBasedChange) {
		setWindowTitle("AWESOME PREVIEW");
		this.textEditBasedChange = textEditBasedChange;
	}
	
	@Override
	public boolean performFinish() {
		return false;
	}

	@Override
	public void addPages() {
		addPage(new ChangePreviewWizardPage(this.textEditBasedChange));
	}


}
