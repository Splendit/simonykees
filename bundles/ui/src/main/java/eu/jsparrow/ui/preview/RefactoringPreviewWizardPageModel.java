package eu.jsparrow.ui.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.rules.common.RefactoringRule;

public abstract class RefactoringPreviewWizardPageModel extends WizardPage {
	ICompilationUnit currentCompilationUnit;
	Map<ICompilationUnit, DocumentChange> changesForRule;
	RefactoringRule rule;

	/*
	 * map that contains all names of working copies and working copies that
	 * were unselected for this page
	 */
	Map<String, ICompilationUnit> unselected = new HashMap<>();

	/*
	 * map that contains working copies that are unselected in one iteration
	 * when this page is active
	 */
	List<ICompilationUnit> unselectedChange = new ArrayList<>();

	protected RefactoringPreviewWizardPageModel(String pageName) {
		super(pageName);
	}
}