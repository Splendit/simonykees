package eu.jsparrow.ui.wizard.projects.wrapper;

/**
 * @since 4.17.0
 */
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * TODO: Use this class in a child list for the Class {@link JavaProjectWrapper}.
 *
 */
public class PackageFragmentRootWrapper {
	private final IPackageFragmentRoot packageFragmentRoot;

	PackageFragmentRootWrapper(IPackageFragmentRoot packageFragmentRoot) {
		this.packageFragmentRoot = packageFragmentRoot;
	}

	public IPackageFragmentRoot getPackageFragmentRoot() {
		return packageFragmentRoot;
	}

}
