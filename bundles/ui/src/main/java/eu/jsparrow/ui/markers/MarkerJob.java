package eu.jsparrow.ui.markers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.IProgressConstants2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkerJob extends WorkspaceJob {

	private static final Logger logger = LoggerFactory.getLogger(MarkerJob.class);
	private final MarkerRunnable runnable;

	public MarkerJob(IResource resource, MarkerRunnable runnable) {
		super(MarkerFactory.JSPARROW_MARKER);
		this.runnable = runnable;
		setRule(ResourcesPlugin.getWorkspace()
			.getRuleFactory()
			.markerRule(resource));
		setUser(false);
		setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		try {
			runnable.run();
		} catch (CoreException e) {
			logger.error("Cannot run MarkerJob", e); //$NON-NLS-1$
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	@FunctionalInterface
	public interface MarkerRunnable {
		void run() throws CoreException;
	}

}
