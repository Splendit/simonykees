package eu.jsparrow.ui.preview;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.dialog.CompareInput;
import eu.jsparrow.ui.preview.model.SummaryWizardPageModel;
import eu.jsparrow.ui.preview.model.SummaryWizardPageModel.ChangedFilesModel;
import eu.jsparrow.ui.util.LicenseUtil;

@SuppressWarnings({ "restriction", "nls" })
public class SummaryWizardPage extends WizardPage {
	
	private static final Logger logger = LoggerFactory.getLogger(RefactoringSummaryWizardPage.class);

	private Composite rootComposite;

	private CLabel labelExecutionTime;

	private CLabel labelIssuesFixed;

	private CLabel labelHoursSaved;
	
	private TableViewer fileTableViewer;

	private TableViewer ruleTableViewer;

	private Composite compareInputContainer;

	private Control compareInputControl;

	private SummaryWizardPageModel summaryWizardPageModel;

	/**
	 * Create the wizard.
	 */
	public SummaryWizardPage(RefactoringPipeline refactoringPipeline) {
		super("wizardPage");
		setTitle("Run Summary");
		this.summaryWizardPageModel = new SummaryWizardPageModel(refactoringPipeline);
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		rootComposite = new Composite(parent, SWT.NONE);
		setControl(rootComposite);
		rootComposite.setLayout(new GridLayout(1, false));
		addHeader();
		addRulesSection();
		addFilesSection();
		initializeDataBindings();
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
		if(visible) {
			createCompareInputControl();
			summaryWizardPageModel.setIsFreeLicense(LicenseUtil.getInstance()
				.isFree());
		}
		super.setVisible(visible);
	}

	private void addHeader() {
		Composite composite = new Composite(rootComposite, SWT.NONE);
		GridLayout layout = new GridLayout(3, true);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		labelExecutionTime = new CLabel(composite, SWT.NONE);
		labelExecutionTime.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		labelExecutionTime.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/fa-hourglass-half.png"));

		labelIssuesFixed = new CLabel(composite, SWT.NONE);
		labelIssuesFixed.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		labelIssuesFixed.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/fa-bolt.png"));

		labelHoursSaved = new CLabel(composite, SWT.NONE);
		labelHoursSaved.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		labelHoursSaved.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/fa-clock.png"));
		
		Label label = new Label(rootComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}


	private void addFilesSection() {
		Group filesGroup = new Group(rootComposite, SWT.SHADOW_ETCHED_IN);
		filesGroup.setLayout(new GridLayout(1, false));
		filesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		filesGroup.setText("Files");
		SashForm sashForm = new SashForm(filesGroup, SWT.VERTICAL);
		sashForm.setLayout(new GridLayout());
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm.setBackground(sashForm.getDisplay().getSystemColor( SWT.COLOR_GRAY));

		fileTableViewer = new TableViewer(sashForm, SWT.SINGLE);

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

	private void addRulesSection() {
		Group tableComposite = new Group(rootComposite, SWT.SHADOW_ETCHED_IN);
		tableComposite.setText("Rules");
		tableComposite.setLayout(new GridLayout(1, false));
		tableComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ruleTableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = ruleTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableViewerColumn colRuleName = new TableViewerColumn(ruleTableViewer, SWT.NONE);
		colRuleName.getColumn()
			.setText("Rule");
		colRuleName.getColumn()
			.setResizable(false);

		TableViewerColumn colTimes = new TableViewerColumn(ruleTableViewer, SWT.NONE);
		colTimes.getColumn()
			.setResizable(false);
		colTimes.getColumn()
			.setText("Times Applied");
		
        TableColumnLayout tableLayout = new TableColumnLayout(); 
        tableComposite.setLayout(tableLayout); 
        tableLayout.setColumnData(colRuleName.getColumn(), new ColumnWeightData(80)); 
        tableLayout.setColumnData(colTimes.getColumn(), new ColumnWeightData(20)); 
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void initializeDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
	
		bindHeader(bindingContext);

		ViewerSupport.bind(ruleTableViewer, summaryWizardPageModel.getRuleTimes(),
				BeanProperties.values("name", "times"));
		ViewerSupport.bind(fileTableViewer, summaryWizardPageModel.getChangedFiles(), BeanProperties.values("name"));

		IViewerObservableValue selectedFile = ViewerProperties.singleSelection()
			.observe(fileTableViewer);

		selectedFile.addValueChangeListener(e -> {
			ChangedFilesModel selectedItem = (ChangedFilesModel) e.getObservableValue()
				.getValue();
			updateCompareInputControl("Test", selectedItem.getSourceLeft(), selectedItem.getSourceRight());
		});
		
		IObservableValue isFreeLicenseObservalbeValue = BeanProperties.value("isFreeLicense").observe(summaryWizardPageModel);
		isFreeLicenseObservalbeValue.addValueChangeListener(e -> {
			Boolean isFreeLicense = (Boolean) e.getObservableValue().getValue();
			StatusInfo statusInfo = new StatusInfo();
			if(isFreeLicense) {
				statusInfo.setWarning(Messages.RefactoringSummaryWizardPage_warn_disableFinishWhenFree);
			}
			StatusUtil.applyToStatusLine(this, statusInfo);
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void bindHeader(DataBindingContext bindingContext) {
		IObservableValue observeTextLabelExecutionTimeObserveWidget = WidgetProperties.text()
			.observe(labelExecutionTime);
		IObservableValue executionTimeSummaryWizardPageModelObserveValue = BeanProperties.value("executionTime")
			.observe(summaryWizardPageModel);
		bindingContext.bindValue(observeTextLabelExecutionTimeObserveWidget,
				executionTimeSummaryWizardPageModelObserveValue, null, null);
		//
		IObservableValue observeTextLabelIssuesFixedObserveWidget = WidgetProperties.text()
			.observe(labelIssuesFixed);
		IObservableValue issuesFixedSummaryWizardPageModelObserveValue = BeanProperties.value("issuesFixed")
			.observe(summaryWizardPageModel);
		bindingContext.bindValue(observeTextLabelIssuesFixedObserveWidget,
				issuesFixedSummaryWizardPageModelObserveValue, null, null);
		//
		IObservableValue observeTextLabelHoursSavedObserveWidget = WidgetProperties.text()
			.observe(labelHoursSaved);
		IObservableValue hoursSavedSummaryWizardPageModelObserveValue = BeanProperties.value("hoursSaved")
			.observe(summaryWizardPageModel);
		bindingContext.bindValue(observeTextLabelHoursSavedObserveWidget, hoursSavedSummaryWizardPageModelObserveValue,
				null, null);
	}

	private void createCompareInputControl() {
		disposeCompareInputControl();
		Display.getDefault()
			.syncExec(() -> {
				CompareInput compareInput = new CompareInput("", "", "");
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
		compareInputControl = compareInput.createContents(compareInputContainer);
		compareInputControl.setSize(compareInputControl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		compareInputControl.setLayoutData(new GridData(GridData.FILL_BOTH));
		compareInputContainer.layout();
	}
}
