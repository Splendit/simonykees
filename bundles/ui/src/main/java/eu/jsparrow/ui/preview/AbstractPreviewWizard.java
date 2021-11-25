package eu.jsparrow.ui.preview;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;

/**
 * A parent class for all preview wizards.
 * 
 * @author Ardit Ymeri
 * @since 2.3.1
 *
 */
public abstract class AbstractPreviewWizard extends Wizard {

	private LicenseUtilService licenseUtil = LicenseUtil.get();

	protected AbstractPreviewWizard() {
		ContextInjectionFactory.inject(this, Activator.getEclipseContext());
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (licenseUtil.isFreeLicense()) {
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
