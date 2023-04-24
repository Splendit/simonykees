package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @since 4.17.0
 */
public class PackageFragmentWrapper extends AbstractJavaElementParentWrapper<CompilationUnitWrapper> {
	private final PackageFragmentRootWrapper parent;
	private final String packageName;
	private final IPackageFragment packageFragment;

	public PackageFragmentWrapper(PackageFragmentRootWrapper parent, IPackageFragment packageFragment,
			ICompilationUnit firstCompilationUnit) {
		this(parent, packageFragment);
		this.setFirstChild(new CompilationUnitWrapper(this, firstCompilationUnit));
	}

	public PackageFragmentWrapper(PackageFragmentRootWrapper parent, IPackageFragment packageFragment) {
		this.parent = parent;
		this.packageFragment = packageFragment;
		this.packageName = packageFragment.getElementName();
	}

	protected List<CompilationUnitWrapper> collectChildren()
			throws JavaModelException {
		IJavaElement[] javaElementArray;
		List<CompilationUnitWrapper> packageFragmentChildWrapperList = new ArrayList<>();
		javaElementArray = packageFragment.getChildren();
		for (IJavaElement javaElement : javaElementArray) {
			if (javaElement instanceof ICompilationUnit) {
				packageFragmentChildWrapperList.add(new CompilationUnitWrapper(this, (ICompilationUnit) javaElement));
			}
		}
		return packageFragmentChildWrapperList;
	}

	@Override
	public PackageFragmentRootWrapper getParent() {
		return parent;
	}

	@Override
	public IPackageFragment getJavaElement() {
		return packageFragment;
	}

	@Override
	public String getElementName() {
		return packageName;
	}
}
