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
public class PackageFragmentWrapper extends AbstractJavaElementParentWrapper {
	private final String packageName;
	private final IPackageFragment packageFragment;

	public PackageFragmentWrapper(AbstractJavaElementParentWrapper parent, IPackageFragment packageFragment) {
		super(parent);
		this.packageFragment = packageFragment;
		this.packageName = packageFragment.getElementName();
	}

	protected List<AbstractJavaElementWrapper> collectChildren()
			throws JavaModelException {
		IJavaElement[] javaElementArray;
		List<AbstractJavaElementWrapper> packageFragmentChildWrapperList = new ArrayList<>();
		javaElementArray = packageFragment.getChildren();
		for (IJavaElement javaElement : javaElementArray) {
			if (javaElement instanceof IPackageFragment) {
				packageFragmentChildWrapperList.add(new PackageFragmentWrapper(this, (IPackageFragment) javaElement));
			} else if (javaElement instanceof ICompilationUnit) {
				packageFragmentChildWrapperList.add(new CompilationUnitWrapper(this, (ICompilationUnit) javaElement));
			}
		}
		return packageFragmentChildWrapperList;
	}

	public String getElementName() {
		return packageName;
	}
}
