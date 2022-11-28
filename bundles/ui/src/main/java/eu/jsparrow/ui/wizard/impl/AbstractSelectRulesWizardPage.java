package eu.jsparrow.ui.wizard.impl;

import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog.REGISTER_FOR_A_FREE_TRIAL_VERSION;
import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog.REGISTER_FOR_A_PREMIUM_LICENSE;
import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog.REGISTRATION_FOR_A_FREE_TRIAL_WILL_UNLOCK_20_OF_OUR_MOST_LIKED_RULES;
import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog.TO_UNLOCK_ALL_OUR_RULES;
import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog.TO_UNLOCK_PREMIUM_RULES;
import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog.TO_UNLOCK_THEM;
import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog.UNLOCK_SELECTED_RULES;
import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog.YOUR_SELECTION_IS_INCLUDING_FREE_RULES;
import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog.YOUR_SELECTION_IS_INCLUDING_ONLY_PREMIUM_RULES;
import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog.YOUR_SELECTION_IS_INCLUDING_PREMIUM_RULES;
import static eu.jsparrow.ui.dialog.SuggestRegistrationDialog._UPGRADE_YOUR_LICENSE;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.dialog.SuggestRegistrationDialog;
import eu.jsparrow.ui.preference.SimonykeesUpdateLicenseDialog;
import eu.jsparrow.ui.startup.registration.RegistrationDialog;
import eu.jsparrow.ui.util.LicenseUtil;

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

	protected AbstractSelectRulesWizardModel model;
	protected AbstractSelectRulesWizardControler controler;

	private Composite composite;

	private TreeViewer leftTreeViewer;
	private TableViewer rightTableViewer;

	private Button addButton;
	private Button addAllButton;
	private Button removeButton;
	private Button removeAllButton;

	private RuleDescriptionStyledText descriptionStyledText;
	private RefactoringRule selectedRuleToDescribe;

	protected IStatus fSelectionStatus;

	private boolean forcedSelectLeft = false;
	private boolean forcedSelectRight = false;
	private SelectionSide latestSelectionSide = SelectionSide.NONE;

	private LicenseUtil licenseUtil = LicenseUtil.get();
	private final List<Runnable> afterLicenseUpdateListeners = new ArrayList<>();

	protected AbstractSelectRulesWizardPage(AbstractSelectRulesWizardModel model,
			AbstractSelectRulesWizardControler controler) {
		super(Messages.SelectRulesWizardPage_page_name);
		setTitle(Messages.SelectRulesWizardPage_title);
		setDescription(Messages.SelectRulesWizardPage_description);

		fSelectionStatus = new StatusInfo();

		this.model = model;
		this.controler = controler;
		afterLicenseUpdateListeners.add(this::afterLicenseUpdate);
	}

	public void addLicenseUpdateListener(Runnable afterLicenseUpdate) {
		afterLicenseUpdateListeners.add(afterLicenseUpdate);
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

		descriptionStyledText = new RuleDescriptionStyledText(composite);
		descriptionStyledText.createDescriptionViewer();

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
				addButtonClicked((IStructuredSelection) leftTreeViewer.getSelection());
			}
		});

		leftTreeViewer.addDoubleClickListener(
				(DoubleClickEvent event) -> addButtonClicked((IStructuredSelection) leftTreeViewer.getSelection()));

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
				if (licenseUtil.isFreeLicense()) {
					List<RefactoringRule> selectionBefore = new ArrayList<>(model.getSelectionAsList());
					controler.addAllButtonClicked();
					showLockedRuleSelectionDialog(collectRecentlySelected(selectionBefore));
				} else {
					controler.addAllButtonClicked();
				}
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
			selectedRuleToDescribe = (RefactoringRule) leftSelection.get(0);
		} else if (latestSelectionSide == SelectionSide.RIGHT && rightSelection.size() == 1) {
			selectedRuleToDescribe = (RefactoringRule) rightSelection.get(0);
		} else {
			selectedRuleToDescribe = null;
		}
		updateDescriptionTextViewer();
	}

	private void updateDescriptionTextViewer() {
		if (selectedRuleToDescribe != null) {
			descriptionStyledText.createTextForDescription(selectedRuleToDescribe);
		} else {
			descriptionStyledText.setText(Messages.SelectRulesWizardPage_defaultDescriptionText);
		}
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

	private void addButtonClicked(IStructuredSelection structuredSelection) {

		if (licenseUtil.isFreeLicense()) {
			List<RefactoringRule> selectionBefore = new ArrayList<>(model.getSelectionAsList());
			controler.addButtonClicked(structuredSelection);
			showLockedRuleSelectionDialog(collectRecentlySelected(selectionBefore));
		} else {
			controler.addButtonClicked(structuredSelection);
		}
	}

	private List<RefactoringRule> collectRecentlySelected(List<RefactoringRule> selectedRulesBefore) {
		return model.getSelectionAsList()
			.stream()
			.filter(rule -> !selectedRulesBefore.contains(rule))
			.collect(Collectors.toList());
	}

	private void showLockedRuleSelectionDialog(List<RefactoringRule> selectedEnabledRules) {

		if (selectedEnabledRules.isEmpty()) {
			return;
		}

		List<Consumer<SuggestRegistrationDialog>> addComponentLambdas = null;
		if (licenseUtil.isActiveRegistration()) {
			boolean allRulesFree = selectedEnabledRules
				.stream()
				.allMatch(RefactoringRule::isFree);

			if (!allRulesFree) {
				addComponentLambdas = Arrays.asList(//
						dialog -> dialog.addLabel(YOUR_SELECTION_IS_INCLUDING_PREMIUM_RULES),
						dialog -> dialog.addLinkToUnlockAllRules(TO_UNLOCK_PREMIUM_RULES, _UPGRADE_YOUR_LICENSE),
						SuggestRegistrationDialog::addRegisterForPremiumButton);
			}
			
		} else {
			boolean containsFreeRule = selectedEnabledRules
				.stream()
				.anyMatch(RefactoringRule::isFree);

			if (containsFreeRule) {
				addComponentLambdas = Arrays.asList(//
						dialog -> dialog.addLabel(YOUR_SELECTION_IS_INCLUDING_FREE_RULES),
						dialog -> dialog.addLabel(TO_UNLOCK_THEM + REGISTER_FOR_A_FREE_TRIAL_VERSION),
						dialog -> dialog.addLabel(
								REGISTRATION_FOR_A_FREE_TRIAL_WILL_UNLOCK_20_OF_OUR_MOST_LIKED_RULES),
						SuggestRegistrationDialog::addRegisterForFreeButton,
						dialog -> dialog.addLinkToUnlockAllRules(TO_UNLOCK_ALL_OUR_RULES,
								REGISTER_FOR_A_PREMIUM_LICENSE),
						SuggestRegistrationDialog::addRegisterForPremiumButton);

			} else {
				addComponentLambdas = Arrays.asList(//
						dialog -> dialog.addLabel(YOUR_SELECTION_IS_INCLUDING_ONLY_PREMIUM_RULES),
						dialog -> dialog.addLinkToUnlockAllRules(TO_UNLOCK_THEM, REGISTER_FOR_A_PREMIUM_LICENSE),
						SuggestRegistrationDialog::addRegisterForPremiumButton,
						dialog -> dialog
							.addLabel(REGISTRATION_FOR_A_FREE_TRIAL_WILL_UNLOCK_20_OF_OUR_MOST_LIKED_RULES),
						SuggestRegistrationDialog::addRegisterForFreeButton);
			}
		}

		if (addComponentLambdas != null) {
			SuggestRegistrationDialog dialog = new SuggestRegistrationDialog(getShell(), addComponentLambdas);
			dialog.useCancelAsLastButton();
			dialog.setTextForShell(UNLOCK_SELECTED_RULES);
			int returnCode = dialog.open();
			if (returnCode == SuggestRegistrationDialog.BUTTON_ID_REGISTER_FOR_A_FREE_TRIAL) {
				showRegistrationDialog();
			} else if (returnCode == SuggestRegistrationDialog.BUTTON_ID_ENTER_PREMIUM_LICENSE_KEY) {
				showSimonykeesUpdateLicenseDialog();
			}
		}

	}

	public void showRegistrationDialog() {
		RegistrationDialog registrationDialog = new RegistrationDialog(getShell(), afterLicenseUpdateListeners);
		registrationDialog.open();
	}

	public void showSimonykeesUpdateLicenseDialog() {
		SimonykeesUpdateLicenseDialog dialog = new SimonykeesUpdateLicenseDialog(getShell(),
				afterLicenseUpdateListeners);
		dialog.create();
		dialog.open();
	}

	private void afterLicenseUpdate() {
		doStatusUpdate();
		configureTree(leftTreeViewer);
		configureTable(rightTableViewer);
		updateDescriptionTextViewer();
	}

	private enum SelectionSide {
		LEFT,
		RIGHT,
		NONE,
	}
}
