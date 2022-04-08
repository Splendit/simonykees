package eu.jsparrow.ui.markers;

import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.NAME_KEY;
import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.RESOLVER_KEY;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.ui.preference.SimonykeesMarkersPreferencePage;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;

/**
 * A quick-fix implementation that allows users to disable jSparrow Markers.
 * This is achieved by opening the {@link SimonykeesMarkersPreferencePage} and
 * putting the search field to the current marker name.
 * 
 * @since 4.10.0
 *
 */
public class JSparrowMarkerDeactivationResolution implements IMarkerResolution2, IJavaCompletionProposal {

	private static final Logger logger = LoggerFactory.getLogger(JSparrowMarkerDeactivationResolution.class);

	private String name;
	private String resolverId;

	public JSparrowMarkerDeactivationResolution(IMarker marker) {
		name = marker.getAttribute(NAME_KEY, ""); //$NON-NLS-1$
		resolverId = marker.getAttribute(RESOLVER_KEY, ""); //$NON-NLS-1$
	}

	@Override
	public String getLabel() {
		return String.format("Deactivate '%s'", name); //$NON-NLS-1$
	}

	@Override
	public void run(IMarker marker) {
		Shell activeShell = Display.getDefault()
			.getActiveShell();
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
				activeShell, "eu.jsparrow.ui.preference.MarkersPreferencePage", null, null); //$NON-NLS-1$
		SimonykeesMarkersPreferencePage page = (SimonykeesMarkersPreferencePage) dialog.getSelectedPage();
		page.setSearchField(name);
		dialog.open();

		boolean remainsActive = SimonykeesPreferenceManager.getAllActiveMarkers()
			.contains(resolverId);
		if (remainsActive) {
			return;
		}
		try {
			marker.delete();
		} catch (CoreException e) {
			logger.warn("Cannot remove marker", e); //$NON-NLS-1$
		}
	}

	@Override
	public String getDescription() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return JSparrowImages.JSPARROW_ACTIVE_16;
	}

	@Override
	public void apply(IDocument document) {
		/*
		 * Auto generated method
		 */
	}

	@Override
	public Point getSelection(IDocument document) {
		/*
		 * Auto generated method
		 */
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		/*
		 * Auto generated method
		 */
		return null;
	}

	@Override
	public String getDisplayString() {
		return name;
	}

	@Override
	public IContextInformation getContextInformation() {
		/*
		 * Auto generated method
		 */
		return null;
	}

	@Override
	public int getRelevance() {
		return 1;
	}

}
