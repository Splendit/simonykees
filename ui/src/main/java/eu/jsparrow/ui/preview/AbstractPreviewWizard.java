package eu.jsparrow.ui.preview;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * A parent class for all preview wizards.
 * 
 * @author Ardit Ymeri
 * @since 2.3.1
 *
 */
public abstract class AbstractPreviewWizard extends Wizard {

	protected RefactoringSummaryWizardPage summaryPage;

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (!LicenseUtil.getInstance()
			.isFullLicense()) {
			return false;
		}
		return super.canFinish();
	}
	
	protected abstract void updateViewsOnNavigation(IWizardPage page);

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

	protected void addSummaryPage(RefactoringPipeline refactoringPipeline, RefactoringPreviewWizardModel model) {
		this.summaryPage = new RefactoringSummaryWizardPage(refactoringPipeline, model);
		addPage(summaryPage);
	}

	public RefactoringSummaryWizardPage getSummaryPage() {
		return summaryPage;
	}
}
