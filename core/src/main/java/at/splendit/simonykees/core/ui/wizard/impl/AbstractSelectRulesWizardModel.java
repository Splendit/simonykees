package at.splendit.simonykees.core.ui.wizard.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.IStructuredSelection;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceManager;
import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.core.ui.wizard.IWizardPageModel;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Model that is storing all the data required for
 * {@link AbstractSelectRulesWizardPage}
 * 
 * @author Andreja Sambolec
 * @since 1.3
 */
public abstract class AbstractSelectRulesWizardModel implements IWizardPageModel {

	private Set<Object> allPosibilities = new HashSet<>();

	private Set<Object> posibilities = new HashSet<>();
	private Set<Object> selection = new HashSet<>();

	private String currentProfileId = ""; //$NON-NLS-1$

	private final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules;

	Set<IValueChangeListener> listeners = new HashSet<>();

	// flag if model is changed or is just selection change
	private boolean changed = false;
	private boolean forced = false;

	private boolean removeDisabled = false;

	public AbstractSelectRulesWizardModel(List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) {
		this.rules = rules;
		addAllItems(allPosibilities);
		addAllItems(posibilities);
		changed = true;
	}

	/**
	 * Adds listener to model which notifies view to refresh data when ever
	 * something in model changes
	 */
	public void addListener(IValueChangeListener listener) {
		listeners.add(listener);
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
	 * Getter for all values that can be shown in left view
	 * 
	 * @return Set containing all possible values for rules
	 */
	public Set<Object> getAllPosibilities() {
		return allPosibilities;
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
	@SuppressWarnings("unchecked")
	public void moveToRight(IStructuredSelection selectedElements) {
		for (Object posibility : selectedElements.toList()) {
			if (((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) posibility).isEnabled()) {
				selection.add(posibility);
				posibilities.remove(posibility);
				changed = true;
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
				changed = true;
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

		posibilities.addAll(selectedElements.toList());

		changed = true;
		notifyListeners();
	}

	/**
	 * Method called when Remove all button was clicked. Adds all elements that
	 * are enabled from right to left and removes them from right view. Ignores
	 * disabled elements.
	 */
	public void moveAllToLeft() {

		posibilities.addAll(selection);

		selection.clear();

		changed = true;
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
	 * Used by filter if group All is chosen. Accepts Set in which adds all
	 * elements from all groups.
	 * 
	 * @param applicable
	 *            Set to fill with all possible elements from all groups
	 */
	@SuppressWarnings("unchecked")
	protected void addAllItems(final Set<Object> applicable) {
		applicable.addAll(rules);
		if (removeDisabled) {
			Set<Object> currentPosibilities = new HashSet<>();
			currentPosibilities.addAll(posibilities);
			for (Object posibility : currentPosibilities) {
				if (!((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) posibility).isEnabled()) {
					applicable.remove(posibility);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void removeDisabledPosibilities(boolean doit) {
		removeDisabled = doit;
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
			posibilities.addAll(rules);

		}
		setChanged(false);
		notifyListeners();
	}

	@SuppressWarnings("unchecked")
	public List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getSelectionAsList() {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = selection.stream()
				.map(object -> (RefactoringRule<? extends AbstractASTRewriteASTVisitor>) object)
				.collect(Collectors.toList());
		Collections.sort(rules, new Comparator<RefactoringRule<? extends AbstractASTRewriteASTVisitor>>() {
			@Override
			public int compare(RefactoringRule<? extends AbstractASTRewriteASTVisitor> o1,
					RefactoringRule<? extends AbstractASTRewriteASTVisitor> o2) {
				return Integer.compare(indexOfRuleInSortedList(o1), indexOfRuleInSortedList(o2));
			}
		});
		return rules;

	}

	private int indexOfRuleInSortedList(RefactoringRule<? extends AbstractASTRewriteASTVisitor> searchedRule) {
		final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> sortedRules = RulesContainer.getAllRules();
		for (int i = 0; i < sortedRules.size(); i++) {
			if (sortedRules.get(i).getName().equals(searchedRule.getName())) {
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

	public abstract String getNameFilter();

	public abstract Set<Object> filterPosibilitiesByName();

	public abstract void filterPosibilitiesByTags();

	public void setPosibilitiesFilteredByTag(Set<Object> filteredPosibilities) {
		posibilities.clear();
		posibilities.addAll(filteredPosibilities);
	}

	@SuppressWarnings("unchecked")
	public void selectFromProfile(final String profileId) {
		currentProfileId = profileId;
		moveAllToLeft();
		if (!currentProfileId.isEmpty()) {
			Set<Object> currentPosibilities = new HashSet<>();
			currentPosibilities.addAll(posibilities);
			for (Object posibility : currentPosibilities) {
				if (SimonykeesPreferenceManager.getProfileFromName(currentProfileId).containsRule(// SimonykeesPreferenceManager.isRuleSelectedInProfile(
						// SimonykeesPreferenceManager.getAllProfileNamesAndIdsMap().get(profileId),
						((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) posibility).getId())) {
					if (((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) posibility).isEnabled()) {
						selection.add(posibility);
						posibilities.remove(posibility);
					}
				}
			}
		}

		setChanged(true);
		notifyListeners();

	}

	public void removeAlreadySelected() {
		Set<Object> currentPosibilities = new HashSet<>();
		currentPosibilities.addAll(posibilities);
		for (Object posibility : currentPosibilities) {
			if (selection.contains(posibility)) {
				posibilities.remove(posibility);
			}
		}
	}

}
