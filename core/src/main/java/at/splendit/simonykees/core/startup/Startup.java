package at.splendit.simonykees.core.startup;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceManager;

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

	@Override
	public void earlyStartup() {

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (SimonykeesPreferenceManager.getEnableIntro()) {
					Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					new StartDialog(activeShell).open();
				}

			}
		});

	}

}
