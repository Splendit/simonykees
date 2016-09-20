package at.splendit.simonykees.core.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;

/**
 * This class has common functionality for all Simonykees WizardPages. 
 * At the moment, this only includes a help page. 
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
	    HelpMessageDialog.openDefaultHelpMessageDialog(getShell());
	}

}
