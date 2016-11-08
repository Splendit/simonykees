package at.splendit.simonykees.core.ui.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Ludwig Werzowa
 * @since 0.9.2
 */
public class SimonykeesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private static final String DEFAULT_PROFILE_NAME = "Default";
	private static final String DEFAULT_PROFILE_VALUE = "default";
	
	private static final String JAVA8_PROFILE_NAME = "Java 8";
	private static final String JAVA8_PROFILE_VALUE = "java8";
	
	private String[][] profileNamesAndValues = {
				{DEFAULT_PROFILE_NAME, DEFAULT_PROFILE_VALUE},
				{JAVA8_PROFILE_NAME, JAVA8_PROFILE_VALUE}
			};
	
	private ComboFieldEditor profileSelectionComboField;
	private BooleanFieldEditor codeFormatterSelected;

	public SimonykeesPreferencePage() {
		super(GRID);
	}
	
	@Override
	protected void createFieldEditors() {
		Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		profileSelectionComboField = new ComboFieldEditor("profileSelection", "Select profile", profileNamesAndValues, composite);
		addField(profileSelectionComboField);
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

}
