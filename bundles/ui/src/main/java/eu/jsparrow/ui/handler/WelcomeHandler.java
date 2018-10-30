package eu.jsparrow.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.ui.startup.WelcomeEditor;
import eu.jsparrow.ui.startup.WelcomeEditorInput;

public class WelcomeHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(WelcomeHandler.class);

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		IWorkbenchPage page = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow()
			.getActivePage();
		try {
			page.openEditor(WelcomeEditorInput.INSTANCE, WelcomeEditor.EDITOR_ID);
		} catch (PartInitException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}
