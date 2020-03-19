package eu.jsparrow.ui.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;

/**
 * Utility class that handles requests from handlers.
 * 
 * @author Hannes Schweighofer, Martin Huter
 * @since 0.9
 */
public class WizardHandlerUtil {

	private static final Logger logger = LoggerFactory.getLogger(WizardHandlerUtil.class);

	private static final String EDITOR = "org.eclipse.jdt.ui.CompilationUnitEditor"; //$NON-NLS-1$
	private static final String PACKAGE_EXPLORER = "org.eclipse.jdt.ui.PackageExplorer"; //$NON-NLS-1$
	private static final String PROJECT_EXPLORER = "org.eclipse.ui.navigator.ProjectExplorer"; //$NON-NLS-1$

	private WizardHandlerUtil() {

	}

	/**
	 * Collects all the {@link IJavaElement} that are selected with the
	 * {@link ExecutionEvent} during an UI interaction in eclipse
	 * 
	 * @param event
	 *            that is triggered with an UI interaction in eclipse
	 * @return <b>{@code List<IJavaElement}></b> that are selected with the
	 *         given <b>{@code event}</b>
	 */
	public static Map<IJavaProject, List<IJavaElement>> getSelectedJavaElements(ExecutionEvent event)
			throws CoreException {
		final String activePartId = HandlerUtil.getActivePartId(event);

		switch (activePartId) {
		case EDITOR:
			Map<IJavaProject, List<IJavaElement>> retVal = new HashMap<>();
			getFromEditor(HandlerUtil.getActiveEditor(event))
				.ifPresent(javaElement -> retVal.put(javaElement.getJavaProject(),
						Collections.singletonList(javaElement)));
			return retVal;
		case PACKAGE_EXPLORER:
		case PROJECT_EXPLORER:
			return getFromExplorer(getCurrentStructuredSelection(event));
		default:
			logger.error(NLS.bind(Messages.AbstractSimonykeesHandler_error_activePartId_unknown, activePartId));
			return Collections.emptyMap();
		}
	}

	/**
	 * Wraps the {@link IJavaElement} from the given editor into a list if is an
	 * instance of {@link ICompilationUnit}.
	 * 
	 * @param editorPart
	 *            active editor
	 * @return a singleton list of {@link IJavaElement} if the java element is
	 *         an instance of {@link ICompilationUnit} or an empty list
	 *         otherwise.
	 */
	private static Optional<IJavaElement> getFromEditor(IEditorPart editorPart) {
		final IEditorInput editorInput = editorPart.getEditorInput();
		final IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editorInput);
		if (javaElement instanceof ICompilationUnit) {
			return Optional.of(javaElement);
		} else {
			logger.error(
					NLS.bind(Messages.AbstractSimonykeesHandler_error_unexpected_object_editor, javaElement.getClass()
						.getName()));
		}
		return Optional.empty();
	}

	/**
	 * Returns the list of {@link IJavaElement} from the selected compilation
	 * unit, package or project.
	 * 
	 * @param iStructuredSelection
	 *            active selection
	 * @return list of {@link IJavaElement}s if the selected structure is a
	 *         either of: {@link ICompilationUnit}, {@link IPackageFragment},
	 *         {@link IPackageFragmentRoot}, {@link IJavaProject} or
	 *         {@link IProject}. Otherwise an empty list.
	 */
	private static Map<IJavaProject, List<IJavaElement>> getFromExplorer(IStructuredSelection iStructuredSelection)
			throws CoreException {
		final Map<IJavaProject, List<IJavaElement>> javaElements = new HashMap<>();
		for (Iterator<?> iterator = iStructuredSelection.iterator(); iterator.hasNext();) {
			final Object object = iterator.next();
			if (object instanceof ICompilationUnit || object instanceof IPackageFragment
					|| object instanceof IPackageFragmentRoot || object instanceof IJavaProject) {
				IJavaElement javaElement = (IJavaElement) object;
				IJavaProject iJavaProject = javaElement.getJavaProject();

				if (!javaElements.containsKey(iJavaProject)) {
					javaElements.put(iJavaProject, new LinkedList<>());
				}

				javaElements.get(iJavaProject)
					.add(javaElement);

			} else if (object instanceof IProject) {
				IProject selectedProject = (IProject) object;

				List<IProject> projects = getSubProjects(selectedProject);
				projects.add(selectedProject);
				projects.stream()
					.filter(project -> hasNature(project, JavaCore.NATURE_ID))
					.map(JavaCore::create)
					.forEach(javaProject -> {
						if (!javaElements.containsKey(javaProject)) {
							javaElements.put(javaProject, new LinkedList<>());
						}

						javaElements.get(javaProject)
							.add(javaProject);
					});
			} else {
				logger.error(
						NLS.bind(Messages.AbstractSimonykeesHandler_error_unexpected_object_explorer, object.getClass()
							.getName()));
			}
		}
		return javaElements;
	}

	private static List<IProject> getSubProjects(IProject parentProject) throws CoreException {
		List<IProject> projects = new LinkedList<>();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] workspaceProjects = root.getProjects();

		IResource[] childResources = parentProject.members();
		for (IResource resource : childResources) {
			Optional<IProject> optionalProject = Arrays.stream(workspaceProjects)
				.filter(p -> p.getName()
					.equals(resource.getName()))
				.collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
					if (list.size() != 1) {
						return Optional.empty();
					}

					return Optional.ofNullable(list.get(0));
				}));

			if (optionalProject.isPresent()) {
				IProject project = optionalProject.get();
				projects.add(project);
				List<IProject> subProjects = getSubProjects(project);
				projects.addAll(subProjects);
			}
		}

		return projects;
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
	private static IStructuredSelection getCurrentStructuredSelection(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			return (IStructuredSelection) selection;
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * 
	 * @param project
	 *            an {@link IProject} to be checked
	 * @param natureId
	 *            a string representing a nature id of the project.
	 * @return {@code true} if the given {@link IProject} has the given nature
	 *         id or {@code false} otherwise.
	 */
	private static boolean hasNature(IProject project, String natureId) {
		try {
			return project.hasNature(natureId);
		} catch (CoreException e) {
			// FIXME find a useful exception
			throw new RuntimeException(e.getCause());
		}
	}

}
