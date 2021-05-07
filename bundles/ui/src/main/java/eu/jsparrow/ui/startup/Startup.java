package eu.jsparrow.ui.startup;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.ui.markers.Engine;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.ui.startup.registration.RegistrationDialog;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * Startup class starts immediately on Eclipse startup with welcome screen if it
 * is not turned of
 * 
 * @author andreja.sambolec
 * 
 * @since 2.0.2
 *
 */
public class Startup implements IStartup {

	private static final Logger logger = LoggerFactory.getLogger(Startup.class);

	@Override
	public void earlyStartup() {
		LicenseUtil licenseUtil = LicenseUtil.get();
		PlatformUI.getWorkbench()
			.getDisplay()
			.asyncExec(() -> {
				IWorkbench workbench = PlatformUI.getWorkbench();
				if (!licenseUtil.isValidProLicensePresentInSecureStore() && !licenseUtil.isActiveRegistration()) {
					
					Shell activeShell = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow()
						.getShell();
					new RegistrationDialog(activeShell).open();
				}
				if (SimonykeesPreferenceManager.getEnableDashboard()) {
					IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow()
						.getActivePage();
					try {
						page.openEditor(WelcomeEditorInput.INSTANCE, WelcomeEditor.EDITOR_ID);
					} catch (PartInitException e) {
						logger.error(e.getMessage(), e);
					}
					/*
					 * Dashboard should be opened just once, when the Eclipse is
					 * first time started after the jSparrow is installed, and
					 * then disabled.
					 */
					SimonykeesPreferenceManager.setEnableDashboard(false);
				}
				
				Engine engine = new Engine();

				engine.track(workbench);
				JavaCore.addElementChangedListener(engine);

			});
	}
}
