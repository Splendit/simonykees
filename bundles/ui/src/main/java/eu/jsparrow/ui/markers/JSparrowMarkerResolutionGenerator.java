package eu.jsparrow.ui.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.markers.CoreRefactoringEventManager;
import eu.jsparrow.rules.common.markers.RefactoringEventManager;

public class JSparrowMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

	private static final Logger logger = LoggerFactory.getLogger(JSparrowMarkerResolutionGenerator.class);
	private RefactoringEventManager eventResolver;

	public JSparrowMarkerResolutionGenerator(RefactoringEventManager eventGenerator) {
		super();
		this.eventResolver = eventGenerator;
	}

	public JSparrowMarkerResolutionGenerator() {
		this(new CoreRefactoringEventManager());
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		return new IMarkerResolution[] { new JSparrowMarkerResolution(marker, eventResolver) };

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
