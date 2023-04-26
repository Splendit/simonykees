package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;

public abstract class AbstractJavaElementWrapperWithChildList<C extends AbstractJavaElementWrapper>
		extends AbstractJavaElementWrapper {

	private List<C> children;

	public List<C> getChildren() {
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

	public boolean isChildListAssigned() {
		return children != null;
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	protected abstract List<C> collectChildren() throws JavaModelException;

}
