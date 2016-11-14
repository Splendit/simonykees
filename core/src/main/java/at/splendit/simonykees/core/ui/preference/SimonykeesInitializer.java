package at.splendit.simonykees.core.ui.preference;

import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.preference.IPreferenceStore;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;

/**
 * 
 * @author Ludwig Werzowa
 * @since 0.9.2
 */
public class SimonykeesInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		preferenceStore.setDefault(SimonykeesPreferenceConstants.DEFAULT_PROFILE, "Default");
		preferenceStore.setDefault(SimonykeesPreferenceConstants.JAVA8_PROFILE, "Java 8");
		
		List<RefactoringRule<? extends ASTVisitor>> rules = RulesContainer.getAllRules();
		for (RefactoringRule<? extends ASTVisitor> refactoringRule : rules) {
			preferenceStore.setDefault(refactoringRule.getId(), refactoringRule.isDefaultRule());
		}
	}

}
