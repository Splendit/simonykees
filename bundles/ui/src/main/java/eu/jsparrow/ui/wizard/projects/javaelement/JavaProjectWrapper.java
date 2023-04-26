package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @since 4.17.0
 */
public class JavaProjectWrapper extends AbstractJavaElementWrapperWithChildList<PackageFragmentRootWrapper> {

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
				IPackageFragment firstPackageFragment = findFirstPackageFragment(packageFragmentRoot).orElse(null);
				if (firstPackageFragment != null) {
					packageFragmentRootWrapperList.add(new PackageFragmentRootWrapper(this, packageFragmentRoot));
				}
			}
		}
		return packageFragmentRootWrapperList;
	}

	public static boolean isPackageFragmentRootWithPackage(
			IPackageFragmentRoot packageFragmentRoot) throws JavaModelException {

		if (isSourcePackageFragmentRoot(packageFragmentRoot)) {
			IPackageFragment firstPackageFragment = findFirstPackageFragment(packageFragmentRoot).orElse(null);
			return firstPackageFragment != null;
		}

		return false;
	}

	private static Optional<IPackageFragment> findFirstPackageFragment(IPackageFragmentRoot packageFragmentRoot)
			throws JavaModelException {
		IJavaElement[] javaElementChildArray = packageFragmentRoot.getChildren();
		for (IJavaElement javaElement : javaElementChildArray) {
			if (javaElement instanceof IPackageFragment) {
				return Optional.of((IPackageFragment) javaElement);
			}
		}
		return Optional.empty();
	}

	public static boolean isSourcePackageFragmentRoot(IPackageFragmentRoot packageFragmentRoot)
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
