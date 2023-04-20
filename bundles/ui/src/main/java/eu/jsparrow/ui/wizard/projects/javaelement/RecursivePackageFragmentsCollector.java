package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class RecursivePackageFragmentsCollector {

	private List<IPackageFragment> packageFragmentList;

	public List<IPackageFragment> collectPackagesContainingSources(IPackageFragmentRoot sourcePackageFragmentRoot)
			throws JavaModelException {

		packageFragmentList = new ArrayList<>();

		IJavaElement[] childArray = sourcePackageFragmentRoot.getChildren();
		for (IJavaElement child : childArray) {
			if (child instanceof IPackageFragment) {
				analyzePackageFragment((IPackageFragment) child);
			}
		}
		return packageFragmentList;
	}

	private void analyzePackageFragment(IPackageFragment packageFragment) throws JavaModelException {
		IJavaElement[] childArray = packageFragment.getChildren();
		for (IJavaElement child : childArray) {
			if (child instanceof ICompilationUnit) {
				packageFragmentList.add(packageFragment);
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
