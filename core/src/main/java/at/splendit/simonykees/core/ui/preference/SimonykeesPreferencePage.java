package at.splendit.simonykees.core.ui.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
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
	private List<BooleanFieldEditor> ruleCheckboxList = new ArrayList<>();

	private String currentProfileId;
	private Group ruleCheckboxGroup;

	public SimonykeesPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		loadCurrentProfileId();

		profileSelectionComboField = new ComboFieldEditor(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT,
				Messages.SimonykeesPreferencePage_selectProfile, SimonykeesPreferenceManager.getAllProfileNamesAndIds(),
				composite);
		addField(profileSelectionComboField);

		generateRuleCheckboxList(composite);

	}

	private String loadCurrentProfileId() {
		return currentProfileId = getPreferenceStore().getString(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT);
	}

	private void generateRuleCheckboxList(Composite composite) {

		ruleCheckboxGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		ruleCheckboxGroup.setLayoutData(data);
		ruleCheckboxGroup.setText(Messages.SimonykeesPreferencePage_rules);

		boolean builtInProfile = SimonykeesPreferenceManager.isProfileBuiltIn(this.currentProfileId);
		
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules();
		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> refactoringRule : rules) {

			BooleanFieldEditor ruleCheckbox = new BooleanFieldEditor(
					SimonykeesPreferenceManager.getProfileRuleKey(currentProfileId, refactoringRule.getId()),
					refactoringRule.getName(), ruleCheckboxGroup);
			ruleCheckbox.setEnabled(!builtInProfile, ruleCheckboxGroup);
			ruleCheckboxList.add(ruleCheckbox);
			addField(ruleCheckbox);
		}

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

	/**
	 * Changes the "binding" for each rule checkbox whenever another profile is
	 * selected.
	 * 
	 * @param profileId
	 *            use the given profileId for each rule checkbox
	 */
	private void updateRulesCheckboxList(String profileId) {
		boolean builtInProfile = SimonykeesPreferenceManager.isProfileBuiltIn(profileId);
		for (BooleanFieldEditor ruleCheckbox : ruleCheckboxList) {
			String newPreferenceName = ruleCheckbox.getPreferenceName().replace(currentProfileId, profileId);
			ruleCheckbox.setPreferenceName(newPreferenceName);
			ruleCheckbox.load();
			ruleCheckbox.setEnabled(!builtInProfile, ruleCheckboxGroup);
		}
		currentProfileId = profileId;
	}

	@Override
	public void init(IWorkbench workbench) {
		// we have to override this method, even though we don't need it.
	}

}
