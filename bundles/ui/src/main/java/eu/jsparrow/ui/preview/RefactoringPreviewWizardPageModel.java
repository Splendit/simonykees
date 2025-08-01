package eu.jsparrow.ui.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.rules.common.RefactoringRule;

/**
 * Model class intended to be used by wizard pages of re-factoring preview
 * wizards which show the Issues found in a Java file.
 * 
 * @since 4.15.0
 */
public class RefactoringPreviewWizardPageModel {
	private ICompilationUnit currentCompilationUnit;
	private Map<ICompilationUnit, DocumentChange> changesForRule;
	private RefactoringRule rule;

	/*
	 * map that contains all names of working copies and working copies that
	 * were unselected for this page
	 */
	private Map<String, ICompilationUnit> unselected = new HashMap<>();

	/*
	 * map that contains working copies that are unselected in one iteration
	 * when this page is active
	 */
	private List<ICompilationUnit> unselectedChange = new ArrayList<>();

	/**
	 * Returns the class name of an {@link ICompilationUnit}, including ".java"
	 * 
	 * @param compilationUnit
	 * @return
	 */
	String getClassNameString(ICompilationUnit compilationUnit) {
		return compilationUnit.getElementName();
	}

	/**
	 * Returns the path of an {@link ICompilationUnit} without leading slash
	 * (the same as in the Externalize Strings refactoring view).
	 * 
	 * @param compilationUnit
	 * @return
	 */
	String getPathString(ICompilationUnit compilationUnit) {
		String temp = compilationUnit.getParent()
			.getPath()
			.toString();
		return StringUtils.startsWith(temp, "/") ? StringUtils.substring(temp, 1) : temp; //$NON-NLS-1$
	}

	public List<ICompilationUnit> getUnselectedChange() {
		return unselectedChange;
	}

	/**
	 * When page is no more in focus, all changes are already stored and
	 * calculated, so unselected changes go to unselected map and cleans
	 * unselectedChanges list.
	 */
	public void applyUnselectedChange() {
		unselectedChange.stream()
			.forEach(unit -> getUnselected().put(unit.getElementName(), unit));
		unselectedChange.clear();
	}

	public RefactoringRule getRule() {
		return rule;
	}

	/**
	 * Updates changes for this page. IF there were changes in currently
	 * displayed working copy, it needs to be updated too.
	 * 
	 * @param changesForRule
	 */
	public void update(Map<ICompilationUnit, DocumentChange> changesForRule) {
		this.setChangesForRule(changesForRule);
		changesForRule.keySet()
			.stream()
			.filter(unit -> unit.getElementName()
				.equals(currentCompilationUnit.getElementName()) && !unit.equals(currentCompilationUnit))
			.forEach(unit -> currentCompilationUnit = unit);
	}

	ICompilationUnit getCurrentCompilationUnit() {
		return currentCompilationUnit;
	}

	void setCurrentCompilationUnit(ICompilationUnit currentCompilationUnit) {
		this.currentCompilationUnit = currentCompilationUnit;
	}

	public Map<ICompilationUnit, DocumentChange> getChangesForRule() {
		return changesForRule;
	}

	public void setChangesForRule(Map<ICompilationUnit, DocumentChange> changesForRule) {
		this.changesForRule = changesForRule;
	}

	public void setRule(RefactoringRule rule) {
		this.rule = rule;
	}

	public Map<String, ICompilationUnit> getUnselected() {
		return unselected;
	}

	public void setUnselected(Map<String, ICompilationUnit> unselected) {
		this.unselected = unselected;
	}
}