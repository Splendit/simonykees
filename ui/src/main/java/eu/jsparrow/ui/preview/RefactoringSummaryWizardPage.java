package eu.jsparrow.ui.preview;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.internal.CompareDialog;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.dialog.CompareInput;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * Wizard page which collects all changes on all {@link ICompilationUnit}s made
 * by all {@link RefactoringRule}s to show preview of all changes
 * 
 * @author Andreja Sambolec
 * @since 2.1
 */
@SuppressWarnings("restriction")
public class RefactoringSummaryWizardPage extends WizardPage {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringSummaryWizardPage.class);

	private RefactoringPipeline refactoringPipeline;
	private Map<RefactoringState, String> initialSource = new HashMap<>();
	private Map<RefactoringState, String> finalSource = new HashMap<>();

	private RefactoringState currentRefactoringState;
	private TableViewer viewer;

	private Control compareControl;
	private Composite changeContainer;

	protected IStatus fSelectionStatus;

	public RefactoringSummaryWizardPage(RefactoringPipeline refactoringPipeline) {
		super(Messages.RefactoringSummaryWizardPage_title);
		setTitle(Messages.RefactoringSummaryWizardPage_title);
		setDescription(Messages.RefactoringSummaryWizardPage_description);

		this.refactoringPipeline = refactoringPipeline;
		setInitialChanges();
		this.currentRefactoringState = initialSource.keySet().stream().findFirst().orElse(null);

		fSelectionStatus = new StatusInfo();
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
		viewer = new TableViewer(parent, SWT.SINGLE);

		/*
		 * label provider that sets the text displayed in CompilationUnits table
		 * to show the name of the CompilationUnit
		 */
		viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ICompilationUnit compUnit = ((RefactoringState) element).getWorkingCopy();
				return String.format("%s - %s", getClassNameString(compUnit), getPathString(compUnit)); //$NON-NLS-1$
			}
		});

		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				ICompilationUnit compUnitFirst = ((RefactoringState) e1).getWorkingCopy();
				ICompilationUnit compUnitSecond = ((RefactoringState) e2).getWorkingCopy();
				return getClassNameString(compUnitFirst).compareTo(getClassNameString(compUnitSecond));
			}
		});

		viewer.addSelectionChangedListener(createSelectionChangedListener());

		populateFileView();
	}

	/**
	 * Sets {@link Map} containing all {@link RefactoringState}s and their
	 * original source code before any change by any rule was made
	 */
	public void setInitialChanges() {
		initialSource.putAll(refactoringPipeline.getInitialSourceMap());
	}

	/**
	 * Sets {@link Map} containing all {@link RefactoringState}s and their final
	 * source code after all rules applied changes and user unselected unwanted
	 * changes
	 */
	public void setFinalChanges() {
		if (!finalSource.isEmpty()) {
			finalSource.clear();
		}
		refactoringPipeline.setSourceMap(finalSource);
		refactoringPipeline.getRefactoringStates().stream().forEach(state -> {
			if (!state.hasChange()) {
				initialSource.remove(state);
				finalSource.remove(state);
			}
		});
	
		populateFileView();
		
		if (viewer.getTable().getItemCount() > 0) {
			this.currentRefactoringState = (RefactoringState) viewer.getElementAt(0);
		} else {
			this.currentRefactoringState = null;			
		}
	}

	protected void populateFileView() {
		// if redraw, remove all items before adding
		if (viewer.getTable().getItemCount() > 0) {
			viewer.getTable().removeAll();
		}
		// adding all elements in table and checking appropriately
		initialSource.keySet().stream().forEach(entry -> {
			for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule : refactoringPipeline.getRules()) {
				if (!entry.getIgnoredRules().contains(rule) && null != entry.getChangeIfPresent(rule)) {
					viewer.add(entry);
					break;
				}
			}
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

		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		changeContainer = new Composite(parent, SWT.NONE);
		changeContainer.setLayout(new GridLayout());
		changeContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		CompareUIPlugin.getDefault().getPreferenceStore().setValue(ComparePreferencePage.OPEN_STRUCTURE_COMPARE,
				Boolean.FALSE);

	}

	/**
	 * When this page gets visible, final changes should be collected and stored
	 * and preview viewer has to be set with content
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			setFinalChanges();
			populatePreviewViewer();
			if (null != currentRefactoringState) {
				viewer.setSelection(new StructuredSelection(currentRefactoringState));
			}
			doStatusUpdate();
		}
		super.setVisible(visible);
	}

	private ISelectionChangedListener createSelectionChangedListener() {
		return event -> {
			IStructuredSelection sel = (IStructuredSelection) event.getSelection();
			if (sel.size() == 1) {
				RefactoringState newSelection = (RefactoringState) sel.getFirstElement();
				if (!newSelection.equals(currentRefactoringState)) {
					currentRefactoringState = newSelection;
					populatePreviewViewer();
				}
			}
		};
	}

	/**
	 * Method to create control for change preview viewer.
	 * 
	 * @param container
	 *            parent in which change viewer will be displayed
	 * @param input
	 *            compare viewer input
	 * @return control for change preview viewer
	 */
	private Control createInput(final Composite container, final CompareInput input) {
		try {
			PlatformUI.getWorkbench().getProgressService().run(true, true, input);
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		final Control c = input.createContents(container);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		return c;
	}

	private void populatePreviewViewer() {
		disposeControl();

		Display.getDefault().syncExec(() -> {
			CompareInput ci;
			if (null != currentRefactoringState) {
				ci = new CompareInput(currentRefactoringState.getWorkingCopyName(),
						initialSource.get(currentRefactoringState), finalSource.get(currentRefactoringState));
			} else {
				ci = new CompareInput("", "", "");
			}
			compareControl = createInput(changeContainer, ci);
			compareControl.getParent().layout();
		});
	}

	/**
	 * Open help dialog
	 */
	@Override
	public void performHelp() {
		SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
	}

	/**
	 * Manually dispose change preview viewer control
	 */
	public void disposeControl() {
		if (null != compareControl) {
			compareControl.dispose();
		}
	}

	protected void doStatusUpdate() {
		if (LicenseUtil.getInstance().isTrial()) {
			((StatusInfo) fSelectionStatus)
					.setWarning(Messages.RefactoringSummaryWizardPage_warn_disableFinishWhenTrial);
		} else {
			fSelectionStatus = new StatusInfo();
		}

		/*
		 * the mode severe status will be displayed and the OK button
		 * enabled/disabled.
		 */
		updateStatus(fSelectionStatus);
	}

	/**
	 * Updates the status line and the OK button according to the given status
	 *
	 * @param status
	 *            status to apply
	 */
	protected void updateStatus(IStatus status) {
		StatusUtil.applyToStatusLine(this, status);
	}

}
