package at.splendit.simonykees.core.ui.wizard.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.Tag;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Model for Wizard page for selecting rules when applying rules to selected
 * resources
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class SelectRulesWizardPageModel extends AbstractSelectRulesWizardModel {

	private String nameFilter = ""; //$NON-NLS-1$

	private String[] tags;

	private final Set<String> appliedTags = new HashSet<>();

	public SelectRulesWizardPageModel(List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) {
		super(rules);

		tags = Tag.getAllTags();
	}

	/**
	 * Getter for List containing all groups for filtering by group.
	 * 
	 * @return List containing all group names.
	 * 
	 */
	public String[] getTags() {
		return tags;
	}

	public Set<String> getAppliedTags() {
		return appliedTags;
	}

	/**
	 * Used to filter all possibilities in left view by search string. Searches
	 * if element contains searched string (nameFilter) as typed.
	 * 
	 * @return Set containing searched string.
	 */
	@SuppressWarnings("unchecked")
	public Set<Object> filterPosibilitiesByName() {
		return super.getPosibilities().stream()
				.filter(object -> ((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) object).getName()
						.contains(nameFilter)
						|| ((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) object).getDescription()
								.contains(nameFilter))
				.collect(Collectors.toSet());
	}

	@SuppressWarnings("unchecked")
	public void filterPosibilitiesByTags() {
		if (!appliedTags.isEmpty()) {
			Set<Object> currentPossibilities = getPosibilities();
			setPosibilitiesFilteredByTag(currentPossibilities.stream()
					.filter(object -> containsTag((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) object))
					.collect(Collectors.toSet()));
		} else {
			addAllItems(getPosibilities());
		}
	}

	private boolean containsTag(RefactoringRule<? extends AbstractASTRewriteASTVisitor> object) {
		for (String tag : appliedTags) {
			if (object.getTags().contains(tag) || object.getName().contains(tag)
					|| object.getDescription().contains(tag)) {
				return true;
			}
		}
		return false;
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

		setChanged(false);
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

	public void addTag(String text) {
		appliedTags.add(text);

		setChanged(false);
		notifyListeners();
	}

	public void removeTag(String text) {
		appliedTags.remove(text);

		setChanged(false);
		notifyListeners();
	}

}
