package eu.jsparrow.ui.preview;

import java.util.Arrays;

import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import eu.jsparrow.core.refactorer.RefactoringState;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.RowLayout;

@SuppressWarnings("restriction")
public class SummaryWizardPage extends WizardPage {

	private Table table;

	/**
	 * Create the wizard.
	 */
	public SummaryWizardPage() {
		super("wizardPage");
		setTitle("Wizard Page title");
		setDescription("Wizard Page description");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		setControl(container);
		container.setLayout(new GridLayout(1, false));
		addExpandSection(container);
	}

	private void addExpandSection(Composite container) {
		ExpandBar expandBar = new ExpandBar(container, SWT.V_SCROLL);
		expandBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		expandBar.setSpacing(8);

		addRulesSection(expandBar);
		addFileSection(expandBar);

	}

	private void addRulesSection(ExpandBar expandBar) {
		Display display = expandBar.getDisplay();

		ExpandItem technicalDebtExpandItem = new ExpandItem(expandBar, SWT.NONE, 0);
		technicalDebtExpandItem.setText("Technical Debt");
		technicalDebtExpandItem.setExpanded(true);
		
		Composite composite = new Composite(expandBar, SWT.NONE);
		technicalDebtExpandItem.setControl(composite);
		technicalDebtExpandItem.setHeight(technicalDebtExpandItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		composite.setLayout(new GridLayout(1, false));
		
		SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
		sashForm.setLocation(0, 0);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		TableViewer viewer = new TableViewer(sashForm, SWT.BORDER | SWT.FULL_SELECTION);
		table = viewer.getTable();
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(Arrays.asList("test","test2"));
		sashForm.setWeights(new int[] {1});
	}

	private void addFileSection(ExpandBar expandBar) {
		ExpandItem filesExpandItem = new ExpandItem(expandBar, SWT.NONE);
		Display display = expandBar.getDisplay();
		filesExpandItem.setExpanded(true);
		filesExpandItem.setText("Files");
		Composite composite = new Composite(expandBar, SWT.NONE);
		filesExpandItem.setControl(composite);
		
		GridLayout layout = new GridLayout(2, false);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		Label label = new Label(composite, SWT.NONE);
		label.setImage(display.getSystemImage(SWT.ICON_ERROR));
		label = new Label(composite, SWT.NONE);
		label.setText("SWT.ICON_ERROR");
		label = new Label(composite, SWT.NONE);
		label.setImage(display.getSystemImage(SWT.ICON_INFORMATION));
		label = new Label(composite, SWT.NONE);
		label.setText("SWT.ICON_INFORMATION");
		label = new Label(composite, SWT.NONE);
		label.setImage(display.getSystemImage(SWT.ICON_WARNING));
		label = new Label(composite, SWT.NONE);
		label.setText("SWT.ICON_WARNING");
		label = new Label(composite, SWT.NONE);
		label.setImage(display.getSystemImage(SWT.ICON_QUESTION));
		label = new Label(composite, SWT.NONE);
		label.setText("SWT.ICON_QUESTION");
		filesExpandItem.setHeight(filesExpandItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

	}

	private void createPreviewViewer(Composite parent) {

		CompareUIPlugin.getDefault()
			.getPreferenceStore()
			.setValue(ComparePreferencePage.OPEN_STRUCTURE_COMPARE, Boolean.FALSE);

	}
}
