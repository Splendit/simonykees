package at.splendit.simonykees.core.ui.wizard.semiautomatic;

import java.util.HashSet;
import java.util.Set;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.wizard.IValueChangeListener;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

public class LoggerRuleWizardPageModel {

	private RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule;
	
	Set<IValueChangeListener> listeners = new HashSet<>();

	public LoggerRuleWizardPageModel(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		this.rule = rule;
	}

	/**
	 * Adds listener to model which notifies view to refresh data when ever
	 * something in model changes
	 */
	public void addListener(IValueChangeListener listener) {
		listeners.add(listener);
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
	
}
