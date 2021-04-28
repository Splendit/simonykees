package eu.jsparrow.ui.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class MarkerFactory {
	
	public static IMarker create(IResource resource) throws CoreException {
		return resource.createMarker(IMarker.TEXT);
	}
	
	public static void delete(IMarker marker) {
		
		try {
			marker.delete();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
