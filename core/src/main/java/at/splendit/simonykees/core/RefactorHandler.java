package at.splendit.simonykees.core;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;


public class RefactorHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final Shell shell = HandlerUtil.getActiveShell(event);
		final String activePartId = HandlerUtil.getActivePartId(event);
		
		log("activePartId [" + activePartId + "]");
		
		switch (activePartId) {
		case "org.eclipse.jdt.ui.CompilationUnitEditor":
			ICompilationUnit compilationUnit = getFromEditor(shell, HandlerUtil.getActiveEditor(event));
			final ASTParser astParser = ASTParser.newParser(AST.JLS8);
			resetParser(compilationUnit, astParser);
			break;
		case "org.eclipse.jdt.ui.PackageExplorer":
		case "org.eclipse.ui.navigator.ProjectExplorer":
			HandlerUtil.getCurrentStructuredSelection(event);
			log(Status.ERROR, "activePartId [" + activePartId + "] must be coded next", null);
			break;

		default:
			log(Status.ERROR, "activePartId [" + activePartId + "] unknown", null);
			break;
		}
		
		final RefactorASTVisitor refactorASTVisitor = new RefactorASTVisitor();
		
		new RefactoringJob().schedule();
		
		return null;
	}
	
	private static ICompilationUnit getFromEditor(Shell shell, IEditorPart editorPart) {
		final IEditorInput editorInput = editorPart.getEditorInput();
		final IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editorInput);
		if (javaElement instanceof ICompilationUnit) {
			return (ICompilationUnit) javaElement;
		}
		return null;
	}
	
	private static void resetParser(ICompilationUnit compilationUnit, ASTParser astParser) {
		astParser.setSource(compilationUnit);
		astParser.setResolveBindings(true);
//		astParser.setCompilerOptions(null);
	}
	
	public static void log(int severity, String message, Exception e) {
		final ILog log = Activator.getDefault().getLog();
		log.log(new Status(severity, Activator.PLUGIN_ID, message, e));
	}
	
	public static void log(String message, Exception e) {
		log(IStatus.INFO, message, e);
	}
	
	public static void log(String message) {
		log(message, null);
	}


}
