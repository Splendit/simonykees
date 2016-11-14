package at.splendit.simonykees.core.ui.preference;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;

/**
 * @author Ludwig Werzowa
 * @since 0.9.2
 */
public class SimonykeesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private String[][] profileNamesAndValues = {
			{ SimonykeesPreferenceConstants.DEFAULT_PROFILE, SimonykeesPreferenceConstants.DEFAULT_PROFILE },
			{ SimonykeesPreferenceConstants.JAVA8_PROFILE, SimonykeesPreferenceConstants.JAVA8_PROFILE } };

	private ComboFieldEditor profileSelectionComboField;

	public SimonykeesPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		profileSelectionComboField = new ComboFieldEditor("profileSelection", "Select profile", profileNamesAndValues,
				composite);
		addField(profileSelectionComboField);

		List<RefactoringRule<? extends ASTVisitor>> rules = RulesContainer.getAllRules();
		for (RefactoringRule<? extends ASTVisitor> refactoringRule : rules) {
			addField(new BooleanFieldEditor(refactoringRule.getId(), refactoringRule.getName(), composite));
		}
		
	}
	
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
