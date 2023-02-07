package eu.jsparrow.ui.preview;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;

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

	public void updateContainerOnCommit() {
		IWizardContainer container = getContainer();
		if (container instanceof PreviewWizardDialog) {
			PreviewWizardDialog previewWizardDialog = (PreviewWizardDialog) container;
			previewWizardDialog.updateOnCommit();
		}
	}

	/**
	 * Called from {@link WizardDialog} when Next button is pressed. Triggers
	 * recalculation if needed. Disposes control from current page which wont be
	 * visible any more
	 */
	protected abstract void pressedNext();

	/**
	 * Called from {@link WizardDialog} when Back button is pressed. Disposes
	 * all controls to be recalculated and created when needed
	 */
	protected abstract void pressedBack();
	
	
	protected abstract boolean needsSummaryPage();
	
	
	public abstract void showSummaryPage();
}
