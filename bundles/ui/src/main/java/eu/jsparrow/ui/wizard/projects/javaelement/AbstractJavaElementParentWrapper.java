package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;

public abstract class AbstractJavaElementParentWrapper<C extends AbstractJavaElementWrapper> implements AbstractJavaElementWrapper {

	protected List<C> children;
	protected C firstChild;

	public void loadChildren() {
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
	}

	public List<C> getChildren() {
		if (children == null) {
			return Collections.emptyList();
		}
		return children;
	}

	public boolean isChildListUnassigned() {
		return children == null;
	}

	protected abstract List<C> collectChildren() throws JavaModelException;
}
