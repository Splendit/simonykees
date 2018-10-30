package eu.jsparrow.ui.startup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;

/**
 * Editor for the welcome page. 
 * 
 * @since 2.7.0
 *
 */
public class WelcomeEditor extends SharedHeaderFormEditor {

	private static final Logger logger = LoggerFactory.getLogger(WelcomeEditor.class);

	public static final String EDITOR_ID = "ui.editors.jsparrow.WelcomeEditor"; //$NON-NLS-1$

	@Override
	protected void addPages() {
		WelcomePage mainPage = new WelcomePage(this, WelcomePage.PAGE_ID, Messages.WelcomeEditor_title);
		try {
			addPage(mainPage);
		} catch (PartInitException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		// Nothing to do
	}

	@Override
	public void doSaveAs() {
		// Nothing to do
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
