package eu.jsparrow.ui.wizard.projects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

import eu.jsparrow.ui.handler.IJavaElementsSelectionProvider;
import eu.jsparrow.ui.wizard.projects.javaelement.IJavaElementWrapper;

public class SelectedJavaElementsCollector implements IJavaElementsSelectionProvider {
	
	private final Set<IJavaElementWrapper> selectedJavaElementWrappers;
	
	SelectedJavaElementsCollector(Set<IJavaElementWrapper> selectedJavaElementWrappers) {
		this.selectedJavaElementWrappers = selectedJavaElementWrappers;
	}

	private List<IJavaElementWrapper> getSelectedElementsWithoutSelectedParent() {
		List<IJavaElementWrapper> selectedElementsWithoutSelectedParent = new ArrayList<>();

		selectedJavaElementWrappers.forEach(element -> {
			if (element.getParent() == null || !selectedJavaElementWrappers.contains(element.getParent())) {
				selectedElementsWithoutSelectedParent.add(element);
			}
		});

		return selectedElementsWithoutSelectedParent;
	}

	@Override
	public Map<IJavaProject, List<IJavaElement>> getSelectedJavaElements() {
		Map<IJavaProject, List<IJavaElement>> mapping = new HashMap<>();
		List<IJavaElementWrapper> selectedElementsWithoutSelectedParent = getSelectedElementsWithoutSelectedParent();
		for (IJavaElementWrapper elementWrapper : selectedElementsWithoutSelectedParent) {
			IJavaElement javaElement = elementWrapper.getJavaElement();
			IJavaProject javaProject = javaElement.getJavaProject();
			mapping.computeIfAbsent(javaProject, key -> new ArrayList<IJavaElement>());
			mapping.get(javaProject)
				.add(javaElement);
		}
		return mapping;
	}
}
