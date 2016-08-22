package at.splendit.simonykees.core.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import at.splendit.simonykees.core.Activator;

public abstract class AbstractSimonykeesHandler extends AbstractHandler {
	
	static List<IJavaElement> getSelectedJavaElements(ExecutionEvent event) {
		final Shell shell = HandlerUtil.getActiveShell(event);
		final String activePartId = HandlerUtil.getActivePartId(event);
		
		switch (activePartId) {
		case "org.eclipse.jdt.ui.CompilationUnitEditor":
			return getFromEditor(shell, HandlerUtil.getActiveEditor(event));
		case "org.eclipse.jdt.ui.PackageExplorer":
		case "org.eclipse.ui.navigator.ProjectExplorer":
			return getFromExplorer(shell, HandlerUtil.getCurrentStructuredSelection(event));
		default:
			Activator.log(Status.ERROR, "activePartId [" + activePartId + "] unknown", null);
			return Collections.emptyList();
		}
	}

	static List<IJavaElement> getFromEditor(Shell shell, IEditorPart editorPart) {
		final IEditorInput editorInput = editorPart.getEditorInput();
		final IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editorInput);
		if (javaElement instanceof ICompilationUnit) {
			return Collections.singletonList(javaElement);
		} else {
			Activator.log(Status.ERROR, "unexpected object class in editor [" + javaElement.getClass().getName() + "]", null);
		}
		return Collections.emptyList();
	}
	
	static List<IJavaElement> getFromExplorer(Shell shell, IStructuredSelection iStructuredSelection) {
		final List<IJavaElement> javaElements = new ArrayList<>();
		for (Iterator<?> iterator = iStructuredSelection.iterator(); iterator.hasNext();) {
			final Object object = iterator.next();
			if (object instanceof ICompilationUnit ||
					object instanceof IPackageFragment ||
					object instanceof IPackageFragmentRoot ||
					object instanceof IJavaProject) {
				javaElements.add((IJavaElement) object);
			} else {
				Activator.log(Status.ERROR, "unexpected object class in explorer [" + object.getClass().getName() + "]", null);
			}
		}
		return javaElements;
	}
	
	static void resetParser(ICompilationUnit compilationUnit, ASTParser astParser) {
		astParser.setSource(compilationUnit);
		astParser.setResolveBindings(true);
//		astParser.setCompilerOptions(null);
	}

}
