package at.splendit.simonykees.core.ui.wizard.impl;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * Lists all rules as checkboxes and a description for the currently selected
 * rule. From version 1.3 checkboxes are replaced with windows for filtering and
 * adding selection
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa, Martin Huter, Andreja Sambolec
 * @since 0.9 refactored in 1.3
 */
public class SelectRulesWizardPage extends WizardPage {

	private SelectRulesWizardPageModel model;
	private SelectRulesWizardPageControler controler;

	private Label groupFilterLabel;
	private Combo groupFilterCombo;

	private Label nameFilterLabel;
	private Text nameFilterText;

	private Button removeDisabledRulesButton;

	private TreeViewer leftTreeViewer;
	private TableViewer rightTableViewer;

	private Button addButton;
	private Button addAllButton;
	private Button removeButton;
	private Button removeAllButton;

	private StyledText descriptionStyledText;

	public SelectRulesWizardPage(SelectRulesWizardPageModel model, SelectRulesWizardPageControler controler) {
		super(Messages.SelectRulesWizardPage_page_name);
		setTitle(Messages.SelectRulesWizardPage_title);
		setDescription(Messages.SelectRulesWizardPage_description);

		this.model = model;
		this.controler = controler;
	}

	/**
	 * Gets called when the page gets created.
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		setControl(composite);

		createFilteringPart(composite);

		createSelectionViewer(composite);

		createDescriptionViewer(composite);

		model.addListener(new IValueChangeListener() {

			@Override
			public void valueChanged() {
				updateData();
			}
		});

		updateData();
	}

	private void createFilteringPart(Composite parent) {
		Composite filterComposite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		filterComposite.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout(2, false);
		filterComposite.setLayout(gridLayout);

		groupFilterLabel = new Label(filterComposite, SWT.NONE);
		groupFilterLabel.setText(Messages.SelectRulesWizardPage_filterByGroup);

		groupFilterCombo = new Combo(filterComposite, SWT.READ_ONLY);
		populateGroupFilterCombo();
		groupFilterCombo.addSelectionListener(createGroupFilterSelectionListener());
		gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
		gridData.widthHint = 200;
		groupFilterCombo.setLayoutData(gridData);

		nameFilterLabel = new Label(filterComposite, SWT.NONE);
		nameFilterLabel.setText(Messages.SelectRulesWizardPage_filterByName);

		nameFilterText = new Text(filterComposite, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		nameFilterText.setMessage(Messages.SelectRulesWizardPage_searchString);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		gridData.widthHint = 200;
		nameFilterText.setLayoutData(gridData);
		nameFilterText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text source = (Text) e.getSource();
				controler.nameFilterTextChanged(source.getText());
			}
		});
		// following doesn't work under Windows7
		nameFilterText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.detail == SWT.CANCEL) {
					Text text = (Text) e.getSource();
					text.setText(Messages.SelectRulesWizardPage_emptyString);
				}
			}
		});

		createRemoveDisabledRulesButton(filterComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		removeDisabledRulesButton.setLayoutData(gridData);
	}

	/**
	 * Set all items for the dropdown ({@link Combo}) and select All as default
	 * group
	 */
	private void populateGroupFilterCombo() {
		model.getGroups().stream().forEach((entry) -> {
			groupFilterCombo.add(entry.getGroupName());
			if (entry.equals(model.getCurrentGroupId())) {
				groupFilterCombo.select(groupFilterCombo.indexOf(entry.getGroupName()));
			}
		});
	}

	/**
	 * {@link SelectionListener} for the profile dropdown ({@link Combo}).
	 * 
	 * @return {@link SelectionListener} that reacts to changes of the selected
	 *         element.
	 */
	private SelectionListener createGroupFilterSelectionListener() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedProfileId = groupFilterCombo.getItem(groupFilterCombo.getSelectionIndex());
				if (selectedProfileId.equals(model.getCurrentGroupId())) {
					// nothing
				} else {
					controler.groupFilterComboChanged(selectedProfileId);
				}
			}
		};
	}

	/**
	 * Adds a button to select / deselect all rules.
	 * 
	 * @param parent
	 */
	private void createRemoveDisabledRulesButton(Composite parent) {
		removeDisabledRulesButton = new Button(parent, SWT.CHECK);
		removeDisabledRulesButton.setText(Messages.SelectRulesWizardPage_removeDisabledRulesButtonText);
		removeDisabledRulesButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// if button is selected, disabled rules shouldn't be shown in
				// left table view
				Button btn = (Button) e.getSource();
				model.removeDisabledPosibilities(btn.getSelection());
			}
		});
	}

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

		leftTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateData();
			}
		});

		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.addButtonClicked((IStructuredSelection) leftTreeViewer.getSelection());
			}
		});

		leftTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				controler.addButtonClicked((IStructuredSelection) leftTreeViewer.getSelection());
			}
		});

		rightTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				removeButton.setEnabled(!event.getSelection().isEmpty());
			}
		});

		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				controler.removeButtonClicked((IStructuredSelection) rightTableViewer.getSelection());
			}
		});

		rightTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				controler.removeButtonClicked((IStructuredSelection) rightTableViewer.getSelection());
			}
		});

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
		gridData.minimumHeight = 60;
		descriptionStyledText.setLayoutData(gridData);
		descriptionStyledText.setMargins(2, 2, 2, 2);
	}

	private void createTree(Composite parent) {
		leftTreeViewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		leftTreeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		leftTreeViewer.setUseHashlookup(true);

		configureTree(leftTreeViewer);
	}

	private void createTable(Composite parent) {
		rightTableViewer = new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		rightTableViewer.getControl().setLayoutData(gd);

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
				return null;
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
		table.setLabelProvider(new TableLabelProvider());
		table.setComparator(new JavaElementComparator());
		rightTableViewer.setContentProvider(new IStructuredContentProvider() {

			@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement) {
				Set<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> list = (Set<RefactoringRule<? extends AbstractASTRewriteASTVisitor>>) inputElement;
				return list.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});
	}

	/**
	 * Updates entire view with data every time something is changed in model.
	 */
	@SuppressWarnings("unchecked")
	private void updateData() {
		if (model.getNameFilter().isEmpty()) {
			leftTreeViewer.setInput(model.getPosibilities());
		} else {
			leftTreeViewer.setInput(model.filterPosibilitiesByName());
		}
		rightTableViewer.setInput(model.getSelection());

		populateDescriptionTextViewer();

		addButton.setEnabled(!leftTreeViewer.getSelection().isEmpty()
				&& selectionContainsEnabledEntry(((IStructuredSelection) leftTreeViewer.getSelection()).toList()));
		addAllButton.setEnabled(((Set<Object>) leftTreeViewer.getInput()).size() > 0);
		removeButton.setEnabled(!rightTableViewer.getSelection().isEmpty());
		removeAllButton.setEnabled(((Set<Object>) rightTableViewer.getInput()).size() > 0);
	}

	/**
	 * Sets the rule description text according to the currently selected rule
	 * or to the default text if no rule is selected.
	 */
	@SuppressWarnings("unchecked")
	private void populateDescriptionTextViewer() {
		List<Object> selection = ((IStructuredSelection) leftTreeViewer.getSelection()).toList();
		if (selection.size() == 1) {
			descriptionStyledText.setText(
					((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) selection.get(0)).getDescription());
		} else {
			descriptionStyledText.setText(Messages.SelectRulesWizardPage_defaultDescriptionText);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean selectionContainsEnabledEntry(List<Object> selection) {
		for (Object object : selection) {
			if (((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) object).isEnabled()) {
				return true;
			}
		}
		return false;
	}
}
