package eu.jsparrow.jdt.ls.core.internal.managers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Service to automatically discover and attach sources to unknown class files.
 *
 * @author Fred Bricon
 *
 */
public interface ISourceDownloader {

	/**
	 * Discovers and attaches sources to the given {@link IClassFile}'s parent
	 * {@link IClasspathEntry}, if it's a jar file.
	 *
	 * @param classFile
	 *            the file to identify and search sources for
	 * @param monitor
	 *            a progress monitor
	 * @throws CoreException
	 */
	public void discoverSource(IClassFile classFile, IProgressMonitor monitor) throws CoreException;


}
