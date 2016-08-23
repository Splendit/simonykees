/**
 * 
 */
package at.splendit.simonykees.core.dialogs;


import org.eclipse.jface.wizard.Wizard;

public class DiffViewWizard extends Wizard {
	
	public DiffViewWizard() {
		addPage(new MyPage("Test", "Page 1"));
		addPage(new MyPage("Test", "Page 2"));
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}
