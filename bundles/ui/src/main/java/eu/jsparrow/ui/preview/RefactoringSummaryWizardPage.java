package eu.jsparrow.ui.preview;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
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
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.model.summary.ChangedFilesModel;
import eu.jsparrow.ui.preview.model.summary.RefactoringSummaryWizardPageModel;
import eu.jsparrow.ui.preview.model.summary.RulesPerFileModel;

public class RefactoringSummaryWizardPage extends AbstractSummaryWizardPage<RefactoringSummaryWizardPageModel> {

	protected RefactoringSummaryWizardPage(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel, boolean enabledFinishButton,
			StandaloneStatisticsMetadata statisticsMetadata) {
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
	protected RefactoringSummaryWizardPageModel summaryPageModelFactory(RefactoringPipeline pipeline,
			RefactoringPreviewWizardModel wizardModel) {
		return new RefactoringSummaryWizardPageModel(pipeline, wizardModel);
	}

	@Override
	protected void initializeFileTableViewer() {
		RefactoringSummaryWizardPageModel summaryWizardPageModel = getSummaryPageModel();
		ViewerSupport.bind(fileTableViewer, summaryWizardPageModel.getChangedFiles(), BeanProperties.values("name")); //$NON-NLS-1$

		IViewerObservableValue<Object> selectedFile = ViewerProperties.singleSelection()
			.observe(fileTableViewer);
		ViewerSupport.bind(rulesPerFileTableViewer, summaryWizardPageModel.getRulesPerFile(),
				BeanProperties.values("name")); //$NON-NLS-1$

		selectedFile.addValueChangeListener(e -> {
			ChangedFilesModel selectedItem = (ChangedFilesModel) e.getObservableValue()
				.getValue();

			if (selectedItem != null) {
				summaryWizardPageModel.updateRulesPerFile(selectedItem.getRules());
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
		rulesPerFileTableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				RulesPerFileModel model1 = (RulesPerFileModel) e1;
				RulesPerFileModel model2 = (RulesPerFileModel) e2;
				return model1.getName()
					.compareTo(model2.getName());
			}
		});
		TableViewerColumn ruleNameCol = new TableViewerColumn(rulesPerFileTableViewer, SWT.NONE);
		TableColumn column = ruleNameCol.getColumn();
		column.setText(Messages.AbstractSummaryWizardPage_rulesPerFileTableViewerTitle);
		column.setToolTipText(Messages.AbstractSummaryWizardPage_rulesPerFileTableViewerToolTipText);

		TableColumnLayout rulesInFileTableLayout = new TableColumnLayout();
		rulesInFileComposite.setLayout(rulesInFileTableLayout);
		rulesInFileTableLayout.setColumnData(column, new ColumnWeightData(100));
		
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
				ChangedFilesModel model1 = (ChangedFilesModel) e1;
				ChangedFilesModel model2 = (ChangedFilesModel) e2;
				return model1.getName()
					.compareTo(model2.getName());
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
