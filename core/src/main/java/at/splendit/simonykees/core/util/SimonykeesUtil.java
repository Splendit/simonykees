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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.TextEdit;

import at.splendit.simonykees.core.Activator;

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

	/**
	 * Reset parser
	 * @param compilationUnit
	 * @param astParser
	 * @param options
	 */
	public static void resetParser(ICompilationUnit compilationUnit, ASTParser astParser, Map<String, String> options) {
		astParser.setSource(compilationUnit);
		astParser.setResolveBindings(true);
		astParser.setCompilerOptions(options);
	}

	/**
	 * Generate a {@code DocumentChange} from a {@code Document} and a {@code TextEdit}
	 * @param name of the change
	 * @param document
	 * @param edit
	 * @return
	 */
	public static DocumentChange generateDocumentChange(String name, Document document, TextEdit edit) {
		DocumentChange documentChange = new DocumentChange(name, document);
		documentChange.setEdit(edit);
		return documentChange;
	}

	/**
	 * Commit changes to a {@code ICompilationUnit} and discard the working copy
	 * @param workingCopy
	 * @throws JavaModelException
	 */
	public static void commitAndDiscardWorkingCopy(ICompilationUnit workingCopy) throws JavaModelException {
		workingCopy.commitWorkingCopy(false, null);
		workingCopy.discardWorkingCopy();
	}
	
	/**
	 * Apply a single rule to a {@code ICompilationUnit}, changes are not committed to {@code workingCopy}
	 * 
	 * @param workingCopy
	 * @param ruleClazz
	 * @return a {@code DocumentChange} containing the old and new source
	 * @throws ReflectiveOperationException
	 * @throws JavaModelException
	 */
	public static DocumentChange applyRule(ICompilationUnit workingCopy, Class<? extends ASTVisitor> ruleClazz) throws ReflectiveOperationException, JavaModelException {
		final ASTParser astParser = ASTParser.newParser(AST.JLS8);
		resetParser(workingCopy, astParser, workingCopy.getJavaProject().getOptions(true));
		final CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);
		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		
		Activator.log("Init rule [" + ruleClazz.getName() + "]");
		ASTVisitor rule = ruleClazz.getConstructor(ASTRewrite.class).newInstance(astRewrite);
		astRoot.accept(rule);
		
		Document document = new Document(workingCopy.getSource());
		TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject().getOptions(true));
		
		workingCopy.applyTextEdit(edits, null);
		workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		
		return generateDocumentChange(ruleClazz.getSimpleName(), document, edits);
	}

}
