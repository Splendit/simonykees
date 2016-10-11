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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
//import org.eclipse.jdt.internal.corext.refactoring.util.NoCommentSourceRangeComputer;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.TextEdit;

import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

public final class SimonykeesUtil {

	private SimonykeesUtil() {
		// no constructor for a utility class
	}

	/**
	 * Populates the list {@code result} with {@code ICompilationUnit}s found in
	 * {@code javaElements}
	 * 
	 * @param result
	 *            will contain compilation units
	 * @param javaElements
	 *            contains java elements which should be split up into
	 *            compilation units
	 * @throws JavaModelException
	 *             * if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 */
	public static void collectICompilationUnits(List<ICompilationUnit> result, List<IJavaElement> javaElements)
			throws JavaModelException {
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

	/**
	 * 
	 * @param result
	 *            List of {@link ICompilationUnit} where the
	 *            {@code compilationUnit} is added
	 * @param compilationUnit
	 *            {@link ICompilationUnit} that is tested for consistency and
	 *            write access.
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 */

	private static void addCompilationUnit(List<ICompilationUnit> result, ICompilationUnit compilationUnit)
			throws JavaModelException {
		if (!compilationUnit.isConsistent()) {
			compilationUnit.makeConsistent(null);
		}
		if (!compilationUnit.isReadOnly()) {
			result.add(compilationUnit);
		}
	}

	/**
	 * 
	 * @param result
	 *            List of {@link ICompilationUnit} where the
	 *            {@code compilationUnits} are added
	 * @param compilationUnits
	 *            array of {@link ICompilationUnit} which are loaded
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 */
	private static void addCompilationUnit(List<ICompilationUnit> result, ICompilationUnit[] compilationUnits)
			throws JavaModelException {
		for (ICompilationUnit compilationUnit : compilationUnits) {
			addCompilationUnit(result, compilationUnit);
		}
	}

	/**
	 * Reset parser to parse next {@link ICompilationUnit} with the given
	 * {@code options}
	 * 
	 * @param compilationUnit
	 *            the Java model compilation unit whose source code is to be
	 *            parsed, or null if none
	 * @param astParser
	 *            A Java language parser for creating abstract syntax trees
	 *            (ASTs).
	 * @param options
	 *            the table of options (key type: String; value type: String),
	 *            or null to set it back to the default
	 */
	public static void resetParser(ICompilationUnit compilationUnit, ASTParser astParser, Map<String, String> options) {
		astParser.setSource(compilationUnit);
		astParser.setResolveBindings(true);
		astParser.setCompilerOptions(options);
	}

	/**
	 * Generate a {@code DocumentChange} from a {@code Document} and a
	 * {@code TextEdit}
	 * 
	 * @param name
	 *            of the change
	 * @param document
	 *            where the change is applied to
	 * @param edit
	 *            is the actual change that will be made
	 * @return returns the {@link TextEdit} that is wrapped in a
	 *         {@link DocumentChange}
	 */
	public static DocumentChange generateDocumentChange(String name, Document document, TextEdit edit) {
		DocumentChange documentChange = new DocumentChange(name, document);
		documentChange.setEdit(edit);
		documentChange.setTextType("java"); //$NON-NLS-1$
		return documentChange;
	}

	/**
	 * Commit changes to a {@code ICompilationUnit} and discard the working copy
	 * 
	 * @param workingCopy
	 *            java document working copy where changes are present
	 * @throws JavaModelException
	 *             if this working copy could not commit. Reasons include: A
	 *             org.eclipse.core.runtime.CoreException occurred while
	 *             updating an underlying resource This element is not a working
	 *             copy (INVALID_ELEMENT_TYPES) A update conflict (described
	 *             above) (UPDATE_CONFLICT) if this working copy could not
	 *             return in its original mode.
	 */
	public static void commitAndDiscardWorkingCopy(ICompilationUnit workingCopy) throws JavaModelException {
		workingCopy.commitWorkingCopy(false, null);
		workingCopy.discardWorkingCopy();
	}

	/**
	 * Apply a single rule to a {@code ICompilationUnit}, changes are not
	 * committed to {@code workingCopy}
	 * 
	 * @param workingCopy
	 *            working copy of the java document that was selected for a rule
	 *            application
	 * @param ruleClazz
	 *            class object of the Rule.class that is applied to the
	 *            {@link ICompilationUnit} workingCopy
	 * @return a {@code DocumentChange} containing the old and new source or
	 *         null if no changes were detected
	 * @throws ReflectiveOperationException
	 *             is thrown if the {@code ruleClazz} has no default constructor
	 *             that could be invoked with newInstance
	 * @throws JavaModelException
	 *             if an exception occurs while accessing its corresponding
	 *             resource
	 */
	public static DocumentChange applyRule(ICompilationUnit workingCopy,
			Class<? extends AbstractASTRewriteASTVisitor> ruleClazz)
			throws ReflectiveOperationException, JavaModelException {
		final ASTParser astParser = ASTParser.newParser(AST.JLS8);
		resetParser(workingCopy, astParser, workingCopy.getJavaProject().getOptions(true));
		final CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);
		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		// FIXME resolves that comments are manipulated during astrewrite
		//
		// Solution from https://bugs.eclipse.org/bugs/show_bug.cgi?id=250142
		// The best solution for such problems is usually to call
		// ASTRewrite#setTargetSourceRangeComputer(TargetSourceRangeComputer)
		// and set a NoCommentSourceRangeComputer or a properly configured
		// TightSourceRangeComputer.

		// astRewrite.setTargetSourceRangeComputer(new
		// NoCommentSourceRangeComputer());

		AbstractASTRewriteASTVisitor rule = ruleClazz.newInstance();
		rule.setAstRewrite(astRewrite);
		astRoot.accept(rule);

		Document document = new Document(workingCopy.getSource());
		TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject().getOptions(true));

		if (edits.hasChildren()) {

			/*
			 * The TextEdit instance changes as soon as it is applied to the
			 * working copy. This results in an incorrect preview of the
			 * DocumentChange. To fix this issue, a copy of the TextEdit is used
			 * for the DocumentChange.
			 */
			DocumentChange documentChange = generateDocumentChange(ruleClazz.getSimpleName(), document, edits.copy());

			workingCopy.applyTextEdit(edits, null);
			workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

			return documentChange;
		} else {
			return null;
		}

	}

}
