package eu.jsparrow.ui.quickfix;

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

import eu.jsparrow.core.markers.EventProducer;
import eu.jsparrow.rules.common.MarkerEvent;
import eu.jsparrow.rules.common.RefactoringEventProducer;

public class Engine extends EditorTracker implements IElementChangedListener {

	private IResource currentResource;
	private static final String JAVA_EXTENSION = "java"; //$NON-NLS-1$
	private MarkerFactory markerFactory = new MarkerFactory();

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

	public void checkElement(IJavaElement element) {
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
		RefactoringEventProducer eventGenerator = new EventProducer();
		List<MarkerEvent> events = eventGenerator.generateEvents(cu);
		final IResource resource = cu.getResource();
		MarkerJob job = new MarkerJob(resource, () -> {
			markerFactory.clear(resource);
			for (MarkerEvent event : events) {
				markerFactory.create(event);
			}
		});
		job.schedule();
	}
}
