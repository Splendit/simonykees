package eu.jsparrow.jdt.ls.core.internal;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public interface IProjectImporter {

	void initialize(File rootFolder);

	boolean applies(IProgressMonitor monitor) throws OperationCanceledException, CoreException;

	/**
	 * Check whether the importer applies to the given project configurations.
	 * @param projectConfigurations Collection of the project configurations.
	 * @param monitor progress monitor.
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	default boolean applies(Collection<IPath> projectConfigurations, IProgressMonitor monitor) throws OperationCanceledException, CoreException {
		return applies(monitor);
	}

	default boolean isResolved(File folder) throws OperationCanceledException, CoreException {
		return false;
	};

	void importToWorkspace(IProgressMonitor monitor) throws OperationCanceledException, CoreException;

	void reset();
}

