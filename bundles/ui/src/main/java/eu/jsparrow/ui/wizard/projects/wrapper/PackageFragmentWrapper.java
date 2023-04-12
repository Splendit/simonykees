package eu.jsparrow.ui.wizard.projects.wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @since 4.17.0
 */
public class PackageFragmentWrapper extends PackageFragmentChildWrapper {
	private final String packageName;
	private final IPackageFragment packageFragment;
	// TODO: abolish the field compilationUnits
	@Deprecated
	private final List<ICompilationUnit> compilationUnits;
	// field children instead of compilationUnits
	private List<PackageFragmentChildWrapper> children;

	@Deprecated
	public PackageFragmentWrapper(IPackageFragment packageFragment, List<ICompilationUnit> compilationUnits) {
		this.packageFragment = packageFragment;
		this.packageName = packageFragment.getElementName();
		this.compilationUnits = compilationUnits;
	}

	public PackageFragmentWrapper(IPackageFragment packageFragment) {
		this.packageFragment = packageFragment;
		this.packageName = packageFragment.getElementName();
		this.compilationUnits = Collections.emptyList();
	}

	@Deprecated
	public List<ICompilationUnit> getCompilationUnits() {
		return compilationUnits;
	}

	public String getPackageName() {
		return packageName;
	}

	public List<PackageFragmentChildWrapper> getChildren() {
		if (children == null) {
			children = new ArrayList<>();
			IJavaElement[] javaPackageChildren;
			try {
				javaPackageChildren = packageFragment.getChildren();
				for (IJavaElement javaElement : javaPackageChildren) {
					if (javaElement instanceof IPackageFragment) {
						children.add(new PackageFragmentWrapper((IPackageFragment) javaElement));
					} else if (javaElement instanceof ICompilationUnit) {
						children.add(new CompilationUnitWrapper((ICompilationUnit) javaElement));
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return children;
	}

}
