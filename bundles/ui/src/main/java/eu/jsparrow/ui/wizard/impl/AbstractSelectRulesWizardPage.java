package eu.jsparrow.ui.wizard.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.ResourceHelper;

/**
 * Lists all rules as checkboxes and a description for the currently selected
 * rule. From version 1.3 checkboxes are replaced with windows for filtering and
 * adding selection
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa, Martin Huter, Andreja Sambolec,
 *         Matthias Webhofer
 * @since 0.9 refactored in 1.3
 */
@SuppressWarnings("restriction") // StatusInfo is internal
public abstract class AbstractSelectRulesWizardPage extends WizardPage {

	private static class SelectedRule {
		private SelectedRule() {

		}

		static int start = 0;
		static int end = 0;
		static String link = ""; //$NON-NLS-1$
	}

	private static final String DOCUMENTATION_SPACE_BASE_URL = "https://jsparrow.github.io/rules/"; //$NON-NLS-1$

	protected AbstractSelectRulesWizardModel model;
	protected AbstractSelectRulesWizardControler controler;

	private Composite composite;

	private TreeViewer leftTreeViewer;
	private TableViewer rightTableViewer;

	private Button addButton;
	private Button addAllButton;
	private Button removeButton;
	private Button removeAllButton;

	private StyledText descriptionStyledText;

	protected IStatus fSelectionStatus;

	private boolean forcedSelectLeft = false;
	private boolean forcedSelectRight = false;
	private SelectionSide latestSelectionSide = SelectionSide.NONE;

	private LicenseUtil licenseUtil = LicenseUtil.get();

	public AbstractSelectRulesWizardPage(AbstractSelectRulesWizardModel model,
			AbstractSelectRulesWizardControler controler) {
		super(Messages.SelectRulesWizardPage_page_name);
		setTitle(Messages.SelectRulesWizardPage_title);
		setDescription(Messages.SelectRulesWizardPage_description);

		fSelectionStatus = new StatusInfo();

		this.model = model;
		this.controler = controler;
	}

	/**
	 * Gets called when the page gets created.
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		setControl(composite);

		createFilteringPart(composite);

		createSelectionViewer(composite);

		createDescriptionViewer(composite);

		model.addListener(this::updateData);

		Dialog.applyDialogFont(composite);

		updateData();
	}

	protected abstract void createFilteringPart(Composite composite);

	/**
	 * Creates part of wizard for selecting the rules, built from tree parts.
	 * First part, left, is tree view in which all filtered rules are shown and
	 * can be chosen to add on right side. Middle part contains buttons to add
	 * chosen rules to selection or remove rules already selected. Third, right,
	 * part is table view containing rules that are selected to be applied.
	 * 
	 * @param parent
	 */
	private void createSelectionViewer(Composite parent) {
		Composite leftCenterRightComposite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = convertHeightInCharsToPixels(20);
		leftCenterRightComposite.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		leftCenterRightComposite.setLayout(gridLayout);

		Composite leftComposite = new Composite(leftCenterRightComposite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = convertWidthInCharsToPixels(40);
		leftComposite.setLayoutData(gridData);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		leftComposite.setLayout(gridLayout);

		Composite centerComposite = new Composite(leftCenterRightComposite, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		centerComposite.setLayout(gridLayout);
		centerComposite.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

		Composite rightComposite = new Composite(leftCenterRightComposite, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = convertWidthInCharsToPixels(40);
		rightComposite.setLayoutData(gridData);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		rightComposite.setLayout(gridLayout);

		createTree(leftComposite);
		createTable(rightComposite);

		createButtonBar(centerComposite);
	}

	private void createButtonBar(Composite parent) {
		Label spacer = new Label(parent, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		addButton = new Button(parent, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addButton.setText(Messages.SelectRulesWizardPage_addButtonLabel);

		addAllButton = new Button(parent, SWT.PUSH);
		addAllButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addAllButton.setText(Messages.SelectRulesWizardPage_addAllButtonLabel);

		removeButton = new Button(parent, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeButton.setText(Messages.SelectRulesWizardPage_removeButtonLabel);

		removeAllButton = new Button(parent, SWT.PUSH);
		removeAllButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		removeAllButton.setText(Messages.SelectRulesWizardPage_removeAllButtonLabel);

		leftTreeViewer.addSelectionChangedListener((SelectionChangedEvent event) -> {
			latestSelectionSide = SelectionSide.LEFT;

			if (forcedSelectLeft) {
				forcedSelectLeft = false;
				/*
				 * if it is manually selected because of moving, don't update
				 * view
				 */
			} else {
				latestSelectionSide = SelectionSide.LEFT;
				controler.selectionChanged();
			}
		});

		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.addButtonClicked((IStructuredSelection) leftTreeViewer.getSelection());
			}
		});

		leftTreeViewer.addDoubleClickListener((DoubleClickEvent event) -> controler
			.addButtonClicked((IStructuredSelection) leftTreeViewer.getSelection()));

		rightTableViewer.addSelectionChangedListener((SelectionChangedEvent event) -> {
			latestSelectionSide = SelectionSide.RIGHT;

			if (forcedSelectRight) {
				forcedSelectRight = false;
			} else {
				controler.selectionChanged();
				removeButton.setEnabled(!event.getSelection()
					.isEmpty());
			}
		});

		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.removeButtonClicked((IStructuredSelection) rightTableViewer.getSelection());
			}
		});

		rightTableViewer.addDoubleClickListener((DoubleClickEvent event) -> controler
			.removeButtonClicked((IStructuredSelection) rightTableViewer.getSelection()));

		addAllButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.
			 * eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.addAllButtonClicked();
			}
		});

		removeAllButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.
			 * eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.removeAllButtonClicked();
			}

		});

	}

	private void createTree(Composite parent) {
		leftTreeViewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		leftTreeViewer.getControl()
			.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		leftTreeViewer.setUseHashlookup(true);

		configureTree(leftTreeViewer);
	}

	private void createTable(Composite parent) {
		rightTableViewer = new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		rightTableViewer.getControl()
			.setLayoutData(gd);
		rightTableViewer.setUseHashlookup(true);

		configureTable(rightTableViewer);
	}

	protected void configureTree(TreeViewer tree) {
		tree.setContentProvider(new ITreeContentProvider() {

			@Override
			public boolean hasChildren(Object element) {
				return false;
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return new Object[] {};
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object inputElement) {
				Set<String> list = (Set<String>) inputElement;
				return list.toArray();
			}

		});
		tree.setLabelProvider(new TreeLabelProvider());
		tree.setComparator(new JavaElementComparator());
	}

	protected void configureTable(TableViewer table) {
		rightTableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement) {
				Set<RefactoringRule> list = (Set<RefactoringRule>) inputElement;
				return list.toArray();
			}
		});

		table.setLabelProvider(new TableLabelProvider());
		table.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				RefactoringRule rule1 = (RefactoringRule) e1;
				RefactoringRule rule2 = (RefactoringRule) e2;
				return rule1.getRuleDescription()
					.getName()
					.compareTo(rule2.getRuleDescription()
						.getName());
			}
		});
	}

	/**
	 * Creates bottom part of select wizard containing Text field with
	 * description of selected rule if only one rule is selected, default
	 * description otherwise.
	 * 
	 * @param parent
	 */
	private void createDescriptionViewer(Composite parent) {
		/*
		 * There is a known issue with automatically showing and hiding
		 * scrollbars and SWT.WRAP. Using StyledText and
		 * setAlwaysShowScrollBars(false) makes the vertical scroll work
		 * correctly at least.
		 */
		descriptionStyledText = new StyledText(parent, SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		descriptionStyledText.setAlwaysShowScrollBars(false);
		descriptionStyledText.setEditable(false);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 110;
		descriptionStyledText.setLayoutData(gridData);
		descriptionStyledText.setMargins(2, 2, 2, 2);
		descriptionStyledText.addListener(SWT.MouseDown, event -> {
			int offset;
			try {
				offset = descriptionStyledText.getOffsetAtPoint(new Point(event.x, event.y));
			} catch (SWTException | IllegalArgumentException e) {
				offset = -1;
			}
			if (offset != -1 && SelectedRule.start < offset && offset < SelectedRule.end) {
				Program.launch(SelectedRule.link);
			}
		});
	}

	/**
	 * Updates entire view with data every time something is changed in model.
	 */
	protected void updateData() {
		/*
		 * check if model has changed to update table and tree view or is just
		 * selection changed to update description field and buttons
		 */
		if (model.hasChanged()) {
			if (!model.isForced()) {
				model.filterPosibilitiesByTags();
				model.removeAlreadySelected();
				if (StringUtils.isEmpty(model.getNameFilter())) {
					leftTreeViewer.setInput(model.getPosibilities());
				} else {
					leftTreeViewer.setInput(model.filterPosibilitiesByName());
				}
				rightTableViewer.setInput(model.getSelection());
				/*
				 * updates enabling Finish button according to right side table
				 * view if selection is empty Finish button is disabled
				 */
			} else {
				leftTreeViewer.setInput(model.getPosibilities());
				rightTableViewer.setInput(model.getSelection());
				model.resetForced();
			}
			if (!model.getRecentlyMoved()
				.isEmpty()) {
				if (model.isMovedToRight()) {
					forcedSelectRight = true;
					rightTableViewer.setSelection(new StructuredSelection(model.getRecentlyMoved()
						.toArray()), false);
				} else {
					forcedSelectLeft = true;
					leftTreeViewer.setSelection(new StructuredSelection(model.getRecentlyMoved()
						.toArray()), false);
				}
				model.getRecentlyMoved()
					.clear();
			}
			getContainer().updateButtons();
			model.resetChanged();
		}
		populateDescriptionTextViewer();
		updateButtons();
		doStatusUpdate();
	}

	@SuppressWarnings("unchecked")
	private void updateButtons() {
		addButton.setEnabled(!leftTreeViewer.getSelection()
			.isEmpty()
				&& selectionContainsEnabledEntry(((IStructuredSelection) leftTreeViewer.getSelection()).toList()));
		addAllButton.setEnabled(!((Set<Object>) leftTreeViewer.getInput()).isEmpty()
				&& selectionContainsEnabledEntry(new ArrayList<>((Set<Object>) leftTreeViewer.getInput())));
		removeButton.setEnabled(!rightTableViewer.getSelection()
			.isEmpty());
		removeAllButton.setEnabled(!((Set<Object>) rightTableViewer.getInput()).isEmpty());
	}

	/**
	 * Sets the rule description text according to the currently selected rule
	 * or to the default text if no rule is selected.
	 */
	@SuppressWarnings("unchecked")
	private void populateDescriptionTextViewer() {
		List<Object> leftSelection = ((IStructuredSelection) leftTreeViewer.getSelection()).toList();
		List<Object> rightSelection = ((IStructuredSelection) rightTableViewer.getSelection()).toList();

		if (latestSelectionSide == SelectionSide.LEFT && leftSelection.size() == 1) {
			createTextForDescription((RefactoringRule) leftSelection.get(0));
		} else if (latestSelectionSide == SelectionSide.RIGHT && rightSelection.size() == 1) {
			createTextForDescription((RefactoringRule) rightSelection.get(0));
		} else {
			descriptionStyledText.setText(Messages.SelectRulesWizardPage_defaultDescriptionText);
		}
	}

	/**
	 * Creating description for rule to be displayed using StyledText
	 * 
	 * @param rule
	 */
	private void createTextForDescription(RefactoringRule rule) {

		final String lineDelimiter = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_lineDelimiter;
		final String requirementsLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_requirementsLabel;
		final String minJavaVersionLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_minJavaVersionLabel;
		final String requiredLibrariesLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_librariesLabel;
		final String tagsLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_tagsLabel;
		final String documentationLabel = Messages.AbstractSelectRulesWizardPage_seeDocumentation;

		String name = rule.getRuleDescription()
			.getName();
		String description = rule.getRuleDescription()
			.getDescription();
		String minJavaVersionValue = rule.getRequiredJavaVersion();
		String requiredLibrariesValue = (null != rule.requiredLibraries()) ? rule.requiredLibraries()
				: Messages.AbstractSelectRulesWizardPage_descriptionStyledText_librariesNoneLabel;
		String jSparrowStarterValue = (rule.isFree() && licenseUtil.isFreeLicense())
				? Messages.AbstractSelectRulesWizardPage_freemiumRegirementsMessage + lineDelimiter
				: ""; //$NON-NLS-1$
		String tagsValue = StringUtils.join(rule.getRuleDescription()
			.getTags()
			.stream()
			.map(Tag::getTagNames)
			.collect(Collectors.toList()), "  "); //$NON-NLS-1$

		FontData data = descriptionStyledText.getFont()
			.getFontData()[0];
		Consumer<StyleRange> h1 = style -> style.font = new Font(getShell().getDisplay(), data.getName(),
				data.getHeight() * 3 / 2, data.getStyle());
		Consumer<StyleRange> h2 = style -> style.font = new Font(getShell().getDisplay(), data.getName(),
				data.getHeight(), data.getStyle());
		Consumer<StyleRange> bold = style -> style.font = new Font(getShell().getDisplay(), data.getName(),
				data.getHeight(), SWT.BOLD);

		Consumer<StyleRange> blue = style -> style.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_BLUE);
		Consumer<StyleRange> red = style -> style.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_RED);
		Consumer<StyleRange> green = style -> style.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_GREEN);

		SelectedRule.link = ResourceHelper.generateLinkToDocumentation(DOCUMENTATION_SPACE_BASE_URL, rule.getId());
		Consumer<StyleRange> documentationConfig = style -> {
			style.underline = true;
			style.underlineStyle = SWT.UNDERLINE_LINK;
			style.data = SelectedRule.link;
		};

		List<StyleContainer> descriptionList = new ArrayList<>();
		descriptionList.add(new StyleContainer(name, h1));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(documentationLabel, blue.andThen(documentationConfig)));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(description));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(requirementsLabel, bold));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(minJavaVersionLabel, h2));
		descriptionList.add(new StyleContainer(minJavaVersionValue, bold.andThen(red), !rule.isSatisfiedJavaVersion()));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(requiredLibrariesLabel, h2));
		descriptionList
			.add(new StyleContainer(requiredLibrariesValue, bold.andThen(red), !rule.isSatisfiedLibraries()));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(jSparrowStarterValue, bold.andThen(green)));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(tagsLabel, bold));
		descriptionList.add(new StyleContainer(lineDelimiter));
		descriptionList.add(new StyleContainer(tagsValue));

		String descriptionText = descriptionList.stream()
			.map(StyleContainer::getValue)
			.collect(Collectors.joining());

		descriptionStyledText.setText(descriptionText);

		int offset = 0;
		for (StyleContainer iterator : descriptionList) {
			if (!lineDelimiter.equals(iterator.getValue()) && iterator.isEnabled()) {
				descriptionStyledText.setStyleRange(iterator.generateStyle(offset));
				if (documentationLabel.equals(iterator.getValue())) {
					SelectedRule.start = offset;
					SelectedRule.end = offset + iterator.getValue()
						.length();
				}
			}
			offset += iterator.getValue()
				.length();
		}

		int requirementsBulletingStartLine = descriptionStyledText
			.getLineAtOffset(name.length() + lineDelimiter.length() + documentationLabel.length()
					+ 2 * lineDelimiter.length() + description.length() + 2 * lineDelimiter.length()
					+ requirementsLabel.length() + lineDelimiter.length());

		StyleRange bulletPointStyle = new StyleRange();
		bulletPointStyle.metrics = new GlyphMetrics(0, 0, 40);
		bulletPointStyle.foreground = getShell().getDisplay()
			.getSystemColor(SWT.COLOR_BLACK);
		Bullet bulletPoint = new Bullet(bulletPointStyle);

		descriptionStyledText.setLineBullet(requirementsBulletingStartLine, jSparrowStarterValue.isEmpty() ? 2 : 3,
				bulletPoint);
	}

	private boolean selectionContainsEnabledEntry(List<Object> selection) {
		return selection.stream()
			.anyMatch(object -> ((RefactoringRule) object).isEnabled());
	}

	public void recalculateLayout() {
		composite.layout(true, true);
	}

	protected abstract void doStatusUpdate();

	protected void doStatusUpdate(IStatus additionalStatus) {
		if (!model.getUnapplicableRules()
			.isEmpty()) {
			((StatusInfo) fSelectionStatus)
				.setWarning(Messages.AbstractSelectRulesWizardPage_warning_RulesInProfileNotApplicable);
		} else if (model.getSelectionAsList()
			.isEmpty()) {
			((StatusInfo) fSelectionStatus).setError(Messages.AbstractSelectRulesWizardPage_error_NoRulesSelected);
		} else if (licenseUtil.isFreeLicense()) {
			if (licenseUtil.isActiveRegistration()) {
				if (model.selectionContainsNonFreemiumRules()) {
					((StatusInfo) fSelectionStatus)
						.setWarning(Messages.AbstractSelectRulesWizardPage_notOnlyFreemiumSelected_statusInfoMessage);
				} else {
					fSelectionStatus = new StatusInfo();
				}
			} else {
				((StatusInfo) fSelectionStatus)
					.setWarning(Messages.AbstractSelectRulesWizardPage_neitherRegisteredNorLicensed_statusInfoMessage);
			}
		} else {
			fSelectionStatus = new StatusInfo();
		}

		// status of all used components
		IStatus[] status;
		if (null != additionalStatus) {
			status = new IStatus[] { fSelectionStatus, additionalStatus };
		} else {
			status = new IStatus[] { fSelectionStatus };
		}

		/*
		 * the mode severe status will be displayed and the OK button
		 * enabled/disabled.
		 */
		updateStatus(status);
	}

	/**
	 * Open help dialog
	 */
	@Override
	public void performHelp() {
		SimonykeesMessageDialog.openDefaultHelpMessageDialog(getShell());
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

	/**
	 * Updates the status line and the OK button according to the status
	 * evaluate from an array of status. The most severe error is taken. In case
	 * that two status with the same severity exists, the status with lower
	 * index is taken.
	 *
	 * @param status
	 *            the array of status
	 */
	protected void updateStatus(IStatus[] status) {
		updateStatus(StatusUtil.getMostSevere(status));
	}

	protected TreeViewer getLeftTreeViewer() {
		return leftTreeViewer;
	}

	protected TableViewer getRightTableViewer() {
		return rightTableViewer;
	}

	protected Button getAddButton() {
		return addButton;
	}

	protected Button getAddAllButton() {
		return addAllButton;
	}

	protected Button getRemoveButton() {
		return removeButton;
	}

	protected Button getRemoveAllButton() {
		return removeAllButton;
	}

	private enum SelectionSide {
		LEFT,
		RIGHT,
		NONE,
	}
}
