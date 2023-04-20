package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @since 4.17.0
 */
public class JavaProjectWrapper extends AbstractJavaElementParentWrapper<PackageFragmentRootWrapper> {

	private final IJavaProject javaProject;
	private final IPath projectPath;
	private final String pathToDisplay;
	private final String projectName;

	protected List<PackageFragmentRootWrapper> collectChildren()
			throws JavaModelException {
		IPackageFragmentRoot[] packageFragmentRootArray = javaProject.getPackageFragmentRoots();
		List<PackageFragmentRootWrapper> packageFragmentRootWrapperList = new ArrayList<>();
		for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRootArray) {
			if (isSourcePackageFragmentRoot(packageFragmentRoot)) {
				packageFragmentRootWrapperList.add(new PackageFragmentRootWrapper(this, packageFragmentRoot));
			}
		}
		return packageFragmentRootWrapperList;
	}

	private static boolean isSourcePackageFragmentRoot(IPackageFragmentRoot packageFragmentRoot)
			throws JavaModelException {

		return packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE &&
				!packageFragmentRoot.isExternal() &&
				!packageFragmentRoot.isArchive();
	}

	public JavaProjectWrapper(IJavaProject javaProject) {
		this.javaProject = javaProject;
		this.projectName = javaProject.getElementName();
		this.projectPath = javaProject.getResource()
			.getFullPath();
		this.pathToDisplay = PathToString.pathToString(projectPath);
	}

	@Override
	public AbstractJavaElementWrapper getParent() {
		return null;
	}

	@Override
	public IJavaProject getJavaElement() {
		return javaProject;
	}

	@Override
	public String getElementName() {
		return projectName;
	}

	public IPath getProjectPath() {
		return projectPath;
	}

	public String getPathToDisplay() {
		return pathToDisplay;
	}
}
