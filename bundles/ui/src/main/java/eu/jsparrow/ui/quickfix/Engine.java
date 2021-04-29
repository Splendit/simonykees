package eu.jsparrow.ui.quickfix;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.ui.IEditorPart;

public class Engine extends EditorTracker implements IElementChangedListener, IPreferenceChangeListener {
	
	private IResource currentResource;

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		
		
	}

	@Override
	public void editorActivated(IEditorPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void editorDeactivated(IEditorPart part) {
		// TODO Auto-generated method stub
		
	}

}
