package eu.jsparrow.ui.wizard.projects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import eu.jsparrow.ui.wizard.projects.wrapper.PackageFragmentWrapper;

/**
 * @since 4.17.0
 */
public class JavaCompilationUnitsCollector {

	private Map<IPackageFragment, List<ICompilationUnit>> packageToCompilationUnitsMap;

	public static List<IPackageFragmentRoot> collectSourcePackageFragmentRoots(IJavaProject javaProject)
			throws JavaModelException {
		IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
		List<IPackageFragmentRoot> sourcePackageFragmentRoots = new ArrayList<>();
		for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
			if (isSourcePackageFragmentRoot(packageFragmentRoot)) {
				sourcePackageFragmentRoots.add(packageFragmentRoot);
			}
		}
		return sourcePackageFragmentRoots;
	}

	private static boolean isSourcePackageFragmentRoot(IPackageFragmentRoot packageFragmentRoot)
			throws JavaModelException {

		return packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE &&
				!packageFragmentRoot.isExternal() &&
				!packageFragmentRoot.isArchive();
	}

	public Map<IPackageFragment, List<ICompilationUnit>> collectPackageToCompilationUnitsMap(IJavaProject javaProject)
			throws JavaModelException {
		List<IPackageFragmentRoot> sourcePackageFragmentRoots = collectSourcePackageFragmentRoots(javaProject);
		packageToCompilationUnitsMap = new HashMap<>();
		for (IPackageFragmentRoot sourcePackageFragmentRoot : sourcePackageFragmentRoots) {
			analyzePackageFragmentRoot(sourcePackageFragmentRoot);
		}

		return packageToCompilationUnitsMap;
	}

	@Deprecated
	public List<PackageFragmentWrapper> loadJavaPackageNodeList(IJavaProject javaProject)
			throws JavaModelException {
		List<IPackageFragmentRoot> sourcePackageFragmentRoots = collectSourcePackageFragmentRoots(javaProject);
		packageToCompilationUnitsMap = new HashMap<>();
		for (IPackageFragmentRoot sourcePackageFragmentRoot : sourcePackageFragmentRoots) {
			analyzePackageFragmentRoot(sourcePackageFragmentRoot);
		}

		return packageToCompilationUnitsMap.entrySet()
			.stream()
			.map(entry -> new PackageFragmentWrapper(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());
	}

	private void analyzePackageFragmentRoot(IPackageFragmentRoot packageFragmentRoot) throws JavaModelException {
		IJavaElement[] javaPackageChildren = packageFragmentRoot.getChildren();
		for (IJavaElement javaElement : javaPackageChildren) {
			if (javaElement instanceof IPackageFragment) {
				analyzePackageFragment((IPackageFragment) javaElement);
			}
		}
	}

	private void analyzePackageFragment(IPackageFragment packageFragment) throws JavaModelException {
		IJavaElement[] javaPackageChildren = packageFragment.getChildren();
		for (IJavaElement javaElement : javaPackageChildren) {
			if (javaElement instanceof IPackageFragment) {
				analyzePackageFragment((IPackageFragment) javaElement);
			} else if (javaElement instanceof ICompilationUnit) {
				storeCompilationUnit(packageFragment, (ICompilationUnit) javaElement);
			}
		}
	}

	private void storeCompilationUnit(IPackageFragment packageFragment, ICompilationUnit compilationUnit) {
		List<ICompilationUnit> compilationUnitList = packageToCompilationUnitsMap.computeIfAbsent(packageFragment,
				key -> new ArrayList<ICompilationUnit>());
		compilationUnitList.add(compilationUnit);
	}

}
