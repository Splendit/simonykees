package eu.jsparrow.ui.preview;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preview.comparator.RenamingRuleSummaryTableViewerComparator;
import eu.jsparrow.ui.preview.comparator.SortableViewerComparator;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.model.summary.ChangedNamesInFileModel;
import eu.jsparrow.ui.preview.model.summary.RenamingSummaryWizardPageModel;

public class RenamingRuleSummaryWizardPage extends AbstractSummaryWizardPage<RenamingSummaryWizardPageModel> {

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
	protected RenamingSummaryWizardPageModel summaryPageModelFactory(RefactoringPipeline pipeline,
			RefactoringPreviewWizardModel wizardModel) {
		return new RenamingSummaryWizardPageModel(pipeline, wizardModel);
	}

	@Override
	protected void initializeFileTableViewer() {
		RenamingSummaryWizardPageModel summaryWizardPageModel = getSummaryPageModel();
		ViewerSupport.bind(fileTableViewer, summaryWizardPageModel.getChangedFiles(),
				BeanProperties.values("fileName")); //$NON-NLS-1$

		IViewerObservableValue selectedFile = ViewerProperties.singleSelection()
			.observe(fileTableViewer);
		ViewerSupport.bind(rulesPerFileTableViewer, summaryWizardPageModel.getRulesPerFile(),
				BeanProperties.values("name", "times")); //$NON-NLS-1$ //$NON-NLS-2$

		selectedFile.addValueChangeListener(e -> {
			ChangedNamesInFileModel selectedItem = (ChangedNamesInFileModel) e.getObservableValue()
				.getValue();

			if (selectedItem != null) {
				summaryWizardPageModel.updateRulesPerFile(selectedItem.getRenamings());
			}
		});
	}

	@Override
	protected void addRulePerFileSection(Composite filesSectionComposite) {
		Composite rulesInFileComposite = new Composite(filesSectionComposite, SWT.NONE);
		rulesInFileComposite.setLayout(new GridLayout(1, true));
		rulesInFileComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		rulesPerFileTableViewer = new TableViewer(rulesInFileComposite, SWT.SINGLE);
		Table table = rulesPerFileTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setToolTipText(Messages.AbstractSummaryWizardPage_rulesPerFileTableViewerToolTipText);

		SortableViewerComparator comparator = new RenamingRuleSummaryTableViewerComparator();
		rulesPerFileTableViewer.setComparator(comparator);

		TableViewerColumn ruleNameCol = createSortableTableViewerColumn(rulesPerFileTableViewer,
				Messages.AbstractSummaryWizardPage_rulesPerFileTableViewerTitle,
				Messages.AbstractSummaryWizardPage_rulesPerFileTableViewerToolTipText, 0, comparator);

		TableColumn namesCol = ruleNameCol.getColumn();

		TableViewerColumn ruleTimesCol = createSortableTableViewerColumn(rulesPerFileTableViewer,
				Messages.RenamingRuleSummaryWizardPage_times, Messages.RenamingRuleSummaryWizardPage_timesToolTipText,
				1, comparator);

		TableColumn timesCol = ruleTimesCol.getColumn();

		TableColumnLayout rulesInFileTableLayout = new TableColumnLayout();
		rulesInFileComposite.setLayout(rulesInFileTableLayout);
		rulesInFileTableLayout.setColumnData(namesCol, new ColumnWeightData(70));
		rulesInFileTableLayout.setColumnData(timesCol, new ColumnWeightData(30));

	}

	@Override
	protected void addFileTableViewerSection(Composite filesSectionComposite) {
		Composite filesComposite = new Composite(filesSectionComposite, SWT.NONE);
		filesComposite.setLayout(new GridLayout(1, true));
		filesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fileTableViewer = new TableViewer(filesComposite, SWT.SINGLE);
		Table fileTable = fileTableViewer.getTable();
		fileTable.setHeaderVisible(true);
		fileTable.setLinesVisible(true);
		fileTable.setToolTipText(Messages.AbstractSummaryWizardPage_fileTableViewerToolTipText);

		// sort files alphabetically (SIM-922)
		fileTableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				ChangedNamesInFileModel model1 = (ChangedNamesInFileModel) e1;
				ChangedNamesInFileModel model2 = (ChangedNamesInFileModel) e2;
				return model1.getFileName()
					.compareTo(model2.getFileName());
			}
		});
		TableViewerColumn filePathCol = new TableViewerColumn(fileTableViewer, SWT.NONE);
		TableColumn pathColumn = filePathCol.getColumn();
		pathColumn.setText(Messages.AbstractSummaryWizardPage_fileTableViewerTitle);
		pathColumn.setToolTipText(Messages.AbstractSummaryWizardPage_fileTableViewerToolTipText);

		TableColumnLayout filesTableLayout = new TableColumnLayout();
		filesComposite.setLayout(filesTableLayout);
		filesTableLayout.setColumnData(pathColumn, new ColumnWeightData(100));

	}
}
