package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;

public abstract class AbstractJavaElementWrapperWithChildList implements IJavaElementWrapper {

	private List<IJavaElementWrapper> children;

	public List<IJavaElementWrapper> getChildren() {
		if (children == null) {
			try {
				children = collectChildren();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			if (children == null) {
				children = Collections.emptyList();
			}
		}
		return children;
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public boolean hasChildListAtHand() {
		return children != null;
	}

	protected abstract List<IJavaElementWrapper> collectChildren() throws JavaModelException;
}
