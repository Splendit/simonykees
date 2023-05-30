package eu.jsparrow.ui.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

public interface IJavaElementsSelectionProvider {

	Map<IJavaProject, List<IJavaElement>> getSelectedJavaElements();

}
