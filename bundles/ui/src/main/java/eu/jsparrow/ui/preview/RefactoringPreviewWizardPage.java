package eu.jsparrow.ui.preview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.FileChangeCount;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.util.RefactoringUtil;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;
import eu.jsparrow.ui.preview.statistics.RuleStatisticsSection;
import eu.jsparrow.ui.preview.statistics.StatisticsSection;
import eu.jsparrow.ui.preview.statistics.StatisticsSectionUpdater;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;

/**
 * The warning concerning restrictions is generated by a call to
 * {@link TextEditChangePreviewViewer}, which is an internal Eclipse class.
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer, Andreja Sambolec, Hans-Jörg
 *         Schrödl, Matthias Webhofer
 * @since 0.9
 */
@SuppressWarnings("restriction")
public class RefactoringPreviewWizardPage extends WizardPage {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringPreviewWizardPage.class);

	private RuleStatisticsSection ruleStatisticsSection;
	private StatisticsSection statisticsSection;
	private StatisticsSectionUpdater statisticsUpdater;

	private IChangePreviewViewer currentPreviewViewer;
	private CheckboxTableViewer viewer;
	private List<Image> disposables = new ArrayList<>();

	private Composite previewContainer;
	protected IStatus fSelectionStatus;

	private RefactoringPreviewWizardModel wizardModel;
	private RefactoringPreviewWizardPageModel model;
	private LicenseUtilService licenseUtil = LicenseUtil.get();

	public RefactoringPreviewWizardPage(Map<ICompilationUnit, DocumentChange> changesForRule, RefactoringRule rule,
			RefactoringPreviewWizardModel wizardModel, boolean enabled, RuleStatisticsSection ruleStatisticsSection,
			StatisticsSectionUpdater statisticsUpdater) {
		super(rule.getRuleDescription()
			.getName());
		this.model = new RefactoringPreviewWizardPageModel();
		this.statisticsUpdater = statisticsUpdater;
		CustomTextEditChangePreviewViewer.setEnableDiffView(enabled);
		ContextInjectionFactory.inject(this, Activator.getEclipseContext());
		this.ruleStatisticsSection = ruleStatisticsSection;
		setTitle(rule.getRuleDescription()
			.getName());
		setDescription(rule.getRuleDescription()
			.getDescription());

		this.wizardModel = wizardModel;
		wizardModel.addRule(rule);
		changesForRule.keySet()
			.stream()
			.forEach(x -> wizardModel.addFileToRule(rule, x.getHandleIdentifier()));

		this.setChangesForRule(changesForRule);
		this.setRule(rule);

		this.setCurrentCompilationUnit(changesForRule.keySet()
			.stream()
			.findFirst()
			.orElse(null));

		fSelectionStatus = new StatusInfo();
	}

	public void setTotalStatisticsSection(StatisticsSection statisticsSection) {
		this.statisticsSection = statisticsSection;
	}

	private Optional<StatisticsSection> getTotalStatisticsSection() {
		return Optional.ofNullable(this.statisticsSection);
	}

	private void initializeDataBindings() {
		this.ruleStatisticsSection.initializeDataBindings();
		getTotalStatisticsSection().ifPresent(StatisticsSection::initializeDataBindings);
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

		GridLayout layout = new GridLayout(1, false);

		// margin from TextEditChangePreviewViewer to Composite
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// without setting the layout, nothing displays
		container.setLayout(layout);

		setControl(container);

		ruleStatisticsSection.createRuleRuleStatisticsView(container);

		// Create the SashForm
		Composite sash = new Composite(container, SWT.NONE);
		sash.setLayout(new GridLayout());
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		SashForm sashForm = new SashForm(sash, SWT.VERTICAL);

		createFileView(sashForm);
		createPreviewViewer(sashForm);

		if (!getChangesForRule().keySet()
			.isEmpty()) {
			this.setCurrentCompilationUnit((ICompilationUnit) viewer.getElementAt(0));
		}
		/*
		 * sets height relation between children to be 1:3 when it has two
		 * children
		 */
		sashForm.setWeights(1, 3);
		List<Image> images = getTotalStatisticsSection().map(statistics -> statistics.createView(container))
			.orElse(Collections.emptyList());
		disposables.addAll(images);
		initializeDataBindings();
	}

	private void createFileView(Composite parent) {
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE);

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

		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				ICompilationUnit compUnitFirst = (ICompilationUnit) e1;
				ICompilationUnit compUnitSecond = (ICompilationUnit) e2;
				return getClassNameString(compUnitFirst).compareTo(getClassNameString(compUnitSecond));
			}
		});

		viewer.addSelectionChangedListener(createSelectionChangedListener());
		viewer.addCheckStateListener(createCheckStateListener());

		populateFileView();
	}

	protected void populateFileView() {
		// if redraw, remove all items before adding
		if (viewer.getTable()
			.getItemCount() > 0) {
			viewer.getTable()
				.removeAll();
		}
		// adding all elements in table and checking appropriately
		getChangesForRule().keySet()
			.stream()
			.forEach(entry -> {
				viewer.add(entry);
				viewer.setChecked(entry,
						!(getUnselected().containsKey(entry.getElementName())
								|| getUnselectedChange().contains(entry)));
			});
	}

	private void createPreviewViewer(Composite parent) {

		// GridData works with GridLayout
		GridData gridData = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gridData);

		previewContainer = new Composite(parent, SWT.NONE);
		previewContainer.setLayout(new GridLayout());
		previewContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		currentPreviewViewer = new CustomTextEditChangePreviewViewer();
	}

	private ISelectionChangedListener createSelectionChangedListener() {
		return event -> {
			IStructuredSelection sel = (IStructuredSelection) event.getSelection();

			if (sel.size() == 1) {
				ICompilationUnit newSelection = (ICompilationUnit) sel.getFirstElement();
				if (!newSelection.equals(getCurrentCompilationUnit())) {
					this.setCurrentCompilationUnit(newSelection);
					if (isCurrentPage()) {
						populatePreviewViewer();
					}
				}
			}
		};
	}

	private ICheckStateListener createCheckStateListener() {
		return event -> {
			ICompilationUnit newSelection = (ICompilationUnit) event.getElement();
			if (event.getChecked()) {
				/*
				 * remove from unselected and recalculate rules for this
				 * compilationUnit
				 */
				if (getUnselected().containsKey(newSelection.getElementName())) {
					getUnselected().remove(newSelection.getElementName());
				}
				if (getUnselectedChange().contains(newSelection)) {
					getUnselectedChange().remove(newSelection);
				}
				clearCounterForChangedFile(newSelection);
				wizardModel.addFileToRule(getRule(), newSelection.getHandleIdentifier());
				immediatelyUpdateForSelected(newSelection);
			} else {
				// add in list with unselected classes
				if (!getUnselected().containsKey(newSelection.getElementName())) {
					getUnselectedChange().add(newSelection);
				}
				clearCounterForChangedFile(newSelection);
				wizardModel.removeFileFromRule(getRule(), newSelection.getHandleIdentifier());
			}
			// This method simply counts checked items in the table. Not very
			// MVC, and should be replaced with a proper solution
			statisticsUpdater.update(ruleStatisticsSection, getRule(), wizardModel);
		};
	}

	/**
	 * Used to populate preview viewer only if this page gets visible
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			populatePreviewViewer();
			if (getRule() instanceof StandardLoggerRule) {
				doStatusUpdate();
			}
		} else {
			disposeControl();
		}
		super.setVisible(visible);
	}

	private void immediatelyUpdateForSelected(ICompilationUnit newSelection) {
		((RefactoringPreviewWizard) getWizard()).imediatelyUpdateForSelected(newSelection, getRule());
	}

	private void populatePreviewViewer() {
		disposeControl();

		currentPreviewViewer.createControl(previewContainer);
		currentPreviewViewer.getControl()
			.setLayoutData(new GridData(GridData.FILL_BOTH));

		currentPreviewViewer.setInput(CustomTextEditChangePreviewViewer.createInput(getCurrentDocumentChange()));
		((CompareViewerSwitchingPane) currentPreviewViewer.getControl())
			.setTitleArgument(getCurrentCompilationUnit().getElementName());

		currentPreviewViewer.getControl()
			.getParent()
			.layout();
	}

	private DocumentChange getCurrentDocumentChange() {
		if (null == getChangesForRule().get(getCurrentCompilationUnit())) {
			DocumentChange documentChange = null;
			try {
				/*
				 * When compilation unit is unselected for rule that is shown,
				 * change preview viewer should show no change. For that
				 * generate document change is called with empty edit to create
				 * document change with text type java but with no changes.
				 */
				TextEdit edit = new MultiTextEdit();
				return RefactoringUtil.generateDocumentChange(getCurrentCompilationUnit().getElementName(),
						new Document(getCurrentCompilationUnit().getSource()), edit);
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), e);
			}
			return documentChange;
		} else {
			return getChangesForRule().get(getCurrentCompilationUnit());
		}
	}

	/**
	 * Used to populate IChangePreviewViewer currentPreviewViewer and
	 * CheckboxTableViewer viewer every time page gets displayed. Sets the
	 * selection in file view part to match file whose changes are displayed in
	 * changes view. If forcePreviewViewerUpdate is set to true preview viewer
	 * is also populated. It is used only when this file is already visible
	 * (when previously unselected file gets selected again).
	 * 
	 * @param forcePreviewViewerUpdate
	 *            flag if preview viewer should be populated
	 */
	public void populateViews(boolean forcePreviewViewerUpdate) {
		populateFileView();
		if (forcePreviewViewerUpdate) {
			populatePreviewViewer();
		}
		viewer.setSelection(new StructuredSelection(getCurrentCompilationUnit()));
	}

	/**
	 * Open help dialog
	 */
	@Override
	public void performHelp() {
		SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
	}

	/**
	 * Used to dispose control every time preview viewer content changes or page
	 * gets invisible. New control is created when needed. This way conflicting
	 * handers are avoided because there is no multiple viewers which would
	 * register multiple handlers for same action.
	 */
	public void disposeControl() {
		if (null != currentPreviewViewer.getControl()) {
			currentPreviewViewer.getControl()
				.dispose();
		}
	}

	protected void doStatusUpdate() {
		if (licenseUtil.isFreeLicense()) {
			((StatusInfo) fSelectionStatus)
				.setWarning(Messages.RefactoringSummaryWizardPage_warn_disableFinishWhenFree);
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

	private void clearCounterForChangedFile(ICompilationUnit newSelection) {
		wizardModel.getChangedFilesPerRule()
			.keySet()
			.stream()
			.filter(changedFileRule -> wizardModel.getFilesForRule(changedFileRule)
				.contains(newSelection.getHandleIdentifier()))
			.map(changedFileRule -> RuleApplicationCount.getFor(changedFileRule)
				.getApplicationsForFile(newSelection.getHandleIdentifier()))
			.forEach(FileChangeCount::clear);
	}

	@Override
	public void dispose() {
		ruleStatisticsSection.dispose();
		disposables.forEach(Image::dispose);
		disposables.clear();
		super.dispose();
	}

	String getClassNameString(ICompilationUnit compilationUnit) {
		return model.getClassNameString(compilationUnit);
	}

	String getPathString(ICompilationUnit compilationUnit) {
		return model.getPathString(compilationUnit);
	}

	public List<ICompilationUnit> getUnselectedChange() {
		return model.getUnselectedChange();
	}

	public void applyUnselectedChange() {
		model.applyUnselectedChange();
	}

	public RefactoringRule getRule() {
		return model.getRule();
	}

	public void update(Map<ICompilationUnit, DocumentChange> changesForRule) {
		model.update(changesForRule);
	}

	ICompilationUnit getCurrentCompilationUnit() {
		return model.getCurrentCompilationUnit();
	}

	void setCurrentCompilationUnit(ICompilationUnit currentCompilationUnit) {
		model.setCurrentCompilationUnit(currentCompilationUnit);
	}

	public Map<ICompilationUnit, DocumentChange> getChangesForRule() {
		return model.getChangesForRule();
	}

	public void setChangesForRule(Map<ICompilationUnit, DocumentChange> changesForRule) {
		model.setChangesForRule(changesForRule);
	}

	public void setRule(RefactoringRule rule) {
		model.setRule(rule);
	}

	public Map<String, ICompilationUnit> getUnselected() {
		return model.getUnselected();
	}

	public void setUnselected(Map<String, ICompilationUnit> unselected) {
		model.setUnselected(unselected);
	}
}
