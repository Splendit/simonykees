package eu.jsparrow.ui.preview;

import org.eclipse.swt.widgets.Composite;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.model.summary.AbstractSummaryWizardPageModel;
import eu.jsparrow.ui.preview.model.summary.RefactoringSummaryWizardPageModel;

public class RefactoringSummaryWizardPage extends AbstractSummaryWizardPage {

	protected RefactoringSummaryWizardPage(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel, boolean enabledFinishButton, StandaloneStatisticsMetadata statisticsMetadata) {
		super(refactoringPipeline, wizardModel, enabledFinishButton, statisticsMetadata);
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
		addRulesSection();
		addFilesSection();
		initializeDataBindings();
	}

	@Override
	protected void initializeDataBindings() {
		super.initializeDataBindings();
		super.initializeRuleTableDataBindings();
	}

	@Override
	protected AbstractSummaryWizardPageModel summaryPageModelFactory(RefactoringPipeline pipeline, RefactoringPreviewWizardModel wizardModel) {
		return new RefactoringSummaryWizardPageModel(pipeline, wizardModel);
	}
}
