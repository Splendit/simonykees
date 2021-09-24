package eu.jsparrow.ui.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.markers.CoreRefactoringEventManager;
import eu.jsparrow.rules.common.markers.RefactoringEventManager;

/**
 * Generates resolution instances for jSparrow markers.
 * 
 * @since 4.0.0
 *
 */
public class JSparrowMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

	private static final Logger logger = LoggerFactory.getLogger(JSparrowMarkerResolutionGenerator.class);
	private RefactoringEventManager eventManager;

	public JSparrowMarkerResolutionGenerator(RefactoringEventManager eventManager) {
		super();
		this.eventManager = eventManager;
	}

	public JSparrowMarkerResolutionGenerator() {
		this(new CoreRefactoringEventManager());
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		// TODO: Check for license?  Is there any other license model? Is there any valid pay per use license model? 
		return new IMarkerResolution[] { new JSparrowMarkerResolution(marker, eventManager) };

	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		try {
			String markerType = marker.getType();
			return MarkerFactory.JSPARROW_MARKER.equals(markerType);
		} catch (CoreException e) {
			logger.error("Cannot read marker annotation type", e); //$NON-NLS-1$
		}
		return false;
	}
}
