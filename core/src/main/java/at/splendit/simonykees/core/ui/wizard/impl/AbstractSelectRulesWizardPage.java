package at.splendit.simonykees.core.ui.wizard.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
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
@SuppressWarnings("restriction") // StatusInfo is internal
public abstract class AbstractSelectRulesWizardPage extends NewElementWizardPage {

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

		model.addListener(new IValueChangeListener() {

			@Override
			public void valueChanged() {
				updateData();
			}
		});

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

		leftTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (forcedSelectLeft) {
					forcedSelectLeft = false;
					/*
					 * if it is manually selected because of moving, don't
					 * update view
					 */
				} else {
					controler.selectionChanged();
				}
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
		table.setLabelProvider(new TableLabelProvider());
		table.setComparator(new ViewerComparator() {
			@SuppressWarnings("unchecked")
			public int compare(Viewer viewer, Object e1, Object e2) {
				RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule1 = (RefactoringRule<? extends AbstractASTRewriteASTVisitor>) e1;
				RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule2 = (RefactoringRule<? extends AbstractASTRewriteASTVisitor>) e2;
				return rule1.getName().compareTo(rule2.getName());
			};

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
	}

	/**
	 * Updates entire view with data every time something is changed in model.
	 */
	@SuppressWarnings("unchecked")
	private void updateData() {
		/*
		 * check if model has changed to update table and tree view or is just
		 * selection changed to update description field and buttons
		 */
		if (model.hasChanged()) {
			if (!model.isForced()) {
				model.filterPosibilitiesByTags();
				model.removeAlreadySelected();
				if (model.getNameFilter().isEmpty()) {
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
			if (!model.getRecentlyMoved().isEmpty()) {
				if (model.isMovedToRight()) {
					rightTableViewer.setSelection(new StructuredSelection(model.getRecentlyMoved().toArray()), false);
				} else {
					forcedSelectLeft = true;
					leftTreeViewer.setSelection(new StructuredSelection(model.getRecentlyMoved().toArray()), false);
				}
				model.getRecentlyMoved().clear();
			}
			getContainer().updateButtons();
			model.resetChanged();
		}
		populateDescriptionTextViewer();

		addButton.setEnabled(!leftTreeViewer.getSelection().isEmpty()
				&& selectionContainsEnabledEntry(((IStructuredSelection) leftTreeViewer.getSelection()).toList()));
		addAllButton.setEnabled(((Set<Object>) leftTreeViewer.getInput()).size() > 0);
		removeButton.setEnabled(!rightTableViewer.getSelection().isEmpty());
		removeAllButton.setEnabled(((Set<Object>) rightTableViewer.getInput()).size() > 0);

		doStatusUpdate();
	}

	/**
	 * Sets the rule description text according to the currently selected rule
	 * or to the default text if no rule is selected.
	 */
	@SuppressWarnings("unchecked")
	private void populateDescriptionTextViewer() {
		List<Object> selection = ((IStructuredSelection) leftTreeViewer.getSelection()).toList();
		if (selection.size() == 1) {
			// descriptionStyledText.setText(
			// ((RefactoringRule<? extends AbstractASTRewriteASTVisitor>)
			// selection.get(0)).getDescription());
			createTextForDescription((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) selection.get(0));
		} else {
			descriptionStyledText.setText(Messages.SelectRulesWizardPage_defaultDescriptionText);
		}
	}

	/**
	 * Creating description for rule to be displayed using StyledText
	 * 
	 * @param rule
	 */
	private void createTextForDescription(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		String lineDelimiter = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_lineDelimiter;
		String name = rule.getName();
		String description = rule.getDescription();
		String requirementsLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_requirementsLabel;
		String minJavaVersionLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_minJavaVersionLabel;
		String minJavaVersionValue = rule.getRequiredJavaVersion().toString();
		String requiredLibrariesLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_librariesLabel;
		String requiredLibrariesValue = (null != rule.requiredLibraries()) ? rule.requiredLibraries()
				: Messages.AbstractSelectRulesWizardPage_descriptionStyledText_librariesNoneLabel;
		String tagsLabel = Messages.AbstractSelectRulesWizardPage_descriptionStyledText_tagsLabel;
		String tagsValue = StringUtils
				.join(rule.getTags().stream().map(tag -> tag.getTagNames()).collect(Collectors.toList()), "  "); //$NON-NLS-1$

		String descriptionText = name + lineDelimiter + lineDelimiter + description + lineDelimiter + lineDelimiter
				+ requirementsLabel + lineDelimiter + minJavaVersionLabel + minJavaVersionValue + lineDelimiter
				+ requiredLibrariesLabel + requiredLibrariesValue + lineDelimiter + lineDelimiter + tagsLabel
				+ lineDelimiter + tagsValue;

		FontData data = descriptionStyledText.getFont().getFontData()[0];
		Font ruleName = new Font(getShell().getDisplay(), data.getName(), data.getHeight() * 3 / 2, data.getStyle());
		Font paragraphTitle = new Font(getShell().getDisplay(), data.getName(), data.getHeight(), SWT.BOLD);
		Font normalTitle = new Font(getShell().getDisplay(), data.getName(), data.getHeight(), data.getStyle());
		Color unsetisfiedRequirementsColor = getShell().getDisplay().getSystemColor(SWT.COLOR_RED);

		StyleRange ruleNameStyleRange = new StyleRange();
		ruleNameStyleRange.start = 0;
		ruleNameStyleRange.length = name.length();
		ruleNameStyleRange.font = ruleName;

		StyleRange requirementsLabelStyleRange = new StyleRange();
		requirementsLabelStyleRange.start = name.length() + lineDelimiter.length() + lineDelimiter.length()
				+ description.length() + lineDelimiter.length() + lineDelimiter.length();
		requirementsLabelStyleRange.length = requirementsLabel.length();
		requirementsLabelStyleRange.font = paragraphTitle;

		StyleRange minJavaVersionLabelStyleRange = new StyleRange();
		minJavaVersionLabelStyleRange.start = name.length() + lineDelimiter.length() + lineDelimiter.length()
				+ description.length() + lineDelimiter.length() + lineDelimiter.length() + requirementsLabel.length()
				+ lineDelimiter.length();
		minJavaVersionLabelStyleRange.length = minJavaVersionLabel.length();
		minJavaVersionLabelStyleRange.font = normalTitle;

		StyleRange requiredLibrariesLabelStyleRange = new StyleRange();
		requiredLibrariesLabelStyleRange.start = name.length() + lineDelimiter.length() + lineDelimiter.length()
				+ description.length() + lineDelimiter.length() + lineDelimiter.length() + requirementsLabel.length()
				+ lineDelimiter.length() + minJavaVersionLabel.length() + minJavaVersionValue.length()
				+ lineDelimiter.length();
		requiredLibrariesLabelStyleRange.length = requiredLibrariesLabel.length();
		requiredLibrariesLabelStyleRange.font = normalTitle;

		StyleRange tagsLabelStyleRange = new StyleRange();
		tagsLabelStyleRange.start = name.length() + lineDelimiter.length() + lineDelimiter.length()
				+ description.length() + lineDelimiter.length() + lineDelimiter.length() + requirementsLabel.length()
				+ lineDelimiter.length() + minJavaVersionLabel.length() + minJavaVersionValue.length()
				+ lineDelimiter.length() + requiredLibrariesLabel.length() + requiredLibrariesValue.length()
				+ lineDelimiter.length() + lineDelimiter.length();
		tagsLabelStyleRange.length = tagsLabel.length();
		tagsLabelStyleRange.font = paragraphTitle;

		StyleRange style0 = new StyleRange();
		style0.metrics = new GlyphMetrics(0, 0, 40);
		style0.foreground = getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK);
		Bullet bullet0 = new Bullet(style0);

		descriptionStyledText.setText(descriptionText);
		descriptionStyledText.setStyleRange(ruleNameStyleRange);
		descriptionStyledText.setStyleRange(requirementsLabelStyleRange);
		descriptionStyledText.setStyleRange(minJavaVersionLabelStyleRange);
		descriptionStyledText.setStyleRange(requiredLibrariesLabelStyleRange);
		descriptionStyledText.setStyleRange(tagsLabelStyleRange);

		if (!rule.isSatisfiedJavaVersion()) {
			StyleRange minJavaVersionUnsatisfiedValueStyleRange = new StyleRange();
			minJavaVersionUnsatisfiedValueStyleRange.start = name.length() + lineDelimiter.length()
					+ lineDelimiter.length() + description.length() + lineDelimiter.length() + lineDelimiter.length()
					+ requirementsLabel.length() + lineDelimiter.length() + minJavaVersionLabel.length();
			minJavaVersionUnsatisfiedValueStyleRange.length = minJavaVersionValue.length();
			minJavaVersionUnsatisfiedValueStyleRange.font = paragraphTitle;
			minJavaVersionUnsatisfiedValueStyleRange.foreground = unsetisfiedRequirementsColor;
			descriptionStyledText.setStyleRange(minJavaVersionUnsatisfiedValueStyleRange);
		}
		if (!rule.isSatisfiedLibraries()) {
			StyleRange requiredLibrariesUnsatisfiedValueStyleRange = new StyleRange();
			requiredLibrariesUnsatisfiedValueStyleRange.start = name.length() + lineDelimiter.length()
					+ lineDelimiter.length() + description.length() + lineDelimiter.length() + lineDelimiter.length()
					+ requirementsLabel.length() + lineDelimiter.length() + minJavaVersionLabel.length()
					+ minJavaVersionValue.length() + lineDelimiter.length() + requiredLibrariesLabel.length();
			requiredLibrariesUnsatisfiedValueStyleRange.length = requiredLibrariesValue.length();
			requiredLibrariesUnsatisfiedValueStyleRange.font = paragraphTitle;
			requiredLibrariesUnsatisfiedValueStyleRange.foreground = unsetisfiedRequirementsColor;
			descriptionStyledText.setStyleRange(requiredLibrariesUnsatisfiedValueStyleRange);
		}

		int requirementsBulletingStartLine = descriptionStyledText.getLineAtOffset(name.length()
				+ lineDelimiter.length() + lineDelimiter.length() + description.length() + lineDelimiter.length()
				+ lineDelimiter.length() + requirementsLabel.length() + lineDelimiter.length());

		descriptionStyledText.setLineBullet(requirementsBulletingStartLine, 2, bullet0);
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

	public void recalculateLayout() {
		composite.layout(true, true);
	}

	protected abstract void doStatusUpdate();

	protected void doStatusUpdate(IStatus additionalStatus) {
		if (model.getSelectionAsList().isEmpty()) {
			((StatusInfo) fSelectionStatus).setError(Messages.AbstractSelectRulesWizardPage_error_NoRulesSelected);
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
}
