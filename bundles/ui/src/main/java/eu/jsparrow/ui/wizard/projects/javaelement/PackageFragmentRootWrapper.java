package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
/**
 * @since 4.17.0
 */
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @since 4.17.0
 */
public class PackageFragmentRootWrapper extends AbstractJavaElementWrapperWithChildList {
	private final JavaProjectWrapper parent;
	private final IPackageFragmentRoot javaElement;
	private final String elementName;
	private final IPath pathRelativeToProject;
	private final String pathToDisplay;

	PackageFragmentRootWrapper(JavaProjectWrapper parent, IPackageFragmentRoot packageFragmentRoot) {
		this.parent = parent;
		this.javaElement = packageFragmentRoot;
		this.elementName = packageFragmentRoot.getElementName();
		IPath projectPath = parent.getProjectPath();
		IPath fullPath = packageFragmentRoot.getResource()
			.getFullPath();
		this.pathRelativeToProject = fullPath.makeRelativeTo(projectPath);
		this.pathToDisplay = PathToString.pathToString(pathRelativeToProject);
	}

	protected List<IJavaElementWrapper> collectChildren()
			throws JavaModelException {
		RecursivePackageFragmentsCollector packageFragmentCollector = new RecursivePackageFragmentsCollector(this);
		List<IJavaElementWrapper> childList = new ArrayList<>();
		packageFragmentCollector.collectPackagesContainingSources(javaElement).forEach(childList::add);
		return childList;
	}

	@Override
	public JavaProjectWrapper getParent() {
		return parent;
	}

	@Override
	public IPackageFragmentRoot getJavaElement() {
		return javaElement;
	}

	@Override
	public String getElementName() {
		return elementName;
	}

	public IPath getPathRelativeToProject() {
		return pathRelativeToProject;
	}

	public String getPathToDisplay() {
		return pathToDisplay;
	}
}
