package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;

public abstract class AbstractJavaElementParentWrapper<C extends AbstractJavaElementWrapper>
		implements AbstractJavaElementWrapper {

	private List<C> children;
	private C firstChild;

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
			if (firstChild != null) {
				return Collections.singletonList(firstChild);
			}
			return Collections.emptyList();
		}
		return children;
	}

	public boolean isChildListUnassigned() {
		return children == null;
	}

	void setFirstChild(C firstChild) {
		this.firstChild = firstChild;
	}

	protected abstract List<C> collectChildren() throws JavaModelException;
}
