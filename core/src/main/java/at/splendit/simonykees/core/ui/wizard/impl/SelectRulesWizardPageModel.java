package at.splendit.simonykees.core.ui.wizard.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.IStructuredSelection;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.core.ui.wizard.IWizardPageModel;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Model that is storing all the data required for {@link AbstractSelectRulesWizardPage}
 * 
 * @author Andreja Sambolec
 * @since 1.3
 */
public class SelectRulesWizardPageModel implements IWizardPageModel {

	private Set<Object> posibilities = new HashSet<>();
	private Set<Object> selection = new HashSet<>();

	private static GroupEnum currentGroupId = GroupEnum.EMPTY;

	private String nameFilter = ""; //$NON-NLS-1$

	private List<GroupEnum> groups;
	private final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules;


	Set<IValueChangeListener> listeners = new HashSet<>();

	public SelectRulesWizardPageModel(List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) {

		groups = Arrays.asList(GroupEnum.values());
		this.rules = rules;

		addAllItems(posibilities);
	}

	/**
	 * Adds listener to model which notifies view to refresh data when ever
	 * something in model changes
	 */
	public void addListener(IValueChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Getter for current group id set in the filter by group combo
	 * 
	 * @return String value of current group id in combo
	 */
	public GroupEnum getCurrentGroupId() {
		return currentGroupId;
	}

	/**
	 * Getter for all values that should be shown in left view
	 * 
	 * @return Set containing all values for left view
	 */
	public Set<Object> getPosibilities() {
		return posibilities;
	}

	/**
	 * Getter for all values that should be shown in right view
	 * 
	 * @return Set containing all values for right view
	 */
	public Set<Object> getSelection() {
		return selection;
	}

	/**
	 * Method called when Add button was clicked. Accepts all elements that were
	 * selected in left view to be added in right view. Adds all elements that
	 * are enabled from left to right and removes them from left view. Ignores
	 * disabled elements.
	 */
	@SuppressWarnings("unchecked")
	public void moveToRight(IStructuredSelection selectedElements) {
		for (Object posibility : selectedElements.toList()) {
			if (((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) posibility).isEnabled()) {
				selection.add(posibility);
				posibilities.remove(posibility);
			}
		}
		notifyListeners();
	}

	/**
	 * Method called when Add all button was clicked. Adds all elements that are
	 * enabled from left to right and removes them from left view. Ignores
	 * disabled elements.
	 */
	@SuppressWarnings("unchecked")
	public void moveAllToRight() {
		Set<Object> currentPosibilities = new HashSet<>();
		currentPosibilities = filterPosibilitiesByName();
		for (Object posibility : currentPosibilities) {
			if (((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) posibility).isEnabled()) {
				selection.add(posibility);
				posibilities.remove(posibility);
			}
		}
		notifyListeners();
	}

	/**
	 * Method called when Remove button was clicked. Accepts all elements that
	 * were selected in right view to be removed from it. Adds all elements that
	 * are enabled from right to left and removes them from right view. Ignores
	 * disabled elements.
	 */
	@SuppressWarnings("unchecked")
	public void moveToLeft(IStructuredSelection selectedElements) {
		selection.removeAll(selectedElements.toList());

		if (currentGroupId.equals(GroupEnum.EMPTY)) {
			posibilities.addAll(selectedElements.toList());
		} else {
			for (Object posibility : selectedElements.toList()) {
				if (((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) posibility).getGroups()
						.contains(currentGroupId)) {
					posibilities.add(posibility);
				}
			}
		}
		notifyListeners();
	}

	/**
	 * Method called when Remove all button was clicked. Adds all elements that
	 * are enabled from right to left and removes them from right view. Ignores
	 * disabled elements.
	 */
	@SuppressWarnings("unchecked")
	public void moveAllToLeft() {
		if (currentGroupId.equals(GroupEnum.EMPTY)) {
			posibilities.addAll(selection);
		} else {
			for (Object posibility : selection) {
				if (((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) posibility).getGroups()
						.contains(currentGroupId)) {
					posibilities.add(posibility);
				}
			}
		}
		selection.clear();

		notifyListeners();
	}

	/**
	 * Called from every method in model that changes anything in model.
	 * Notifies view to redraw all elements with new data.
	 */
	public void notifyListeners() {
		for (IValueChangeListener listener : listeners) {
			listener.valueChanged();
		}
	}

	/**
	 * Used to filter all objects in left view to show only objects that are in
	 * group selected in filtering combo. By default shows group All containing
	 * all possible elements. Removes elements that are already added in right
	 * view.
	 */
	public void filterByGroup(final String filter) {
		final Set<Object> applicable = new HashSet<>();
		currentGroupId = GroupEnum.findByGroupName(filter);

		if (currentGroupId == null || currentGroupId.equals(GroupEnum.EMPTY)) {
			addAllItems(applicable);
		} else if (groups.contains(currentGroupId)) {
			applicable.addAll(rules.stream().filter(rule -> rule.getGroups().contains(currentGroupId))
					.collect(Collectors.toList()));
		}
		posibilities.clear();
		posibilities.addAll(applicable);

		removeAlreadyAdded(applicable);

		notifyListeners();
	}

	/**
	 * Used by filter if group All is chosen. Accepts Set in which adds all
	 * elements from all groups.
	 * 
	 * @param applicable
	 *            Set to fill with all possible elements from all groups
	 */
	private void addAllItems(final Set<Object> applicable) {
		applicable.addAll(rules);
	}

	/**
	 * Used by filter to remove elements from left view that are already added
	 * in right view.
	 * 
	 * @param applicable
	 */
	private void removeAlreadyAdded(final Set<Object> applicable) {
		for (final Object object : applicable) {
			if (selection.contains(object)) {
				posibilities.remove(object);
			}
		}
	}

	/**
	 * Getter for List containing all groups for filtering by group.
	 * 
	 * @return List containing all group names.
	 */
	public List<GroupEnum> getGroups() {
		return groups;
	}

	@SuppressWarnings("unchecked")
	public void removeDisabledPosibilities(boolean doit) {
		if (doit) {
			Set<Object> currentPosibilities = new HashSet<>();
			currentPosibilities.addAll(posibilities);
			for (Object posibility : currentPosibilities) {
				if (!((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) posibility).isEnabled()) {
					posibilities.remove(posibility);
				}
			}
		} else {
			posibilities.clear();
			if (currentGroupId.equals(GroupEnum.EMPTY)) {
				posibilities.addAll(rules);
			} else {
				posibilities.addAll(rules.stream().filter(rule -> rule.getGroups().contains(currentGroupId))
						.collect(Collectors.toList()));
			}
		}
		notifyListeners();
	}

	/**
	 * Used to filter all possibilities in left view by search string. Searches
	 * if element contains searched string (nameFilter) as typed.
	 * 
	 * @return Set containing searched string.
	 */
	@SuppressWarnings("unchecked")
	public Set<Object> filterPosibilitiesByName() {
		return posibilities.stream()
				.filter(object -> ((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) object).getName()
						.contains(nameFilter)
						|| ((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) object).getDescription()
								.contains(nameFilter))
				.collect(Collectors.toSet());
	}

	/**
	 * When ever Text field is changed this method is called with text from view
	 * to be set as parameter for filtering by name. Notifies listener that
	 * something is changed to refresh the view with possibilities filtered by
	 * searched string.
	 * 
	 * @param nameFilter
	 *            text entered in filter Text field by user
	 */
	public void setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;

		notifyListeners();
	}

	/**
	 * Getter for text entered in filter Text field by user
	 * 
	 * @return String for filter by name
	 */
	public String getNameFilter() {
		return nameFilter;
	}

	@SuppressWarnings("unchecked")
	public List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getSelectionAsList() {
		return selection.stream().map(object -> (RefactoringRule<? extends AbstractASTRewriteASTVisitor>) object)
				.collect(Collectors.toList());
	}
}
