package eu.jsparrow.ui.wizard;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.ui.preview.AbstractPreviewWizard;

/**
 * Intended to be used for preview wizards which are subclasses of
 * {@link AbstractPreviewWizard}.
 * 
 * @since 4.17.0
 */
public class RuleWizardDialog extends WizardDialog {

	protected RuleWizardDialog(Shell parentShell, AbstractRuleWizard newWizard) {
		super(parentShell, newWizard);
	}

}
