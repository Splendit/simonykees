package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;

public abstract class AbstractJavaElementParentWrapper<C extends AbstractJavaElementWrapper>
		implements AbstractJavaElementWrapper {

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

	public boolean isChildListUnassigned() {
		return children == null;
	}

	void setFirstChild(C firstChild) {
		// this.firstChild = firstChild;
	}

	
	
	@Override
	public boolean hasChildren() {
		return true;
	}

	protected abstract List<C> collectChildren() throws JavaModelException;	
	
}
