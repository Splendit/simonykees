package at.splendit.simonykees.core.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.i18n.Messages;

/**
 * TODO SIM-103 class description
 * 
 * @author Hannes Schweighofer
 * @since 0.9
 */
public abstract class AbstractSimonykeesHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSimonykeesHandler.class);
	
	private static final String EDITOR = "org.eclipse.jdt.ui.CompilationUnitEditor"; //$NON-NLS-1$
	private static final String PACKAGE_EXPLORER = "org.eclipse.jdt.ui.PackageExplorer"; //$NON-NLS-1$
	private static final String PROJECT_EXPLORER = "org.eclipse.ui.navigator.ProjectExplorer"; //$NON-NLS-1$

	static List<IJavaElement> getSelectedJavaElements(ExecutionEvent event) {
		final Shell shell = HandlerUtil.getActiveShell(event);
		final String activePartId = HandlerUtil.getActivePartId(event);

		switch (activePartId) {
		case EDITOR:
			return getFromEditor(shell, HandlerUtil.getActiveEditor(event));
		case PACKAGE_EXPLORER:
		case PROJECT_EXPLORER:
			return getFromExplorer(shell, getCurrentStructuredSelection(event));
		default:
			//Activator.log(Status.ERROR,
			//		NLS.bind(Messages.AbstractSimonykeesHandler_error_activePartId_unknown, activePartId), null);
			logger.error(NLS.bind(Messages.AbstractSimonykeesHandler_error_activePartId_unknown, activePartId));
			return Collections.emptyList();
		}
	}

	static List<IJavaElement> getFromEditor(Shell shell, IEditorPart editorPart) {
		final IEditorInput editorInput = editorPart.getEditorInput();
		final IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editorInput);
		if (javaElement instanceof ICompilationUnit) {
			return Collections.singletonList(javaElement);
		} else {
			//Activator.log(Status.ERROR, NLS.bind(Messages.AbstractSimonykeesHandler_error_unexpected_object_editor,
			//		javaElement.getClass().getName()), null);
			logger.error(NLS.bind(Messages.AbstractSimonykeesHandler_error_unexpected_object_editor,
					javaElement.getClass().getName()));
		}
		return Collections.emptyList();
	}

	static List<IJavaElement> getFromExplorer(Shell shell, IStructuredSelection iStructuredSelection) {
		final List<IJavaElement> javaElements = new ArrayList<>();
		for (Iterator<?> iterator = iStructuredSelection.iterator(); iterator.hasNext();) {
			final Object object = iterator.next();
			if (object instanceof ICompilationUnit || object instanceof IPackageFragment
					|| object instanceof IPackageFragmentRoot || object instanceof IJavaProject) {
				javaElements.add((IJavaElement) object);
			} else if (object instanceof IProject) {
				IProject project = (IProject) object;
				if (hasNature(project, JavaCore.NATURE_ID)) {
					javaElements.add(JavaCore.create(project));
				}
			} else {
//				Activator.log(Status.ERROR,
//						NLS.bind(Messages.AbstractSimonykeesHandler_error_unexpected_object_explorer,
//								object.getClass().getName()),
//						null);
				logger.error(NLS.bind(Messages.AbstractSimonykeesHandler_error_unexpected_object_explorer,
								object.getClass().getName()));
			}
		}
		return javaElements;
	}

	/**
	 * Return the current structured selection, or
	 * <code>StructuredSelection.EMPTY</code> if the current selection is not a
	 * structured selection or <code>null</code>.
	 *
	 * @param event
	 *            The execution event that contains the application context
	 * @return the current IStructuredSelection, or
	 *         <code>StructuredSelection.EMPTY</code>.
	 */
	static IStructuredSelection getCurrentStructuredSelection(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			return (IStructuredSelection) selection;
		}
		return StructuredSelection.EMPTY;
	}

	static boolean hasNature(IProject project, String natureId) {
		try {
			return project.hasNature(natureId);
		} catch (CoreException e) {
			// FIXME find a useful exception
			throw new RuntimeException(e.getCause());
		}
	}

}
