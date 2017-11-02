package eu.jsparrow.ui.preview;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import eu.jsparrow.ui.util.MapContentProvider;

@SuppressWarnings({"restriction", "nls"})
public class SummaryWizardPage extends WizardPage {

	private Composite rootcomposite;
	private ExpandBar expandBar;

	/**
	 * Create the wizard.
	 */
	public SummaryWizardPage() {
		super("wizardPage");
		setTitle("Run Summary");

	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		rootcomposite = new Composite(parent, SWT.NONE);
		setControl(rootcomposite);
		rootcomposite.setLayout(new GridLayout(1, false));
		addHeader();
		Label label = new Label(rootcomposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addExpandSection(rootcomposite);
	}

	private void addHeader() {
		Composite composite = new Composite(rootcomposite, SWT.NONE);
		composite.setLayout(new GridLayout(3, true));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label labelExecutionTime = new Label(composite, SWT.NONE);
		labelExecutionTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		labelExecutionTime.setText("Run Duration: 30 Seconds");

		Label labelIssuesFixed = new Label(composite, SWT.NONE);
		labelIssuesFixed.setText("230 Issues fixed");
		labelIssuesFixed.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

		Label labelHoursSaved = new Label(composite, SWT.NONE);
		labelHoursSaved.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true));
		labelHoursSaved.setText("1003 Hours Saved");
	}

	private void addExpandSection(Composite container) {

		expandBar = new ExpandBar(container, SWT.V_SCROLL);
		expandBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		expandBar.setSpacing(8);

		addRulesSection(expandBar);
		addFilesSection(expandBar);
	}

	private void addFilesSection(ExpandBar expandBar) {
		ExpandItem technicalDebtExpandItem = new ExpandItem(expandBar, SWT.NONE, 0);
		technicalDebtExpandItem.setText("File Summary");
		technicalDebtExpandItem.setExpanded(true);

		Composite composite = new Composite(expandBar, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);

		addFilePreview(composite);
		technicalDebtExpandItem.setControl(composite);
		technicalDebtExpandItem.setHeight(technicalDebtExpandItem.getControl()
			.computeSize(SWT.DEFAULT, composite.getDisplay()
				.getActiveShell()
				.getSize().y / 2).y);
	}

	private void addFilePreview(Composite composite) {
		SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		TableViewer viewer = new TableViewer(sashForm, SWT.SINGLE);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(Arrays.asList("Somefile.java", "CoolFile.java", "TableViewThing.java", "MegaList.java",
				"MuchWow.java"));

		Composite previewContainer = new Composite(sashForm, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		previewContainer.setLayout(layout);
		previewContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		IChangePreviewViewer currentPreviewViewer = new TextEditChangePreviewViewer();
		currentPreviewViewer.createControl(previewContainer);
		currentPreviewViewer.getControl()
			.setLayoutData(new GridData(GridData.FILL_BOTH));

		sashForm.setWeights(new int[] { 1, 1 });
	}

	private void addRulesSection(ExpandBar expandBar) {
		ExpandItem filesExpandItem = new ExpandItem(expandBar, SWT.NONE);
		filesExpandItem.setExpanded(true);
		filesExpandItem.setText("Rule Summary");

		Composite composite = new Composite(expandBar, SWT.NONE);
		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
		layout.marginWidth = layout.marginHeight = 10;
		composite.setLayout(layout);
		filesExpandItem.setControl(composite);

		Composite tableComposite = new Composite(composite, SWT.NONE);

		TableViewer viewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(MapContentProvider.getInstance());

		TableViewerColumn colRuleName = new TableViewerColumn(viewer, SWT.NONE);
		colRuleName.getColumn()
			.setText("Rule");
		colRuleName.getColumn()
			.setResizable(false);
		colRuleName.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				Entry<String, Integer> p = (Entry<String, Integer>) element;
				return p.getKey();
			}
		});

		TableViewerColumn colTimes = new TableViewerColumn(viewer, SWT.NONE);
		colTimes.getColumn()
			.setResizable(false);
		colTimes.getColumn()
			.setText("Times Applied");
		colTimes.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				Entry<String, Integer> p = (Entry<String, Integer>) element;
				return p.getValue()
					.toString();
			}
		});

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableComposite.setLayout(tableLayout);
		tableLayout.setColumnData(colRuleName.getColumn(), new ColumnWeightData(80));
		tableLayout.setColumnData(colTimes.getColumn(), new ColumnWeightData(20));

		// Testdata for checking height scaling
		Map<String, Integer> map = new HashMap<>();
		map.put("Replace For-Loop with Stream::anyMatch", 15);
		map.put("Remove Inherited Interfaces from Class Declaration", 9);
		map.put("Replace For-Loop with Enhanced-For-Loop", 120);
		map.put("Remove toString() on String", 4);
		map.put("Replace Assignment with Compound Operator", 22);
		map.put("Remove Explicit Type Argument1", 42);
		map.put("Remove Explicit Type Argument2", 42);
		map.put("Remove Explicit Type Argument3", 42);
		map.put("Remove Explicit Type Argument4", 42);
		map.put("Remove Explicit Type Argument5", 42);
		map.put("Remove Explicit Type Argumen67t", 42);
		map.put("Remove Explicit Tyape Aedrgument", 42);
		map.put("Remove Explicit Typdse Argument", 42);
		map.put("Remove Explicit Tyaape Argument", 42);
		map.put("Remove Explicit Typae Argument", 42);
		map.put("Remoade Explicit Type Argument", 42);
		map.put("Remove Explicitasd Type Argument", 42);
		map.put("Remove Exasdfplicit Type Argument", 42);
		map.put("Remove Explicit Tasdfype Argument", 42);
		map.put("Remove Explicit Typeasd Argument", 42);
		map.put("Remove Expasddflicit Type Argument", 42);
		map.put("Remove Exasdpasdflicit Type Argument", 42);
		map.put("Remove Expaasdfsdflicit Type Argument", 42);
		map.put("Remove Expaasdsdflicit Type Argument", 42);
		map.put("Remove Expasdflicit Tyasdpe Argument", 42);
		map.put("Remove Expaaaasdflicit Type Argument", 42);
		map.put("Remove Expssssasdflicit Type Argument", 42);
		map.put("Remove Expadddddsdflicit Type Argument", 42);
		map.put("Remove Expafffffsdflicit Type Argument", 42);
		map.put("Remove Expagggggsdflicit Type Argument", 42);
		map.put("Remove Expahhhhhhsdflicit Type Argument", 42);
		map.put("Remove Expasccccccdflicit Type Argument", 42);
		map.put("Remove Expasdvvvvvvvflicit Type Argument", 42);
		map.put("Remove Expasdflbbbbbbbicit Type Argument", 42);
		map.put("Remove Expasdfbnnnlicit Type Argument", 42);
		viewer.setInput(map);

		//Set the size to at most half of the display
		int halfDisplayHeight = composite.getDisplay()
			.getActiveShell()
			.getSize().y / 2;
		int height = Math.min(filesExpandItem.getControl()
			.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, halfDisplayHeight);
		filesExpandItem.setHeight(height);
	}

}
