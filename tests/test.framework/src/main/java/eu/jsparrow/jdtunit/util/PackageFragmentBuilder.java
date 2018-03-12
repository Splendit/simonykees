package eu.jsparrow.jdtunit.util;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import eu.jsparrow.jdtunit.JdtUnitException;

public class PackageFragmentBuilder {

	private String packageName = "DefaultPackage";

	private IJavaProject javaProject;
	
	public PackageFragmentBuilder(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	public PackageFragmentBuilder setName(String packageName) {

		this.packageName = packageName;
		return this;
	}

	public IPackageFragment build() throws JdtUnitException {
		if (javaProject == null) {
			throw new JdtUnitException("Failed to build package, invalid project");
		}

		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(javaProject.getProject());
		IPackageFragment result;
		try {
			result = root.createPackageFragment(packageName, false, null);
		} catch (JavaModelException e) {
			throw new JdtUnitException("Failed to create package", e);
		}
		return result;
	}
}
