package eu.jsparrow.ui.wizard.projects.javaelement;

public class AbstractJavaElementWrapper {
	
	private AbstractJavaElementParentWrapper parent;
	
	protected AbstractJavaElementWrapper(AbstractJavaElementParentWrapper parent) {
		this.parent = parent;
	}

	public AbstractJavaElementParentWrapper getParent() {
		return parent;
	}
}
