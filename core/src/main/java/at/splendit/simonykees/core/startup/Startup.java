package at.splendit.simonykees.core.startup;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceManager;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
		    public void run() {
		    	if (SimonykeesPreferenceManager.getEnableIntro()) {
			        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					new BrowserDialog(activeShell).open();
		    	}

		    }
		});

	}

}
