package at.splendit.simonykees.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.TextEdit;

public final class SimonykeesUtil {
	
	private SimonykeesUtil() {
		// no constructor for a utility class
	}
	
	/**
	 * Populates the list {@code result} with {@code ICompilationUnit}s found in {@code javaElements} 
	 * 
	 * @param result will contain compilation units
	 * @param javaElements contains java elements which should be split up into compilation units
	 * @throws JavaModelException
	 */
	public static void collectICompilationUnits(List<ICompilationUnit> result, List<IJavaElement> javaElements) throws JavaModelException {
		for (IJavaElement javaElement : javaElements) {
			if (javaElement instanceof ICompilationUnit) {
				ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
				addCompilationUnit(result, compilationUnit);
			} else if (javaElement instanceof IPackageFragment) {
				IPackageFragment packageFragment = (IPackageFragment) javaElement;
				addCompilationUnit(result, packageFragment.getCompilationUnits());
			} else if (javaElement instanceof IPackageFragmentRoot) {
				IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) javaElement;
				collectICompilationUnits(result, Arrays.asList(packageFragmentRoot.getChildren()));
			} else if (javaElement instanceof IJavaProject) {
				IJavaProject javaProject = (IJavaProject) javaElement;
				for (IPackageFragment packageFragment : javaProject.getPackageFragments()) {
					addCompilationUnit(result, packageFragment.getCompilationUnits());
				}
			}
		}
	}
	
	private static void addCompilationUnit(List<ICompilationUnit> result, ICompilationUnit compilationUnit) throws JavaModelException {
		if (!compilationUnit.isConsistent()) {
			compilationUnit.makeConsistent(null);
		}
		if (!compilationUnit.isReadOnly()) {
			result.add(compilationUnit);
		}
	}
	
	private static void addCompilationUnit(List<ICompilationUnit> result, ICompilationUnit[] compilationUnits) throws JavaModelException {
		for (ICompilationUnit compilationUnit : compilationUnits) {
			addCompilationUnit(result, compilationUnit);
		}
	}

	public static void resetParser(ICompilationUnit compilationUnit, ASTParser astParser, Map<String, String> options) {
		astParser.setSource(compilationUnit);
		astParser.setResolveBindings(true);
		astParser.setCompilerOptions(options);
	}

	public static DocumentChange generateDocumentChange(String name, Document document, TextEdit edits) {
		DocumentChange documentChange = new DocumentChange(name, document);
		documentChange.setEdit(edits);
		return documentChange;
	}

}
