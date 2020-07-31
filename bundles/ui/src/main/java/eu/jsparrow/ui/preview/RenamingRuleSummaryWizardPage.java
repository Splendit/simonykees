package eu.jsparrow.ui.preview;

import org.eclipse.swt.widgets.Composite;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.model.summary.AbstractSummaryWizardPageModel;
import eu.jsparrow.ui.preview.model.summary.RenamingSummaryWizardPageModel;

public class RenamingRuleSummaryWizardPage extends AbstractSummaryWizardPage {

	public RenamingRuleSummaryWizardPage(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel, boolean enabledFinishButton) {
		super(refactoringPipeline, wizardModel, enabledFinishButton);
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		addHeader();
		addFilesSection();
		initializeDataBindings();
	}

	@Override
	protected AbstractSummaryWizardPageModel summaryPageModelFactory(RefactoringPipeline pipeline,
			RefactoringPreviewWizardModel wizardModel) {
		return new RenamingSummaryWizardPageModel(pipeline, wizardModel);
	}
}
