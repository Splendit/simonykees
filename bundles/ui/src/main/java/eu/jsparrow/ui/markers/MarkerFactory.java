package eu.jsparrow.ui.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.ui.Activator;

import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.*;

/**
 * A factory class for creating {@link IMarker}s for the generated
 * {@link RefactoringMarkerEvent}s.
 * 
 * @since 3.31.0
 *
 */
public class MarkerFactory {

	private static final Logger logger = LoggerFactory.getLogger(MarkerFactory.class);

	public static final String JSPARROW_MARKER = "jsparrow.marker"; //$NON-NLS-1$

	/**
	 * Creates an {@link IMarker} for the given {@link RefactoringMarkerEvent}.
	 * 
	 * @param event
	 *            the event to create the {@link IMarker} for.
	 */
	public void create(RefactoringMarkerEvent event) {
		try {
			IJavaElement javaElement = event.getJavaElement();
			IResource resource = javaElement.getResource();
			String resolver = event.getResolver();
			String name = event.getName();
			String message = event.getMessage();
			int offset = event.getOffset();
			int length = event.getLength();
			int newLength = event.getHighlightLength();
			String codePreview = event.getCodePreview();
			scheduleWorkspaceJob(resolver, name, message, resource, offset, length, newLength, codePreview);
		} catch (CoreException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void scheduleWorkspaceJob(final String resolver, final String name, final String message,
			final IResource resource, final int start, final int length, final int highlightLength, String codePreview)
			throws CoreException {
		Integer offset = Integer.valueOf(start);
		Integer end = Integer.valueOf(start + length);
		IMarker marker = create(resource);
		if (marker == null) {
			return;
		}
		String[] markerAttributeKeys = {
				RESOLVER_KEY, NAME_KEY,
				IMarker.MESSAGE,
				IMarker.CHAR_START,
				IMarker.CHAR_END,
				HIGHLIGHT_LENGTH_KEY,
				CODE_PREVIEW_KEY,
				IMarker.SOURCE_ID };
		Object[] attributeValues = {
				resolver,
				name,
				message,
				offset,
				end,
				highlightLength,
				codePreview,
				Activator.PLUGIN_ID };
		marker.setAttributes(markerAttributeKeys, attributeValues);

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
