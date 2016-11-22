package at.splendit.simonykees.core.ui.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Main preference page for the plug-in. {@link FieldEditor}s are used for
 * convenient preference handling.
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class SimonykeesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private ComboFieldEditor profileSelectionComboField;
	private List<BooleanFieldEditor> booleanFieldEditors = new ArrayList<>();

	private String currentProfileId;

	public SimonykeesPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		loadCurrentProfileId();

		profileSelectionComboField = new ComboFieldEditor(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT,
				Messages.SimonykeesPreferencePage_selectProfile, SimonykeesPreferenceManager.getProfileNamesAndValues(),
				composite);
		addField(profileSelectionComboField);

		generateRulesCheckboxList(composite);

	}

	private String loadCurrentProfileId() {
		return currentProfileId = getPreferenceStore().getString(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT);
	}

	private void generateRulesCheckboxList(Composite composite) {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules();
		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> refactoringRule : rules) {

			BooleanFieldEditor editor = new BooleanFieldEditor(
					SimonykeesPreferenceManager.getProfileRuleKey(currentProfileId, refactoringRule.getId()),
					refactoringRule.getName(), composite);
			booleanFieldEditors.add(editor);
			addField(editor);
		}
	}

	private void updateRulesCheckboxList(String profileId) {
		for (BooleanFieldEditor editor : booleanFieldEditors) {
			String newPreferenceName = editor.getPreferenceName().replace(currentProfileId, profileId);
			editor.setPreferenceName(newPreferenceName);
			editor.load();
		}
		currentProfileId = profileId;
	}

	/**
	 * Eclipse doesn't like normal property change listeners for some reason.
	 * Adding a change listener directly to the combo field never fires an
	 * event. See: http://stackoverflow.com/a/11361344
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource() == profileSelectionComboField) {
			updateRulesCheckboxList(event.getNewValue().toString());
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		// we have to override this method, even though we don't need it.
	}

}
