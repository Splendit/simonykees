package eu.jsparrow.ui.startup;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class WelcomeEditorInputFactory implements IElementFactory {

	public static final String FACTORY_ID = "eu.jsparrow.ui.startup.elementFactory";

	@Override
	public IAdaptable createElement(IMemento memento) {
		return WelcomeEditorInput.INSTANCE;
	}

	public static void save(IMemento memento) {
		// nothing to do really
		memento.putString("openDashboard", "true");
	}
}
