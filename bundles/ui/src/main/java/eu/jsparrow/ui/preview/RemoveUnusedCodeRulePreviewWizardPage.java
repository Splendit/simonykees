package eu.jsparrow.ui.preview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.util.RefactoringUtil;

/**
 * Represents a page in the preview wizard that displays changes made by rules
 * that remove unused code.
 * In case of removing unused fields, one page groups the changes of fields with the same access modifier. 
 * 
 * @since 4.8.0
 *
 */
public class RemoveUnusedCodeRulePreviewWizardPage extends WizardPage {

	private Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> changes;

	private CheckboxTreeViewer viewer;
	private IChangePreviewViewer currentPreviewViewer;

	private Composite previewComposite;

	private List<RemoveUnusedCodeDocumentChangeWrapper> changesWrapperList;
	private RemoveUnusedCodeDocumentChangeWrapper selectedDocWrapper;

	private List<UnusedClassMemberWrapper> uncheckedFields = new ArrayList<>();
	private List<UnusedClassMemberWrapper> recheckedFields = new ArrayList<>();
	private Map<IPath, Document> originalDocuments;
	private Consumer<UnusedClassMemberWrapper>selectionUpdater;
	private Consumer<UnusedClassMemberWrapper>unselectionUpdater;

	public RemoveUnusedCodeRulePreviewWizardPage(Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> changes,
			Map<IPath, Document> originalDocuments, RefactoringRule rule1, String title, boolean enabledDiffView, 
			Consumer<UnusedClassMemberWrapper>checkUpdater, Consumer<UnusedClassMemberWrapper>uncheckUpdater) {
		super(rule1.getRuleDescription()
			.getName());
		CustomTextEditChangePreviewViewer.setEnableDiffView(enabledDiffView);
		this.changes = changes;
		this.selectionUpdater = checkUpdater;
		this.unselectionUpdater = uncheckUpdater;

		setTitle(title);
		setDescription(rule1.getRuleDescription()
			.getDescription());
		this.originalDocuments = originalDocuments;

		convertChangesToDocumentChangeWrappers();

	}

	/**
	 * Creates {@link RemoveUnusedCodeDocumentChangeWrapper} for each
	 * {@link DocumentChange}. First finds parent and then calls method to
	 * create children for that parent.
	 */
	private void convertChangesToDocumentChangeWrappers() {
		changesWrapperList = new ArrayList<>();
		changes.entrySet()
			.stream()
			.forEach(entry -> {
				UnusedClassMemberWrapper unusedFieldWrapper = entry.getKey();
				Map<ICompilationUnit, DocumentChange> changesForField = entry.getValue();
				if (!changesForField.isEmpty()) {
					DocumentChange parent = null;
					ICompilationUnit parentICU = null;
					for (Map.Entry<ICompilationUnit, DocumentChange> dcEntry : changesForField.entrySet()) {
						ICompilationUnit iCompilationUnit = dcEntry.getKey();
						if ((unusedFieldWrapper.getDeclarationPath()).equals(iCompilationUnit.getPath())) {
							parent = changesForField.get(iCompilationUnit);
							parentICU = iCompilationUnit;
						}
					}
					if (null != parent) {
						createDocumentChangeWrapperChildren(unusedFieldWrapper,
								this.originalDocuments.get(parentICU.getPath()),
								changesForField, parent);
					}
				}
			});
		if (!changesWrapperList.isEmpty()) {
			this.selectedDocWrapper = changesWrapperList.get(0);
		}
	}

	/**
	 * Creates children for {@link RemoveUnusedCodeDocumentChangeWrapper}
	 * parent.
	 * 
	 * @param fieldData
	 * @param changesForField
	 * @param parent
	 */
	private void createDocumentChangeWrapperChildren(UnusedClassMemberWrapper fieldData, Document originalDocument,
			Map<ICompilationUnit, DocumentChange> changesForField, DocumentChange parent) {
		RemoveUnusedCodeDocumentChangeWrapper dcw = new RemoveUnusedCodeDocumentChangeWrapper(parent, null,
				originalDocument, fieldData);
		changesForField.entrySet()
			.stream()
			.map(Map.Entry::getKey)
			.forEach(iCompilationUnit -> {
				if (!(fieldData.getDeclarationPath()).equals(iCompilationUnit.getPath())) {
					DocumentChange document = changesForField.get(iCompilationUnit);
					dcw.addChild(document, iCompilationUnit.getElementName(),
							this.originalDocuments.get(iCompilationUnit.getPath()));
				}
			});

		changesWrapperList.add(dcw);
	}

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

		if (!changesWrapperList.isEmpty()) {
			ChangeElementContentProvider changeElementContentProvider = (ChangeElementContentProvider) viewer
				.getContentProvider();
			Object viewerInput = viewer.getInput();
			this.selectedDocWrapper = (RemoveUnusedCodeDocumentChangeWrapper) changeElementContentProvider
				.getElements(viewerInput)[0];
		}

		/*
		 * sets height relation between children to be 1:3 when it has two
		 * children
		 */
		sashForm.setWeights(1, 3);
	}

	/**
	 * Creates {@link CheckboxTreeViewer} which shows tree view of files with
	 * changes.
	 * 
	 * @param parent
	 *            component holding the viewer
	 */
	private void createFileView(SashForm parent) {
		viewer = new CheckboxTreeViewer(parent);
		viewer.getTree()
			.setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ChangeElementContentProvider());
		viewer.setLabelProvider(new ChangeElementLabelProvider());
		viewer.setInput("root"); // pass a non-null that will be //$NON-NLS-1$
									// ignored

		/*
		 * When checkbox state changes, set same for parent, if element it self
		 * isn't parent, and all it's children
		 */
		viewer.addCheckStateListener(this::createCheckListener);
		viewer.addSelectionChangedListener(this::createSelectionChangedListener);

		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {

				Comparator<RemoveUnusedCodeDocumentChangeWrapper> comparator = Comparator
					.comparing(RemoveUnusedCodeDocumentChangeWrapper::getCompilationUnitName)
					.thenComparing(RemoveUnusedCodeDocumentChangeWrapper::getIdentifier);
				return comparator.compare((RemoveUnusedCodeDocumentChangeWrapper) e1,
						(RemoveUnusedCodeDocumentChangeWrapper) e2);
			}
		});

		populateFileView();
	}

	private void createSelectionChangedListener(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		if (sel.size() == 1) {
			RemoveUnusedCodeDocumentChangeWrapper newSelection = (RemoveUnusedCodeDocumentChangeWrapper) sel
				.getFirstElement();
			if (!newSelection.equals(selectedDocWrapper)) {
				selectedDocWrapper = newSelection;
				populatePreviewViewer();
			}
		}
	}

	private void createCheckListener(CheckStateChangedEvent event) {
		RemoveUnusedCodeDocumentChangeWrapper selectedWrapper = (RemoveUnusedCodeDocumentChangeWrapper) event
			.getElement();
		boolean checked = event.getChecked();
		if (null == selectedWrapper.getParent()) {
			viewer.setSubtreeChecked(selectedWrapper, checked);
		} else {
			viewer.setSubtreeChecked(selectedWrapper.getParent(), checked);
		}

		UnusedClassMemberWrapper selectedFieldData = selectedWrapper.getFieldData();
		if (checked) {
			markAsNewCheck(selectedFieldData);
			this.selectionUpdater.accept(selectedFieldData);
		} else {
			markAsNewUncheck(selectedFieldData);
			this.unselectionUpdater.accept(selectedFieldData);
		}
		populatePreviewViewer();
	}

	private void markAsNewUncheck(UnusedClassMemberWrapper selectedFieldData) {
		if (recheckedFields.contains(selectedFieldData)) {
			recheckedFields.remove(selectedFieldData);
		} else if (!uncheckedFields.contains(selectedFieldData)) {
			uncheckedFields.add(selectedFieldData);
		}
	}

	private void markAsNewCheck(UnusedClassMemberWrapper selectedFieldData) {
		if (uncheckedFields.contains(selectedFieldData)) {
			uncheckedFields.remove(selectedFieldData);
		} else if (!recheckedFields.contains(selectedFieldData)) {
			recheckedFields.add(selectedFieldData);
		}
	}

	/**
	 * Fills {@link CheckboxTreeViewer} component with data.
	 */
	private void populateFileView() {
		RemoveUnusedCodeDocumentChangeWrapper[] changesArray = changesWrapperList
			.toArray(new RemoveUnusedCodeDocumentChangeWrapper[] {});
		viewer.setInput(changesArray);
		Arrays.asList(changesArray)
			.stream()
			.forEach(change -> viewer.setSubtreeChecked(change, true));
	}

	/**
	 * Creates {@link TextEditChangePreviewViewer} component to display changes
	 * before and after applying the rule.
	 * 
	 * @param parent
	 *            component holding the viewer
	 */
	private void createPreviewViewer(SashForm parent) {

		previewComposite = new Composite(parent, SWT.NONE);
		// GridData works with GridLayout
		GridData gridData = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gridData);
		previewComposite.setLayout(new GridLayout());
		previewComposite.setLayoutData(gridData);

		currentPreviewViewer = new CustomTextEditChangePreviewViewer();
		currentPreviewViewer.createControl(previewComposite);

		currentPreviewViewer.getControl()
			.getParent()
			.layout();

		populatePreviewViewer();
	}

	/**
	 * Populates change viewer with data.
	 */
	public void populatePreviewViewer() {

		if (this.selectedDocWrapper != null) {
			disposePages();
			currentPreviewViewer.createControl(previewComposite);
			currentPreviewViewer.getControl()
				.setLayoutData(new GridData(GridData.FILL_BOTH));

			@SuppressWarnings("restriction")
			ChangePreviewViewerInput viewerInput = TextEditChangePreviewViewer.createInput(getCurrentDocumentChange());
			currentPreviewViewer.setInput(viewerInput);

			currentPreviewViewer.getControl()
				.getParent()
				.layout();
		}
	}

	/**
	 * Disposes the control of all pages of type
	 * {@link RenamingRulePreviewWizardPage} and
	 * {@link RenamingRuleSummaryWizardPage} of the current wizard.
	 */
	private void disposePages() {
		IWizardPage[] pages = getWizard().getPages();
		for (IWizardPage page : pages) {
			if (page instanceof RemoveUnusedCodeRulePreviewWizardPage) {
				((RemoveUnusedCodeRulePreviewWizardPage) page).disposeControl();
			}
		}
	}

	/**
	 * Used to populate preview viewer only if this page gets visible
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			populatePreviewViewer();
		} else {
			disposeControl();
		}
		super.setVisible(visible);
	}

	/**
	 * Used to dispose control every time preview viewer content changes or page
	 * gets invisible. New control is created when needed. This way conflicting
	 * handlers are avoided because there is no multiple viewers which would
	 * register multiple handlers for same action.
	 */
	public void disposeControl() {
		if (currentPreviewViewer != null && null != currentPreviewViewer.getControl()) {
			currentPreviewViewer.getControl()
				.dispose();
		}
	}

	/**
	 * Gets the current DocumentChange if checkbox in front of file name is
	 * selected. Otherwise generates and returns new DocumentChange with empty
	 * text edit.
	 * 
	 * @return current or generated DocumentChange
	 */
	private DocumentChange getCurrentDocumentChange() {
		if (!viewer.getChecked(selectedDocWrapper)) {
			/*
			 * When compilation unit is unselected for rule that is shown,
			 * change preview viewer should show no change. For that generate
			 * document change is called with empty edit to create document
			 * change with text type java but with no changes.
			 */
			TextEdit edit = new MultiTextEdit();
			return RefactoringUtil.generateDocumentChange(selectedDocWrapper.getCompilationUnitName(),
					selectedDocWrapper.getOriginalDocument(), edit);
		} else {
			return selectedDocWrapper.getDocumentChange();
		}
	}

	public boolean isRecalculateNeeded() {
		return !uncheckedFields.isEmpty() || !recheckedFields.isEmpty();
	}

	public void populateViews(boolean forcePreviewViewerUpdate) {
		populateFileView();
		if (forcePreviewViewerUpdate) {
			populatePreviewViewer();
		}
		viewer.setSelection(new StructuredSelection(selectedDocWrapper));
	}

	public void setSelection() {
		viewer.setSelection(new StructuredSelection(selectedDocWrapper));
	}

	/**
	 * Clears the flags which indicate whether new recalculation is needed as a
	 * result of new un/selections.
	 */
	public void clearNewSelections() {
		uncheckedFields.clear();
		recheckedFields.clear();
	}
}
