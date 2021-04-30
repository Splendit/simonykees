package eu.jsparrow.ui.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.rules.common.MarkerEvent;
import eu.jsparrow.ui.Activator;

public class MarkerFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(MarkerFactory.class);
	public static final String JSPARROW_MARKER = "jsparrow.marker"; //$NON-NLS-1$
	
	public void create(MarkerEvent event) {
		try {
			IJavaElement javaElement = event.getJavaElement();
			IResource resource = javaElement.getResource();
			scheduleWorkspaceJob(event.getMessage(), resource, event.getOffset(), event.getLength());
		} catch (CoreException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void scheduleWorkspaceJob(final String message, final IResource resource, final int start, final int length)
			throws CoreException {
		IMarker marker = create(resource);
		if (marker != null) {
			marker.setAttributes(new String[] { IMarker.MESSAGE, IMarker.CHAR_START, IMarker.CHAR_END,
					IMarker.SOURCE_ID },
					new Object[] { message, Integer.valueOf(start), Integer.valueOf(start + length), Activator.PLUGIN_ID });
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
