package eu.jsparrow.core.exception.runtime;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Indicates that the {@link ICompilationUnit} of an {@link IJavaElement} cannot 
 * be found.
 * 
 * @author Ardit Ymeri
 *
 */
public class ICompilationUnitNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 7357897454464472862L;
	
	public ICompilationUnitNotFoundException(String message) {
		super(message);
	}
}
