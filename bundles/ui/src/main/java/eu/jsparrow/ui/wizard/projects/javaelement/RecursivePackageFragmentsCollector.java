package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class RecursivePackageFragmentsCollector {

	private List<PackageFragmentWrapper> packageFragmentWrapperList;

	public List<PackageFragmentWrapper> collectPackagesContainingSources(IPackageFragmentRoot sourcePackageFragmentRoot)
			throws JavaModelException {

		packageFragmentWrapperList = new ArrayList<>();

		IJavaElement[] childArray = sourcePackageFragmentRoot.getChildren();
		for (IJavaElement child : childArray) {
			if (child instanceof IPackageFragment) {
				analyzePackageFragment((IPackageFragment) child);
			}
		}
		return packageFragmentWrapperList;
	}

	private void analyzePackageFragment(IPackageFragment packageFragment) throws JavaModelException {
		IJavaElement[] childArray = packageFragment.getChildren();
		for (IJavaElement child : childArray) {
			if (child instanceof ICompilationUnit) {
				ICompilationUnit firstCompilationUnit = (ICompilationUnit) child;
				packageFragmentWrapperList
					.add(new PackageFragmentWrapper(null, packageFragment, firstCompilationUnit));
				break;
			}
		}
		for (IJavaElement child : childArray) {
			if (child instanceof IPackageFragment) {
				analyzePackageFragment((IPackageFragment) child);
			}
		}
	}
}
