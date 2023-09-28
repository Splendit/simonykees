package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;

public class PathToString {
	
	static String pathToString(IPath path) {
		if (path.segments().length == 1) {
			return path.segments()[0];
		}
		return Arrays.stream(path.segments())
			.collect(Collectors.joining("/")); //$NON-NLS-1$
	}

	private PathToString() {
		// private default constructor hiding implicit public once
	}
}
