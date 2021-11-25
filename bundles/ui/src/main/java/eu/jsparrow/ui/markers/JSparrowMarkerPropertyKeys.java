package eu.jsparrow.ui.markers;

import org.eclipse.core.resources.IMarker;

/**
 * Defines jSparrow marker keys that are not part of the predefined
 * {@link IMarker} properties.
 * 
 * @since 4.0.0
 *
 */
public class JSparrowMarkerPropertyKeys {

	public static final String RESOLVER_KEY = "resolver"; //$NON-NLS-1$
	public static final String NAME_KEY = "name"; //$NON-NLS-1$
	public static final String CODE_PREVIEW_KEY = "description"; //$NON-NLS-1$
	public static final String HIGHLIGHT_LENGTH_KEY = "highlightLength"; //$NON-NLS-1$
	public static final String JSPARROW_MARKER_COLOR_KEY = "jsparrow.marker.color"; //$NON-NLS-1$
	public static final String WEIGHT_VALUE_KEY = "weight.value"; //$NON-NLS-1$

	private JSparrowMarkerPropertyKeys() {
		/*
		 * Hide the default constructor.
		 */
	}

}
