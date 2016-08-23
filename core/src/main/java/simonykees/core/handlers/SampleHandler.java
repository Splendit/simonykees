package simonykees.core.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import at.splendit.simonykees.core.dialogs.DiffViewWizard;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		// Create the wizard
		DiffViewWizard wizard = new DiffViewWizard();
//		wizard.init(window.getWorkbench(), null);

		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		
		// Open the wizard dialog
		dialog.open();

		return null;

	}
}
