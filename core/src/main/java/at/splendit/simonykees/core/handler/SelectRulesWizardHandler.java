package at.splendit.simonykees.core.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import at.splendit.simonykees.core.ui.SelectRulesWizard;

/**
 * TODO SIM-103 add class description
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa
 * @since 0.9
 * 
 */
public class SelectRulesWizardHandler extends AbstractSimonykeesHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), new SelectRulesWizard(getSelectedJavaElements(event)));
		
		// the dialog is made as small as possible horizontally (we want line breaks for rule descriptions)
		dialog.setPageSize(1, 200); 
		
		dialog.open();
		return null;
	}

}
