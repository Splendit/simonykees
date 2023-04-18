package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;

public abstract class AbstractJavaElementParentWrapper extends AbstractJavaElementWrapper {

	protected AbstractJavaElementParentWrapper(AbstractJavaElementParentWrapper parent) {
		super(parent);
	}

	protected List<AbstractJavaElementWrapper> children;

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

	public List<AbstractJavaElementWrapper> getChildren() {
		if (children == null) {
			return Collections.emptyList();
		}
		return children;
	}

	public boolean isChildListUnassigned() {
		return children == null;
	}

	protected abstract List<AbstractJavaElementWrapper> collectChildren() throws JavaModelException;

}
