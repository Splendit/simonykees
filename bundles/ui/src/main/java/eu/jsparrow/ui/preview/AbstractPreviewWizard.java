package eu.jsparrow.ui.preview;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.NewLicenseUtil;

/**
 * A parent class for all preview wizards.
 * 
 * @author Ardit Ymeri
 * @since 2.3.1
 *
 */
public abstract class AbstractPreviewWizard extends Wizard {

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (NewLicenseUtil.get()
			.isFreeLicense()) {
			return false;
		}
		return super.canFinish();
	}

	public abstract void updateViewsOnNavigation(IWizardPage page);

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		updateViewsOnNavigation(page);
		return super.getPreviousPage(page);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		updateViewsOnNavigation(page);
		return super.getNextPage(page);
	}
}
