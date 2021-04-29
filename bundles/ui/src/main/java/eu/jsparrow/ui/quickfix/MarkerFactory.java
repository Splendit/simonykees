package eu.jsparrow.ui.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;

import eu.jsparrow.rules.common.MarkerEvent;
import eu.jsparrow.ui.Activator;

public class MarkerFactory {
	
	public static final String JSPARROW_MARKER = "jsparrow.marker"; //$NON-NLS-1$
	
	public void create(MarkerEvent event) {
		try {
			IJavaElement javaElement = event.getJavaElement();
			IResource resource = javaElement.getResource();

			scheduleWorkspaceJob(event.getMessage(), resource, event.getOffset(), event.getLength());

		} catch (CoreException e) {
			//TODO: log me
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

	private IMarker create(IResource resource) {
		try {
			if (resource.exists()) {
				return resource.createMarker(JSPARROW_MARKER);
			} else {
				return null;
			}
		} catch (CoreException e) {
			// Log me.
			return null;
		}
	}

	public IMarker[] find(IResource target) {
		try {
			return target.findMarkers(JSPARROW_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// log me
			return null;
		}
	}

	public void clear(final IResource resource) {
		try {
			resource.deleteMarkers(JSPARROW_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// Log me.
		}
	}

}
