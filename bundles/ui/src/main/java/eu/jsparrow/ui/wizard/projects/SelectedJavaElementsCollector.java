package eu.jsparrow.ui.wizard.projects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

import eu.jsparrow.ui.wizard.projects.javaelement.AbstractJavaElementWrapper;

public class SelectedJavaElementsCollector {

	List<AbstractJavaElementWrapper> getSelectedElementsWithoutSelectedParent(
			Set<AbstractJavaElementWrapper> allSelectedElements) {
		List<AbstractJavaElementWrapper> selectedElementsWithoutSelectedParent = new ArrayList<>();

		allSelectedElements.forEach(element -> {
			if (element.getParent() == null || !allSelectedElements.contains(element.getParent())) {
				selectedElementsWithoutSelectedParent.add(element);
			}
		});

		return selectedElementsWithoutSelectedParent;
	}

	Map<IJavaProject, List<IJavaElement>> getSelectedJavaElementsMapping(
			Set<AbstractJavaElementWrapper> allSelectedElements) {
		Map<IJavaProject, List<IJavaElement>> mapping = new HashMap<>();
		List<AbstractJavaElementWrapper> selectedElementsWithoutSelectedParent = getSelectedElementsWithoutSelectedParent(
				allSelectedElements);
		for (AbstractJavaElementWrapper elementWrapper : selectedElementsWithoutSelectedParent) {
			IJavaElement javaElement = elementWrapper.getJavaElement();
			IJavaProject javaProject = javaElement.getJavaProject();
			mapping.computeIfAbsent(javaProject, key -> new ArrayList<IJavaElement>());
			mapping.get(javaProject)
				.add(javaElement);
		}
		return mapping;
	}
}
