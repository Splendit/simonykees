package eu.jsparrow.ui.preview;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsData;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.statistic.entity.JsparrowMetric;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.model.DurationFormatUtil;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.model.summary.ChangedFilesModel;
import eu.jsparrow.ui.preview.model.summary.RefactoringSummaryWizardPageModel;
import eu.jsparrow.ui.preview.model.summary.RulesPerFileModel;
import eu.jsparrow.ui.util.ResourceHelper;

@SuppressWarnings({ "restriction" })
public abstract class AbstractSummaryWizardPage extends WizardPage {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSummaryWizardPage.class);

	private Composite rootComposite;

	private CLabel labelExecutionTime;
	private CLabel labelIssuesFixed;
	private CLabel labelHoursSaved;

	private TableViewer fileTableViewer;
	private TableViewer ruleTableViewer;
	private TableViewer rulesPerFileTableViewer;

	private RefactoringSummaryWizardPageModel summaryWizardPageModel;

	private int displayHeight;

	private boolean enabledFinishButton;
	private StandaloneStatisticsMetadata statisticsMetadata;
	private long endTime;

	protected AbstractSummaryWizardPage(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel, boolean enabledFinishButton,
			StandaloneStatisticsMetadata statisticsMetadata) {
		this(refactoringPipeline, wizardModel, enabledFinishButton);
		this.statisticsMetadata = statisticsMetadata;
		this.endTime = Instant.now()
			.getEpochSecond();
	}

	protected AbstractSummaryWizardPage(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel, boolean enabledFinishButton) {
		super("wizardPage"); //$NON-NLS-1$
		ContextInjectionFactory.inject(this, Activator.getEclipseContext());

		setTitle(Messages.SummaryWizardPage_RunSummary);
		this.summaryWizardPageModel = new RefactoringSummaryWizardPageModel(refactoringPipeline, wizardModel);
		this.enabledFinishButton = enabledFinishButton;
		displayHeight = Display.getCurrent()
			.getPrimaryMonitor()
			.getBounds().height;
	}

	/**
	 * Create contents of the wizard.
	 *
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		rootComposite = new Composite(parent, SWT.NONE);
		setControl(rootComposite);
		rootComposite.setLayout(new GridLayout(1, false));
	}

	@Override
	public void performHelp() {
		SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			setStatusInfo();
			summaryWizardPageModel.updateData();
			saveStatisticsData();

			// We must wait to set selection until control is visible
			setInitialFileSelection();

		}
		super.setVisible(visible);
	}

	private void saveStatisticsData() {
		/*
		 * statistics data is only saved, when the java system property
		 * eu.jsparrow.statistics.save.path is set to a path. if it's empty or
		 * null, nothing will be saved.
		 */
		String path = System.getProperty("eu.jsparrow.statistics.save.path"); //$NON-NLS-1$
		if (path == null || path.isEmpty()) {
			return;
		}

		RefactoringPipeline refactoringPipeline = summaryWizardPageModel.getRefactoringPipeline();

		StandaloneStatisticsData statisticsData = new StandaloneStatisticsData(refactoringPipeline.getFileCount(),
				statisticsMetadata.getRepoName(), statisticsMetadata, refactoringPipeline);

		statisticsData.setMetricData();

		statisticsData.setEndTime(endTime);
		Optional<JsparrowMetric> metric = statisticsData.getMetricData();
		metric.ifPresent(m -> {
			try {
				ObjectMapper om = new ObjectMapper();
				final Path filePath = Paths.get(path, statisticsMetadata.getRepoName(), Instant.now()
					.getEpochSecond() + ".json"); //$NON-NLS-1$

				File file = filePath.toFile();
				file.getParentFile()
					.mkdirs();
				om.writeValue(filePath.toFile(), m);
			} catch (IOException e) {
				logger.debug(e.getMessage(), e);
			}
		});
	}

	protected void addHeader() {
		Composite composite = new Composite(rootComposite, SWT.NONE);
		GridLayout layout = new GridLayout(3, true);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		labelExecutionTime = new CLabel(composite, SWT.NONE);
		labelExecutionTime.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		labelExecutionTime.setImage(ResourceHelper.createImage("icons/fa-hourglass-half.png")); //$NON-NLS-1$

		labelIssuesFixed = new CLabel(composite, SWT.NONE);
		labelIssuesFixed.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		labelIssuesFixed.setImage(ResourceHelper.createImage("icons/fa-bolt.png")); //$NON-NLS-1$

		labelHoursSaved = new CLabel(composite, SWT.NONE);
		labelHoursSaved.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		labelHoursSaved.setImage(ResourceHelper.createImage("icons/fa-clock.png")); //$NON-NLS-1$

		Label label = new Label(rootComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void addRulesSection() {
		Group tableComposite = new Group(rootComposite, SWT.SHADOW_ETCHED_IN);
		tableComposite.setText(Messages.SummaryWizardPage_Rules);
		tableComposite.setLayout(new GridLayout(1, false));
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.heightHint = displayHeight / 3;
		tableComposite.setLayoutData(layoutData);
		ruleTableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		Table table = ruleTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		SummaryPageRuleTableViewerComparator comparator = new SummaryPageRuleTableViewerComparator();
		ruleTableViewer.setComparator(comparator);

		TableViewerColumn colRuleName = createTableViewerColumn(Messages.SummaryWizardPage_Rule, 0,
				comparator);
		TableViewerColumn colTimes = createTableViewerColumn(Messages.SummaryWizardPage_TimesApplied, 1,
				comparator);
		TableViewerColumn colTimeSaved = createTableViewerColumn(Messages.SummaryWizardPage_TimeSaved, 2,
				comparator);

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableComposite.setLayout(tableLayout);
		tableLayout.setColumnData(colRuleName.getColumn(), new ColumnWeightData(60));
		tableLayout.setColumnData(colTimes.getColumn(), new ColumnWeightData(20));
		tableLayout.setColumnData(colTimeSaved.getColumn(), new ColumnWeightData(20));

	}

	private TableViewerColumn createTableViewerColumn(String title, int colNumber,
			SummaryPageRuleTableViewerComparator comparator) {
		TableViewerColumn tableViewerColumn = new TableViewerColumn(ruleTableViewer, SWT.NONE);
		TableColumn column = tableViewerColumn.getColumn();

		column.setResizable(false);
		column.setText(title);
		column.addSelectionListener(
				getSelectionAdapterForRulesTableViewer(column, colNumber, comparator));

		return tableViewerColumn;
	}

	private SelectionAdapter getSelectionAdapterForRulesTableViewer(final TableColumn column,
			final int index, SummaryPageRuleTableViewerComparator comparator) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				ruleTableViewer.getTable()
					.setSortDirection(dir);
				ruleTableViewer.getTable()
					.setSortColumn(column);
				ruleTableViewer.refresh();
			}
		};
	}

	protected void addFilesSection() {
		Group filesGroup = new Group(rootComposite, SWT.SHADOW_ETCHED_IN);
		filesGroup.setLayout(new GridLayout(1, true));
		filesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesGroup.setText(Messages.SummaryWizardPage_Files);
		SashForm sashForm = new SashForm(filesGroup, SWT.HORIZONTAL);
		sashForm.setLayout(new GridLayout());
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.setBackground(sashForm.getDisplay()
			.getSystemColor(SWT.COLOR_GRAY));

		fileTableViewer = new TableViewer(sashForm, SWT.SINGLE);

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

		rulesPerFileTableViewer = new TableViewer(sashForm, SWT.SINGLE);
		rulesPerFileTableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				RulesPerFileModel model1 = (RulesPerFileModel) e1;
				RulesPerFileModel model2 = (RulesPerFileModel) e2;
				return model1.getName()
					.compareTo(model2.getName());
			}
		});		
	}

	protected void initializeDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();

		initializeHeaderDataBindings(bindingContext);

		ViewerSupport.bind(fileTableViewer, summaryWizardPageModel.getChangedFiles(), BeanProperties.values("name")); //$NON-NLS-1$

		IViewerObservableValue<Object> selectedFile = ViewerProperties.singleSelection()
			.observe(fileTableViewer);
		ViewerSupport.bind(rulesPerFileTableViewer, summaryWizardPageModel.getRulesPerFile(), BeanProperties.values("name")); //$NON-NLS-1$
		

		selectedFile.addValueChangeListener(e -> {
			ChangedFilesModel selectedItem = (ChangedFilesModel) e.getObservableValue()
				.getValue();

			if (selectedItem != null) {
				summaryWizardPageModel.updateRulesPerFile(selectedItem.getRules());
			}
		});
	}

	private void setStatusInfo() {
		StatusInfo statusInfo = new StatusInfo();
		if (!enabledFinishButton) {
			statusInfo.setWarning(Messages.RefactoringSummaryWizardPage_warn_disableFinishWhenFree);
		}
		StatusUtil.applyToStatusLine(this, statusInfo);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initializeHeaderDataBindings(DataBindingContext bindingContext) {
		IConverter convertRunDuration = IConverter.create(Long.class, String.class,
				x -> DurationFormatUtil.formatRunDuration((Long) x));
		IObservableValue observeTextLabelExecutionTimeObserveWidget = WidgetProperties.text()
			.observe(labelExecutionTime);
		IObservableValue executionTimeSummaryWizardPageModelObserveValue = BeanProperties.value("runDuration") //$NON-NLS-1$
			.observe(summaryWizardPageModel);
		bindingContext.bindValue(observeTextLabelExecutionTimeObserveWidget,
				executionTimeSummaryWizardPageModelObserveValue, null, UpdateValueStrategy.create(convertRunDuration));

		IConverter convertIssuesFixed = IConverter.create(Integer.class, String.class,
				x -> (String.format(Messages.SummaryWizardPageModel_IssuesFixed, (Integer) x)));
		IObservableValue observeTextLabelIssuesFixedObserveWidget = WidgetProperties.text()
			.observe(labelIssuesFixed);
		IObservableValue issuesFixedSummaryWizardPageModelObserveValue = BeanProperties.value("issuesFixed") //$NON-NLS-1$
			.observe(summaryWizardPageModel);
		bindingContext.bindValue(observeTextLabelIssuesFixedObserveWidget,
				issuesFixedSummaryWizardPageModelObserveValue, null, UpdateValueStrategy.create(convertIssuesFixed));

		IConverter convertTimeSaved = IConverter.create(Duration.class, String.class, x -> String
			.format(Messages.DurationFormatUtil_TimeSaved, DurationFormatUtil.formatTimeSaved((Duration) x)));
		IObservableValue observeTextLabelHoursSavedObserveWidget = WidgetProperties.text()
			.observe(labelHoursSaved);
		IObservableValue hoursSavedSummaryWizardPageModelObserveValue = BeanProperties.value("timeSaved") //$NON-NLS-1$
			.observe(summaryWizardPageModel);
		bindingContext.bindValue(observeTextLabelHoursSavedObserveWidget, hoursSavedSummaryWizardPageModelObserveValue,
				null, UpdateValueStrategy.create(convertTimeSaved));
	}

	private void setInitialFileSelection() {
		Object item = fileTableViewer.getElementAt(0);
		if (item != null) {
			fileTableViewer.setSelection(new StructuredSelection(item));
		}
	}

	protected void initializeRuleTableDataBindings() {
		
		ViewerSupport.bind(ruleTableViewer, summaryWizardPageModel.getRuleTimes(),
				BeanProperties.values("name", "times", "timeSaved")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
