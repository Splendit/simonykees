package eu.jsparrow.ui.startup;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * 
 * @since 2.7.0
 *
 */
public class WelcomeEditorInputFactory implements IElementFactory {

	public static final String FACTORY_ID = "eu.jsparrow.ui.startup.elementFactory"; //$NON-NLS-1$

	@Override
	public IAdaptable createElement(IMemento memento) {
		return WelcomeEditorInput.INSTANCE;
	}

	public static void save(IMemento memento) {
		// nothing to do really
		memento.putString("openDashboard", "true");  //$NON-NLS-1$//$NON-NLS-2$
	}
}
