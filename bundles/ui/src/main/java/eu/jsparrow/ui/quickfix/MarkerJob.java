package eu.jsparrow.ui.quickfix;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IProgressConstants2;

public class MarkerJob extends WorkspaceJob {

	private final MarkerRunnable runnable;

	public MarkerJob(IResource resource, MarkerRunnable runnable) {
		super(MarkerFactory.JSPARROW_MARKER);
		this.runnable = runnable;
		setRule(ResourcesPlugin.getWorkspace().getRuleFactory().markerRule(resource));
		setUser(false);
		setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		try {
			runnable.run();
		} catch (CoreException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	@FunctionalInterface
	public interface MarkerRunnable {
		void run() throws CoreException;
	}

}
