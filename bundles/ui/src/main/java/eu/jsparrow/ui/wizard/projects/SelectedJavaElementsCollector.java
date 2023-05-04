package eu.jsparrow.ui.wizard.projects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

import eu.jsparrow.ui.handler.IJavaElementsSelectionProvider;
import eu.jsparrow.ui.wizard.projects.javaelement.AbstractJavaElementWrapper;

public class SelectedJavaElementsCollector implements IJavaElementsSelectionProvider {
	
	private final Set<AbstractJavaElementWrapper> selectedJavaElementWrappers;
	
	SelectedJavaElementsCollector(Set<AbstractJavaElementWrapper> selectedJavaElementWrappers) {
		this.selectedJavaElementWrappers = selectedJavaElementWrappers;
	}

	private List<AbstractJavaElementWrapper> getSelectedElementsWithoutSelectedParent() {
		List<AbstractJavaElementWrapper> selectedElementsWithoutSelectedParent = new ArrayList<>();

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
		List<AbstractJavaElementWrapper> selectedElementsWithoutSelectedParent = getSelectedElementsWithoutSelectedParent();
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
