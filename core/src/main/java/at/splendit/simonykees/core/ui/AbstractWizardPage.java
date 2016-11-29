package at.splendit.simonykees.core.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;

import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;

/**
 * This class has common functionality for all Simonykees WizardPages. At the
 * moment, this only includes a help page.
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa
 * @since 0.9
 * 
 */
public abstract class AbstractWizardPage extends WizardPage {

	public AbstractWizardPage(String pageName) {
		super(pageName);
	}

	protected AbstractWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/**
	 * Open help dialog
	 */
	@Override
	public void performHelp() {
		SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
	}

}
