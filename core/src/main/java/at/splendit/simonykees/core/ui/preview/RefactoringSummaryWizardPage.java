package at.splendit.simonykees.core.ui.preview;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
import at.splendit.simonykees.core.util.RefactoringUtil;

@SuppressWarnings("restriction")
public class RefactoringSummaryWizardPage extends WizardPage {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringSummaryWizardPage.class);

	private RefactoringPipeline refactoringPipeline;
	Map<ICompilationUnit, DocumentChange> changes = new HashMap<>();

	private ICompilationUnit currentCompilationUnit;
	private IChangePreviewViewer currentPreviewViewer;
	private CheckboxTableViewer viewer;

	public RefactoringSummaryWizardPage(RefactoringPipeline refactoringPipeline) {
		super("Summary");
		setTitle("Summary");
		setDescription("All changes made by all rules");

		this.refactoringPipeline = refactoringPipeline;
		setChanges();
		this.currentCompilationUnit = changes.keySet().stream().findFirst().orElse(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.
	 * widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();

		// margin from TextEditChangePreviewViewer to Composite
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// without setting the layout, nothing displays
		container.setLayout(layout);

		setControl(container);

		SashForm sashForm = new SashForm(container, SWT.VERTICAL);

		createFileView(sashForm);
		createPreviewViewer(sashForm);

		/*
		 * sets height relation between children to be 1:3 when it has two
		 * children
		 */
		sashForm.setWeights(new int[] { 1, 3 });

	}

	private void createFileView(Composite parent) {
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI);

		/*
		 * label provider that sets the text displayed in CompilationUnits table
		 * to show the name of the CompilationUnit
		 */
		viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ICompilationUnit compUnit = (ICompilationUnit) element;
				return String.format("%s - %s", getClassNameString(compUnit), getPathString(compUnit)); //$NON-NLS-1$
			}
		});

		viewer.addSelectionChangedListener(createSelectionChangedListener());

		populateFileView();

	}

	public Map<ICompilationUnit, DocumentChange> setChanges() {
		refactoringPipeline.getRules().forEach(rule -> {
			Map<ICompilationUnit, DocumentChange> changesForRule = refactoringPipeline.getChangesForRule(rule);
			if (!changesForRule.isEmpty()) {
				for (ICompilationUnit unit : changesForRule.keySet()) {
					if (changes.containsKey(unit)) {
						// TODO handle when changes are overlapping
						changes.get(unit).addEdit(changesForRule.get(unit).getEdit());
					} else {
						changes.put(unit, changesForRule.get(unit));
					}
				}
			}
		});
		return changes;
	}

	protected void populateFileView() {
		// if redraw, remove all items before adding
		if (viewer.getTable().getItemCount() > 0) {
			viewer.getTable().removeAll();
		}
		// adding all elements in table and checking appropriately
		changes.keySet().stream().forEach(entry -> {
			viewer.add(entry);
		});
	}

	/**
	 * Returns the class name of an {@link ICompilationUnit}, including ".java"
	 * 
	 * @param compilationUnit
	 * @return
	 */
	private String getClassNameString(ICompilationUnit compilationUnit) {
		return compilationUnit.getElementName();
	}

	/**
	 * Returns the path of an {@link ICompilationUnit} without leading slash
	 * (the same as in the Externalize Strings refactoring view).
	 * 
	 * @param compilationUnit
	 * @return
	 */
	private String getPathString(ICompilationUnit compilationUnit) {
		String temp = compilationUnit.getParent().getPath().toString();
		return temp.startsWith("/") ? temp.substring(1) : temp; //$NON-NLS-1$
	}

	private void createPreviewViewer(Composite parent) {

		// GridData works with GridLayout
		GridData gridData = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gridData);

		currentPreviewViewer = new TextEditChangePreviewViewer();
		currentPreviewViewer.createControl(parent);

		populatePreviewViewer();

	}

	private ISelectionChangedListener createSelectionChangedListener() {
		return new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();

				if (sel.size() == 1) {
					ICompilationUnit newSelection = (ICompilationUnit) sel.getFirstElement();
					if (!newSelection.equals(currentCompilationUnit)) {
						currentCompilationUnit = newSelection;
						populatePreviewViewer();
					}
				}
			}
		};
	}

	private void populatePreviewViewer() {
		currentPreviewViewer.setInput(TextEditChangePreviewViewer.createInput(getCurrentDocumentChange()));
		((CompareViewerSwitchingPane) currentPreviewViewer.getControl())
				.setTitleArgument(currentCompilationUnit.getElementName());
	}

	private DocumentChange getCurrentDocumentChange() {
		if (null == changes.get(currentCompilationUnit)) {
			DocumentChange documentChange = null;
			try {
				/*
				 * When compilation unit is unselected for rule that is shown,
				 * change preview viewer should show no change. For that
				 * generate document change is called with empty edit to create
				 * document change with text type java but with no changes.
				 */
				TextEdit edit = new MultiTextEdit();
				return RefactoringUtil.generateDocumentChange(currentCompilationUnit.getElementName(),
						new Document(currentCompilationUnit.getSource()), edit);
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), e);
			}
			return documentChange;
		} else {
			return changes.get(currentCompilationUnit);
		}
	}

	/**
	 * Used to populate IChangePreviewViewer currentPreviewViewer and
	 * CheckboxTableViewer viewer every time page gets displayed. Sets the
	 * selection in file view part to match file whose changes are displayed in
	 * changes view.
	 */
	public void populateViews() {
		populateFileView();
		populatePreviewViewer();
		viewer.setSelection(new StructuredSelection(currentCompilationUnit));
	}

	/**
	 * Open help dialog
	 */
	@Override
	public void performHelp() {
		SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
	}

}
