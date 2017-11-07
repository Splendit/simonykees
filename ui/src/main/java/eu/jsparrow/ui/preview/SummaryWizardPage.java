package eu.jsparrow.ui.preview;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.ResourceManager;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preview.SummaryWizardPageModel.ChangedFilesModel;
import eu.jsparrow.ui.preview.dialog.CompareInput;

@SuppressWarnings({ "restriction", "nls" })
public class SummaryWizardPage extends WizardPage {
	private DataBindingContext bindingContext;

	private Composite rootComposite;

	private CLabel labelExecutionTime;

	private CLabel labelIssuesFixed;

	private CLabel labelHoursSaved;

	private TableViewer fileTableViewer;

	private TableViewer ruleTableViewer;

	
	private Composite sashFormContainer; 
	
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
		Label label = new Label(rootComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addExpandSection(rootComposite);

		initDataBindings();
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

	}

	private void addExpandSection(Composite container) {

		ExpandBar expandBar = new ExpandBar(container, SWT.V_SCROLL);
		expandBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		expandBar.setSpacing(8);

		addRulesSection(expandBar);
		addFilesSection(expandBar);
	}

	private void addFilesSection(ExpandBar expandBar) {
		ExpandItem technicalDebtExpandItem = new ExpandItem(expandBar, SWT.NONE, 0);
		technicalDebtExpandItem.setText("File Summary");
		technicalDebtExpandItem.setExpanded(true);

		sashFormContainer = new Composite(expandBar, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.verticalSpacing = 10;
		sashFormContainer.setLayout(layout);

		addFilePreview(sashFormContainer);
		technicalDebtExpandItem.setControl(sashFormContainer);
		technicalDebtExpandItem.setHeight(technicalDebtExpandItem.getControl()
			.computeSize(SWT.DEFAULT, Display.getDefault()
				.getActiveShell()
				.getSize().y).y);
	}

	private void addFilePreview(Composite composite) {
		SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
		sashForm.setLayout(new GridLayout());
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		fileTableViewer = new TableViewer(sashForm, SWT.SINGLE);

		compareInputContainer = new Composite(sashForm, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		compareInputContainer.setLayout(layout);
		compareInputContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		CompareUIPlugin.getDefault()
			.getPreferenceStore()
			.setValue(ComparePreferencePage.OPEN_STRUCTURE_COMPARE, Boolean.FALSE);

		sashForm.setWeights(new int[] { 1, 3 });

	}

	private void addRulesSection(ExpandBar expandBar) {
		ExpandItem rulesExpandItem = new ExpandItem(expandBar, SWT.NONE);
		rulesExpandItem.setExpanded(true);
		rulesExpandItem.setText("Rule Summary");

		Composite composite = new Composite(expandBar, SWT.NONE);
		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
		layout.marginWidth = layout.marginHeight = 10;
		composite.setLayout(layout);
		rulesExpandItem.setControl(composite);

		ruleTableViewer = addRulesTable(composite);

		// Set the size to at most half of the display
		int thirdDisplayHeight = Display.getDefault()
			.getActiveShell()
			.getSize().y / 3;
		// TODO: Bind list height to number of items
		// int height = Math.min(filesExpandItem.getControl()
		// .computeSize(SWT.DEFAULT, SWT.DEFAULT).y, thirdDisplayHeight);
		rulesExpandItem.setHeight(thirdDisplayHeight);
	}

	private TableViewer addRulesTable(Composite composite) {
		Composite tableComposite = new Composite(composite, SWT.NONE);
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
		return ruleTableViewer;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void initDataBindings() {
		bindingContext = new DataBindingContext();
		//
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

		ViewerSupport.bind(ruleTableViewer, summaryWizardPageModel.getRuleTimes(),
				BeanProperties.values("name", "times"));

		ViewerSupport.bind(fileTableViewer, summaryWizardPageModel.getChangedFiles(), BeanProperties.values("name"));

		IViewerObservableValue selectedFile = ViewerProperties.singleSelection()
			.observe(fileTableViewer);
		IObservableValue detailValue = PojoProperties.value("name", String.class)
			.observeDetail(selectedFile);

		selectedFile.addValueChangeListener(e -> {
			ChangedFilesModel selectedItem = (ChangedFilesModel) e.getObservableValue()
				.getValue();
			updateCompareInputControl("Test", selectedItem.getSourceLeft(), selectedItem.getSourceRight());
		});
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			createCompareInputControl();
		}
		super.setVisible(visible);
	}

	private void createCompareInputControl() {
		Display.getDefault()
			.syncExec(() -> {
				CompareInput compareInput = new CompareInput("", "", ""); 
				updateCompareInputControl(compareInput);
			});
	}

	private void updateCompareInputControl(String name, String left, String right) {
		if (compareInputControl != null) {
			compareInputControl.dispose();
		}
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
			e.printStackTrace();
		}
		compareInputControl = compareInput.createContents(compareInputContainer);
		compareInputControl.setLayoutData(new GridData(GridData.FILL_BOTH));
		compareInputControl.getParent().layout();
		compareInputContainer.layout();
		sashFormContainer.layout();
	}
}
