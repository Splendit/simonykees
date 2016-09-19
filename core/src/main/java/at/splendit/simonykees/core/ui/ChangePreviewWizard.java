package at.splendit.simonykees.core.ui;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import at.splendit.simonykees.core.i18n.Messages;

public class ChangePreviewWizard extends Wizard {
	
	private DocumentChange documentChange;
	
	public ChangePreviewWizard(DocumentChange documentChange) {
		setWindowTitle(Messages.ChangePreviewWizard_ChangePreview);
		this.documentChange = documentChange;
	}

	@Override
	public void addPages() {
		addPage(new ChangePreviewWizardPage(this.documentChange));
	}
	
	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public boolean performCancel() {
		if (documentChange != null) {
			documentChange.dispose();
		}
		return super.performCancel();
	}

}
