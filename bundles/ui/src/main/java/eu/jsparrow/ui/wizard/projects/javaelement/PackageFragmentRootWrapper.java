package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
/**
 * @since 4.17.0
 */
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @since 4.17.0
 */
public class PackageFragmentRootWrapper extends AbstractJavaElementParentWrapper {
	private final IPackageFragmentRoot packageFragmentRoot;
	private final String elementName;

	PackageFragmentRootWrapper(AbstractJavaElementParentWrapper parent, IPackageFragmentRoot packageFragmentRoot) {
		super(parent);
		this.packageFragmentRoot = packageFragmentRoot;
		this.elementName = packageFragmentRoot.getElementName();
	}

	public IPackageFragmentRoot getPackageFragmentRoot() {
		return packageFragmentRoot;
	}

	protected List<AbstractJavaElementWrapper> collectChildren()
			throws JavaModelException {
		IJavaElement[] javaElementArray = packageFragmentRoot.getChildren();
		List<AbstractJavaElementWrapper> packageFragmentWrapperList = new ArrayList<>();
		for (IJavaElement javaElement : javaElementArray) {
			if (javaElement instanceof IPackageFragment) {
				packageFragmentWrapperList.add(new PackageFragmentWrapper(this, (IPackageFragment) javaElement));
			}
		}
		return packageFragmentWrapperList;
	}

	public String getElementName() {
		return elementName;
	}
}
