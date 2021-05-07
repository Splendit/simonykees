package eu.jsparrow.ui.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.ui.Activator;

public class MarkerFactory {

	private static final Logger logger = LoggerFactory.getLogger(MarkerFactory.class);
	public static final String JSPARROW_MARKER = "jsparrow.marker"; //$NON-NLS-1$
	private static final String RESOLVER_KEY =  "resolver"; //$NON-NLS-1$
	private static final String NAME_KEY = "name"; //$NON-NLS-1$
	private static final String DESCRIPTION_KEY = "description"; //$NON-NLS-1$

	public void create(RefactoringMarkerEvent event) {
		try {
			IJavaElement javaElement = event.getJavaElement();
			IResource resource = javaElement.getResource();
			String resolver = event.getResolver();
			String name = event.getName();
			String message = event.getMessage();
			int offset = event.getOffset();
			int length = event.getLength();
			String description = event.getDescription();
			scheduleWorkspaceJob(resolver, name, message, resource, offset, length, description);
		} catch (CoreException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void scheduleWorkspaceJob(final String resolver, final String name, final String message, final IResource resource,
			final int start, final int length, String description)
			throws CoreException {
		Integer offset = Integer.valueOf(start);
		Integer end = Integer.valueOf(start + length);
		IMarker marker = create(resource);
		if (marker != null) {
			marker.setAttributes(
					new String[] {RESOLVER_KEY, NAME_KEY, IMarker.MESSAGE, IMarker.CHAR_START, IMarker.CHAR_END, DESCRIPTION_KEY,
							IMarker.SOURCE_ID },
					new Object[] { resolver, name, message, offset, end, description, Activator.PLUGIN_ID });
		}
	}

	private IMarker create(IResource resource) throws CoreException {
		if (resource.exists()) {
			return resource.createMarker(JSPARROW_MARKER);
		} else {
			return null;
		}
	}

	public IMarker[] find(IResource target) throws CoreException {
		return target.findMarkers(JSPARROW_MARKER, true, IResource.DEPTH_INFINITE);
	}

	public void clear(final IResource resource) throws CoreException {
		resource.deleteMarkers(JSPARROW_MARKER, true, IResource.DEPTH_INFINITE);
	}

}
