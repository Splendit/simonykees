package at.splendit.simonykees.core;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Executed by click menu.<br/>
 */
public class SampleHandler extends AbstractHandler {

    private final IWorkbenchWindow window;

    /**
     * constructor.
     */
    public SampleHandler() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        this.window = workbench.getActiveWorkbenchWindow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    	IProject[] project = root.getProjects(0);
    	String p = ""; //$NON-NLS-1$
    	for (IProject iProject : project) {
			p += iProject.getName() + "\n"; //$NON-NLS-1$
		}
        MessageDialog.openInformation(window.getShell(), "Eclipse Plugin Archetype", "Hello, Maven+Eclipse world,\n simonykees is built with Tycho\n" + p); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
    }

}
