package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IPackageFragment;
/**
 * @since 4.17.0
 */
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @since 4.17.0
 */
public class PackageFragmentRootWrapper extends AbstractJavaElementParentWrapper<PackageFragmentWrapper> {
	private final JavaProjectWrapper parent;
	private final IPackageFragmentRoot javaElement;
	private final String elementName;
	private final IPath pathRelativeToProject;
	private final String pathToDisplay;
	
	PackageFragmentRootWrapper(JavaProjectWrapper parent, IPackageFragmentRoot packageFragmentRoot, IPackageFragment firstPackageFragment) {
		this(parent, packageFragmentRoot);
		this.firstChild = new PackageFragmentWrapper(this, firstPackageFragment);
	}

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

	protected List<PackageFragmentWrapper> collectChildren()
			throws JavaModelException {
		RecursivePackageFragmentsCollector packageFragmentCollector = new RecursivePackageFragmentsCollector();
		return packageFragmentCollector.collectPackagesContainingSources(javaElement);
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
