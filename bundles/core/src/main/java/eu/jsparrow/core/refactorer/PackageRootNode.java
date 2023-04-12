package eu.jsparrow.core.refactorer;

/**
 * @since 4.17.0
 */
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * TODO: Use this class in a child list for the Class {@link JavaProjectNode}.
 *
 */
public class PackageRootNode {
	private final IPackageFragmentRoot packageFragmentRoot;

	PackageRootNode(IPackageFragmentRoot packageFragmentRoot) {
		this.packageFragmentRoot = packageFragmentRoot;
	}

	public IPackageFragmentRoot getPackageFragmentRoot() {
		return packageFragmentRoot;
	}

}
