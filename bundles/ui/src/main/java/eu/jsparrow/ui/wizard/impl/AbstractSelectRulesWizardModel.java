package eu.jsparrow.ui.wizard.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.IStructuredSelection;

import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.ui.preference.profile.SimonykeesProfile;
import eu.jsparrow.ui.wizard.IValueChangeListener;
import eu.jsparrow.ui.wizard.IWizardPageModel;

/**
 * Model that is storing all the data required for
 * {@link AbstractSelectRulesWizardPage}
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 1.3
 */
public abstract class AbstractSelectRulesWizardModel implements IWizardPageModel {

	private Set<Object> allPossibilities = new HashSet<>();

	private Set<Object> possibilities = new HashSet<>();
	private Set<Object> selection = new HashSet<>();
	private Set<Object> unapplicableRules = new HashSet<>();

	private Set<Object> recentlyMoved = new HashSet<>();

	private String currentProfileId = Messages.SelectRulesWizardPage_EmptyProfileLabel;

	private final List<RefactoringRule> rules;

	Set<IValueChangeListener> listeners = new HashSet<>();

	// flag if model is changed or is just selection change
	private boolean changed = false;
	private boolean forced = false;

	private boolean removeDisabled = false;

	/*
	 * flag for making selection when moved from one side to other, true for
	 * moving from left to right, false otherwise
	 */
	private boolean movedToRight = false;

	public AbstractSelectRulesWizardModel(List<RefactoringRule> rules) {
		this.rules = rules;
		addAllItems(allPossibilities);
		addAllItems(possibilities);
		changed = true;
	}

	/**
	 * Adds listener to model which notifies view to refresh data when ever
	 * something in model changes
	 */
	@Override
	public void addListener(IValueChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Getter for all values that should be shown in left view
	 * 
	 * @return Set containing all values for left view
	 */
	public Set<Object> getPossibilities() {
		return possibilities;
	}

	/**
	 * Getter for all values that can be shown in left view
	 * 
	 * @return Set containing all possible values for rules
	 */
	public Set<Object> getAllPossibilities() {
		return allPossibilities;
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
	 * Getter for currently selected profile in combo view
	 * 
	 * @return String id of currently selected profile in combo
	 */
	public String getCurrentProfileId() {
		return currentProfileId;
	}

	/**
	 * Method called when Add button was clicked. Accepts all elements that were
	 * selected in left view to be added in right view. Adds all elements that
	 * are enabled from left to right and removes them from left view. Ignores
	 * disabled elements.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void moveToRight(IStructuredSelection selectedElements) {
		selectedElements.toList()
			.stream()
			.filter(posibility -> ((RefactoringRule) posibility).isEnabled())
			.forEach(posibility -> {
				selection.add(posibility);
				possibilities.remove(posibility);
				recentlyMoved.add(posibility);
				changed = true;
			});
		movedToRight = true;
		notifyListeners();
	}

	/**
	 * Method called when Add all button was clicked. Adds all elements that are
	 * enabled from left to right and removes them from left view. Ignores
	 * disabled elements.
	 */
	@Override
	public void moveAllToRight() {
		Set<Object> currentPosibilities = filterPosibilitiesByName();
		currentPosibilities.stream()
			.filter(posibility -> ((RefactoringRule) posibility).isEnabled())
			.forEach(posibility -> {
				selection.add(posibility);
				possibilities.remove(posibility);
				changed = true;
			});
		notifyListeners();
	}

	/**
	 * Method called when Remove button was clicked. Accepts all elements that
	 * were selected in right view to be removed from it. Adds all elements that
	 * are enabled from right to left and removes them from right view. Ignores
	 * disabled elements.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void moveToLeft(IStructuredSelection selectedElements) {

		selection.removeAll(selectedElements.toList());

		possibilities.addAll(selectedElements.toList());
		recentlyMoved.addAll(selectedElements.toList());

		changed = true;
		movedToRight = false;
		notifyListeners();
	}

	/**
	 * Method called when Remove all button was clicked. Adds all elements that
	 * are enabled from right to left and removes them from right view. Ignores
	 * disabled elements.
	 */
	@Override
	public void moveAllToLeft() {

		possibilities.addAll(selection);

		selection.clear();

		changed = true;
		notifyListeners();
	}

	/**
	 * Called from every method in model that changes anything in model.
	 * Notifies view to redraw all elements with new data.
	 */
	public void notifyListeners() {
		listeners.forEach(IValueChangeListener::valueChanged);
	}

	/**
	 * Used by filter if group All is chosen. Accepts Set in which adds all
	 * elements from all groups.
	 * 
	 * @param applicable
	 *            Set to fill with all possible elements from all groups
	 */
	protected void addAllItems(final Set<Object> applicable) {
		applicable.addAll(rules);
		if (removeDisabled) {
			Set<Object> currentPosibilities = new HashSet<>();
			currentPosibilities.addAll(possibilities);
			currentPosibilities.stream()
				.filter(posibility -> !((RefactoringRule) posibility).isEnabled())
				.forEach(applicable::remove);
		}
	}

	public void removeDisabledPosibilities(boolean doit) {
		removeDisabled = doit;
		if (doit) {
			Set<Object> currentPosibilities = new HashSet<>();
			currentPosibilities.addAll(possibilities);
			currentPosibilities.stream()
				.filter(posibility -> !((RefactoringRule) posibility).isEnabled())
				.forEach(possibilities::remove);
		} else {
			possibilities.clear();
			possibilities.addAll(rules);

		}
		setChanged(false);
		notifyListeners();
	}

	public List<RefactoringRule> getSelectionAsList() {
		List<RefactoringRule> rulesList = selection.stream()
			.map(object -> (RefactoringRule) object)
			.collect(Collectors.toList());
		Collections.sort(rulesList,
				(o1, o2) -> Integer.compare(indexOfRuleInSortedList(o1), indexOfRuleInSortedList(o2)));
		return rulesList;

	}

	public boolean selectionContainsNonFreemiumRules() {
		return selection.stream()
			.map(object -> (RefactoringRule) object)
			.anyMatch(rule -> !rule.isFree());
	}

	private int indexOfRuleInSortedList(RefactoringRule searchedRule) {
		final List<RefactoringRule> sortedRules = RulesContainer.getAllRules(false);
		for (int i = 0; i < sortedRules.size(); i++) {
			if (sortedRules.get(i)
				.getRuleDescription()
				.getName()
				.equals(searchedRule.getRuleDescription()
					.getName())) {
				return i;
			}
		}
		return -1;
	}

	public boolean hasChanged() {
		return changed;
	}

	public void resetChanged() {
		this.changed = false;
	}

	public boolean isForced() {
		return forced;
	}

	public void resetForced() {
		this.forced = false;
	}

	public void setChanged(boolean forced) {
		this.changed = true;
		this.forced = forced;
	}

	public boolean isMovedToRight() {
		return movedToRight;
	}

	public Set<Object> getRecentlyMoved() {
		return recentlyMoved;
	}

	public Set<Object> getUnapplicableRules() {
		return unapplicableRules;
	}

	public abstract String getNameFilter();

	public abstract Set<Object> filterPosibilitiesByName();

	public abstract void filterPosibilitiesByTags();

	public void setPosibilitiesFilteredByTag(Set<Object> filteredPosibilities) {
		possibilities.clear();
		possibilities.addAll(filteredPosibilities);
	}

	public void selectFromProfile(final String profileId) {
		currentProfileId = profileId;
		moveAllToLeft();
		unapplicableRules.clear();
		
		if (!currentProfileId.equals(Messages.SelectRulesWizardPage_EmptyProfileLabel)
				&& !StringUtils.isEmpty(currentProfileId)) {
			findProfileByName().ifPresent(this::selectFromProfile);
		}

		setChanged(true);
		notifyListeners();
	}

	private void selectFromProfile(SimonykeesProfile profile) {
		Set<Object> currentPosibilities = new HashSet<>();
		currentPosibilities.addAll(possibilities);
		currentPosibilities.stream()
			.filter(posibility -> profile.containsRule(// SimonykeesPreferenceManager.isRuleSelectedInProfile(
					// SimonykeesPreferenceManager.getAllProfileNamesAndIdsMap().get(profileId),
					((RefactoringRule) posibility).getId()))
			.forEach(posibility -> {
				if (((RefactoringRule) posibility).isEnabled()) {
					selection.add(posibility);
					possibilities.remove(posibility);
				} else {
					unapplicableRules.add(posibility);
				}
			});
	}

	protected Optional<SimonykeesProfile> findProfileByName() {
		return SimonykeesPreferenceManager
			.getProfileFromName(currentProfileId);
	}

	public void removeAlreadySelected() {
		Set<Object> currentPosibilities = new HashSet<>();
		currentPosibilities.addAll(possibilities);
		currentPosibilities.stream()
			.filter(selection::contains)
			.forEach(possibilities::remove);
	}

}
