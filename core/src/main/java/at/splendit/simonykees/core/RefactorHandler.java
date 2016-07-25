package at.splendit.simonykees.core;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;


public class RefactorHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final Shell shell = HandlerUtil.getActiveShell(event);
		final String activePartId = HandlerUtil.getActivePartId(event);
		
		MessageDialog.openError(shell, "activePartId", activePartId);
		
		final RefactorASTVisitor refactorASTVisitor = new RefactorASTVisitor();
		
		return null;
	}
	
	public static void log(int severity, String message, Exception e) {
		final ILog log = Activator.getDefault().getLog();
		log.log(new Status(severity, Activator.PLUGIN_ID, message, e));
	}


}
