package eu.jsparrow.ui.preview;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsData;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.statistic.entity.JsparrowMetric;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.dialog.CompareInput;
import eu.jsparrow.ui.preview.model.DurationFormatUtil;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.model.summary.ChangedFilesModel;
import eu.jsparrow.ui.preview.model.summary.RefactoringSummaryWizardPageModel;
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

	private Composite compareInputContainer;

	private Control compareInputControl;

	private RefactoringSummaryWizardPageModel summaryWizardPageModel;

	private int displayHeight;

	private boolean enabledFinishButton;

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

	public void disposeCompareInputControl() {
		if (compareInputControl != null) {
			compareInputControl.dispose();
		}
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

			createCompareInputControl();
			// We must wait to set selection until control is visible
			setInitialFileSelection();

		}
		super.setVisible(visible);
	}

	private void saveStatisticsData() {
		RefactoringPipeline refactoringPipeline = summaryWizardPageModel.getRefactoringPipeline();
		
		ObjectMapper om = new ObjectMapper();
		final Path filePath = Paths.get(System.getProperty("user.home"), "Desktop", "statistics", refactoringPipeline.getProjectName(), Instant.now().getEpochSecond() + ".json");
		
		StandaloneStatisticsData statisticsData = new StandaloneStatisticsData(refactoringPipeline.getFileCount(),
				refactoringPipeline.getProjectName(), refactoringPipeline.getStatisticsMetadata(), refactoringPipeline);
		
		statisticsData.setMetricData();
		statisticsData.setEndTime(refactoringPipeline.getFinishTime().getEpochSecond());
		Optional<JsparrowMetric> metric = statisticsData.getMetricData();
		metric.ifPresent(m -> {
			try {
				File file = filePath.toFile();
				file.getParentFile().mkdirs();
				om.writeValue(filePath.toFile(), m);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		layoutData.heightHint = displayHeight / 4;
		tableComposite.setLayoutData(layoutData);
		ruleTableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		Table table = ruleTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableViewerColumn colRuleName = new TableViewerColumn(ruleTableViewer, SWT.NONE);
		colRuleName.getColumn()
			.setText(Messages.SummaryWizardPage_Rule);
		colRuleName.getColumn()
			.setResizable(false);

		TableViewerColumn colTimes = new TableViewerColumn(ruleTableViewer, SWT.NONE);
		colTimes.getColumn()
			.setResizable(false);
		colTimes.getColumn()
			.setText(Messages.SummaryWizardPage_TimesApplied);

		TableViewerColumn colTimeSaved = new TableViewerColumn(ruleTableViewer, SWT.NONE);
		colTimeSaved.getColumn()
			.setResizable(false);
		colTimeSaved.getColumn()
			.setText(Messages.SummaryWizardPage_TimeSaved);

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableComposite.setLayout(tableLayout);
		tableLayout.setColumnData(colRuleName.getColumn(), new ColumnWeightData(60));
		tableLayout.setColumnData(colTimes.getColumn(), new ColumnWeightData(20));
		tableLayout.setColumnData(colTimeSaved.getColumn(), new ColumnWeightData(20));

	}

	protected void addFilesSection() {
		Group filesGroup = new Group(rootComposite, SWT.SHADOW_ETCHED_IN);
		filesGroup.setLayout(new GridLayout(1, false));
		filesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		filesGroup.setText(Messages.SummaryWizardPage_Files);
		SashForm sashForm = new SashForm(filesGroup, SWT.VERTICAL);
		sashForm.setLayout(new GridLayout());
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
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

		compareInputContainer = new Composite(sashForm, SWT.FILL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		compareInputContainer.setLayout(layout);
		compareInputContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		compareInputContainer.setSize(SWT.DEFAULT, 1000);

		CompareUIPlugin.getDefault()
			.getPreferenceStore()
			.setValue(ComparePreferencePage.OPEN_STRUCTURE_COMPARE, Boolean.FALSE);

		sashForm.setWeights(new int[] { 1, 3 });
	}

	@SuppressWarnings("unchecked")
	protected void initializeDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();

		initializeHeaderDataBindings(bindingContext);

		ViewerSupport.bind(fileTableViewer, summaryWizardPageModel.getChangedFiles(), BeanProperties.values("name")); //$NON-NLS-1$

		IViewerObservableValue selectedFile = ViewerProperties.singleSelection()
			.observe(fileTableViewer);

		selectedFile.addValueChangeListener(e -> {
			ChangedFilesModel selectedItem = (ChangedFilesModel) e.getObservableValue()
				.getValue();
			if (selectedItem != null) {
				updateCompareInputControl(selectedItem.getName(), selectedItem.getSourceLeft(),
						selectedItem.getSourceRight());
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

	private void createCompareInputControl() {
		disposeCompareInputControl();
		Display.getDefault()
			.syncExec(() -> {
				CompareInput compareInput = new CompareInput("", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				updateCompareInputControl(compareInput);
			});
	}

	private void updateCompareInputControl(String name, String left, String right) {
		disposeCompareInputControl();
		Display.getDefault()
			.syncExec(() -> {
				CompareInput compareInput = new CompareInput(name, left, right);
				updateCompareInputControl(compareInput);
			});
	}

	private void updateCompareInputControl(CompareInput compareInput) {
		try {
			PlatformUI.getWorkbench()
				.getProgressService()
				.run(true, true, compareInput);
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		createControlsIfNoneExist(compareInput);
	}

	private void createControlsIfNoneExist(CompareInput compareInput) {
		// Condition fixes SIM-902
		if (compareInputContainer.getChildren().length == 0) {
			compareInputControl = compareInput.createContents(compareInputContainer);
			compareInputControl.setSize(compareInputControl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
			compareInputControl.setLayoutData(new GridData(GridData.FILL_BOTH));
			compareInputContainer.layout();
		}
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
