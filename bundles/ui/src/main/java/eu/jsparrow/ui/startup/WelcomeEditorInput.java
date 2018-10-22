package eu.jsparrow.ui.startup;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

public class WelcomeEditorInput implements IEditorInput, IPersistableElement {

	public final static WelcomeEditorInput INSTANCE = new WelcomeEditorInput();

	private WelcomeEditorInput() {
	}

	@Override
	public boolean exists() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public String getFactoryId() {
		return WelcomeEditorInputFactory.FACTORY_ID;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return "Welcome";
	}

	@Override
	public IPersistableElement getPersistable() {
		return this;
	}

	@Override
	public String getToolTipText() {
		return "Welcome";
	}

	@Override
	public void saveState(IMemento memento) {
		WelcomeEditorInputFactory.save(memento);
	}

}
