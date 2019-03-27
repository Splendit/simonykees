package eu.jsparrow.ui.startup;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * 
 * 
 * @since 2.7.0
 *
 */
public class WelcomeEditorInput implements IEditorInput, IPersistableElement {

	public static final WelcomeEditorInput INSTANCE = new WelcomeEditorInput();

	private WelcomeEditorInput() {
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
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
		return "Welcome"; //$NON-NLS-1$
	}

	@Override
	public IPersistableElement getPersistable() {
		return this;
	}

	@Override
	public String getToolTipText() {
		return "Welcome"; //$NON-NLS-1$
	}

	@Override
	public void saveState(IMemento memento) {
		WelcomeEditorInputFactory.save(memento);
	}

}
