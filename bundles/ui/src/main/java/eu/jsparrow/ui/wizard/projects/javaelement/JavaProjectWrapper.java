package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @since 4.17.0
 */
public class JavaProjectWrapper extends AbstractJavaElementParentWrapper {

	private final IJavaProject javaProject;
	private final String projectName;

	protected List<AbstractJavaElementWrapper> collectChildren()
			throws JavaModelException {
		IPackageFragmentRoot[] packageFragmentRootArray = javaProject.getPackageFragmentRoots();
		List<AbstractJavaElementWrapper> packageFragmentRootWrapperList = new ArrayList<>();
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
		super(null);
		this.javaProject = javaProject;
		this.projectName = javaProject.getElementName();
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}

	public String getElementName() {
		return projectName;
	}
}
