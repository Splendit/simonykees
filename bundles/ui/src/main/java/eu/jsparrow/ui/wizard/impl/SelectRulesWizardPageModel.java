package eu.jsparrow.ui.wizard.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.Tag;

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

	public SelectRulesWizardPageModel(List<RefactoringRule> rules) {
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
	@Override
	public Set<Object> filterPosibilitiesByName() {
		return super.getPosibilities().stream()
			.filter(object -> StringUtils.contains(StringUtils
				.lowerCase(((RefactoringRule) object).getRuleDescription().getName()), nameFilter))
			.collect(Collectors.toSet());
	}

	@Override
	public void filterPosibilitiesByTags() {
		if (!appliedTags.isEmpty()) {
			Set<Object> currentPossibilities = getPosibilities();
			setPosibilitiesFilteredByTag(currentPossibilities.stream()
				.filter(object -> containsTag((RefactoringRule) object))
				.collect(Collectors.toSet()));
		} else {
			addAllItems(getPosibilities());
		}
	}

	private boolean containsTag(RefactoringRule object) {
		for (String tag : appliedTags) {
			if (null != Tag.getTagForName(tag)) {
				if (object.getRuleDescription()
					.getTags()
					.contains(Tag.getTagForName(tag))) {
					return true;
				}
			} else if (StringUtils.contains(StringUtils
				.lowerCase(object.getRuleDescription().getName()), tag)) {
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
	@Override
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
