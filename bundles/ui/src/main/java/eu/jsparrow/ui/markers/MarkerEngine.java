package eu.jsparrow.ui.markers;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;

import eu.jsparrow.rules.common.markers.RefactoringEventManager;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

/**
 * An engine for creating and clearing jSparrow markers based on the generated {@link RefactoringMarkerEvent}s. 
 * The implementation is designed after jdt.spelling plugin. 
 * 
 * @see {@linkplain https://github.com/stuarthendren/jdt.spelling}.
 * 
 * @since 3.31.0
 *
 */
public class MarkerEngine extends EditorTracker implements IElementChangedListener {

	private IResource currentResource;
	private static final String JAVA_EXTENSION = "java"; //$NON-NLS-1$
	private MarkerFactory markerFactory;
	private RefactoringEventManager eventGenerator;

	public MarkerEngine(MarkerFactory markerFactory, RefactoringEventManager eventGenerator) {
		this.markerFactory = markerFactory;
		this.eventGenerator = eventGenerator;
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		IJavaElementDelta delta = event.getDelta();
		handleDelta(delta);
	}

	@Override
	public void editorActivated(IEditorPart part) {
		currentResource = getResource(part);
		checkResource(currentResource);
	}

	@Override
	public void editorDeactivated(IEditorPart part) {
		clearEditor(part);
		IResource resource = getResource(part);
		if (resource != null && resource.equals(currentResource)) {
			setCurrentResource(null);
		}
	}

	private boolean isCurrent(IJavaElement element) {
		IResource resource = element.getResource();
		if (resource == null) {
			return false;
		}
		return resource.equals(currentResource);
	}

	private void setCurrentResource(IEditorPart editor) {
		currentResource = getResource(editor);
		checkResource(currentResource);
	}

	private void clearEditor(IEditorPart editor) {
		IResource resource = getResource(editor);
		if (shouldProcess(resource)) {
			MarkerJob job = new MarkerJob(resource, () -> markerFactory.clear(resource));
			job.schedule();
		}
	}

	private void checkResource(IResource resource) {
		if (shouldProcess(resource)) {
			checkElement(JavaCore.create(resource));
		}
	}

	private IResource getResource(IEditorPart editor) {
		if (editor == null) {
			return null;
		}
		IEditorInput editorInput = editor.getEditorInput();
		return ResourceUtil.getResource(editorInput);
	}

	private boolean shouldProcess(IResource resource) {
		return resource != null && resource.exists() && IResource.FILE == resource.getType()
				&& JAVA_EXTENSION.equals(resource.getFileExtension());
	}

	private void checkElement(IJavaElement element) {
		if (element == null) {
			return;
		}

		ICompilationUnit cu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (cu == null) {
			return;
		}

		if (cu.getOwner() != null) {
			cu = cu.getPrimary();
		}

		handleParentSourceReference(cu);
	}

	private void handleDelta(IJavaElementDelta delta) {
		if (IJavaElementDelta.REMOVED == delta.getKind()) {
			return;
		}

		IJavaElement element = delta.getElement();

		if (isCurrent(element)) {
			checkElement(element);
		}
	}

	private void handleParentSourceReference(ICompilationUnit cu) {
		List<RefactoringMarkerEvent> oldEvents = RefactoringMarkers.getAllEvents();
		RefactoringMarkers.clear();
		eventGenerator.discoverRefactoringEvents(cu);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		if (allSame(events, oldEvents)) {
			return;
		}
		final IResource resource = cu.getResource();
		MarkerJob job = new MarkerJob(resource, () -> {
			markerFactory.clear(resource);
			for (RefactoringMarkerEvent event : events) {
				markerFactory.create(event);
			}
		});
		job.schedule();
	}

	private boolean allSame(List<RefactoringMarkerEvent> events, List<RefactoringMarkerEvent> oldEvents) {
		if (events.size() != oldEvents.size()) {
			return false;
		}
		for (int i = 0; i < events.size(); i++) {
			RefactoringMarkerEvent event = events.get(i);
			RefactoringMarkerEvent oldEvent = events.get(i);
			if (!event.equals(oldEvent)) {
				return false;
			}
		}
		return true;
	}
}
