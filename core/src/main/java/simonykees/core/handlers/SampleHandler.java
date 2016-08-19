package simonykees.core.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.internal.ui.javadocexport.JavadocWizard;
import org.eclipse.jdt.internal.ui.refactoring.ExtractInterfaceWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {

	@SuppressWarnings("restriction")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		MessageDialog.openInformation(window.getShell(), "simonykees Core Plugin", "Hello, Eclipse world");
		// Create the wizard
		@SuppressWarnings("restriction")
		JavadocWizard wizard = new JavadocWizard();
		wizard.init(window.getWorkbench(), null);

		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		// Open the wizard dialog
		dialog.open();
		ExtractInterfaceWizard wizard2 = new ExtractInterfaceWizard(null, (Refactoring) null);

		WizardDialog dialog2 = new WizardDialog(window.getShell(), wizard2);
		// Open the wizard dialog
		dialog2.open();

		return null;

	}
}
