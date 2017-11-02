package eu.jsparrow.ui.preview;

import java.util.Arrays;

import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
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
	private Composite rootcomposite;

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
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblNewLabel.setText("Run Duration: 30 Seconds");
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setText("230 Issues fixed");
		lblNewLabel_1.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true));
		lblNewLabel_2.setText("1003 Hours Saved");
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
		
		Composite composite = new Composite(expandBar, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		
		addFilePreview(composite);
		technicalDebtExpandItem.setControl(composite);
		technicalDebtExpandItem.setHeight(technicalDebtExpandItem.getControl().computeSize(SWT.DEFAULT, composite.getDisplay().getActiveShell().getSize().y / 2).y);
	}

	private void addFilePreview(Composite composite) {
		SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		TableViewer viewer = new TableViewer(sashForm, SWT.SINGLE);
		table = viewer.getTable();
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(Arrays.asList("test","test2","test3"));

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
		
		sashForm.setWeights(new int[] {1,1});
	}

	private void addRulesSection(ExpandBar expandBar) {
		ExpandItem filesExpandItem = new ExpandItem(expandBar, SWT.NONE);
		filesExpandItem.setExpanded(true);
		filesExpandItem.setText("Rule Summary");
		Composite composite = new Composite(expandBar, SWT.NONE);
		filesExpandItem.setControl(composite);
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);
		
		filesExpandItem.setHeight(filesExpandItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

	}
}
