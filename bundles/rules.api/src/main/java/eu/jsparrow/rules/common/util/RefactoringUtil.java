package eu.jsparrow.rules.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Simonykees
 * 
 * @author Hannes Schweighofer, Andreja Sambolec, Hans-Jörg Schrödl, Matthias
 *         Webhofer
 * @since 0.9
 */
public final class RefactoringUtil {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringUtil.class);

	private static final String BACKSLASH_N = "\n"; //$NON-NLS-1$
	private static final String LINE_SEPARATOR_PROPERTY = "line.separator"; //$NON-NLS-1$

	/**
	 * Get the line separator for the current system, if none is found
	 * <code>&#92;n</code> is used
	 * 
	 * @since 0.9.2
	 */
	public static final String LINE_SEPARATOR = System.getProperty(LINE_SEPARATOR_PROPERTY, BACKSLASH_N);

	/**
	 * Constructor should never be called
	 */
	private RefactoringUtil() {
		// no constructor for a utility class
	}

	/**
	 *
	 * @return List[PackageFragment]
	 */
	@SuppressWarnings("unused")
	private static List<IJavaElement> getSubPackages(IPackageFragment p) {
		List<IJavaElement> result = new ArrayList<>();
		List<IJavaElement> packages;
		if (p.getParent() instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) p.getParent();
			try {
				packages = Arrays.asList(fragmentRoot.getChildren());
				packages.stream()
					.filter(packageElement -> StringUtils.startsWith(packageElement.getElementName(),
							p.getElementName())
							&& !packageElement.getElementName()
								.equals(p.getElementName()))
					.forEach(packageElement -> {
						result.add(packageElement);
						logger.debug("Subpackage found:" + packageElement.getElementName()); //$NON-NLS-1$
					});
			} catch (JavaModelException e) {
				logger.debug("Java Model Exception", e); //$NON-NLS-1$
			}
		}
		return result;
	}

	/**
	 * Creates the new parser to parse {@link ICompilationUnit}
	 * 
	 * @param compilationUnit
	 *            the Java model compilation unit whose source code is to be
	 *            parsed, or null if none
	 * 
	 * @return newly created parsed compilation unit
	 * 
	 * @since 0.9
	 */
	public static CompilationUnit parse(ICompilationUnit compilationUnit) {
		int astLevel = JdtCoreVersionBindingUtil.findJLSLevel(JdtCoreVersionBindingUtil.findCurrentJDTCoreVersion());
		ASTParser astParser = ASTParser.newParser(astLevel);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setSource(compilationUnit);
		astParser.setResolveBindings(true);
		IJavaProject iJavaProject = compilationUnit.getJavaProject();
		astParser.setCompilerOptions(iJavaProject.getOptions(true));
		return (CompilationUnit) astParser.createAST(null);
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
	 * @since 0.9
	 */
	public static DocumentChange generateDocumentChange(String name, Document document, TextEdit edit) {
		DocumentChange documentChange = new DocumentChange(name, document);
		documentChange.setEdit(edit);
		documentChange.setTextType("java"); //$NON-NLS-1$
		return documentChange;
	}

	/**
	 * Checks if the {@link ICompilationUnit} has any errors in the current
	 * configuration it is loaded. If no IMarker of severity error is present it
	 * passes.
	 * 
	 * @param iCompilationUnit
	 *            file to check
	 * @return returns {@code false} if NO error exists, otherwise {@code true}
	 * @since 1.2
	 * 
	 */
	public static boolean checkForSyntaxErrors(ICompilationUnit iCompilationUnit) {
		try {
			/**
			 * findMaxProblemSeverity returns the SEVERITY-Level of the highest
			 * order.
			 */

			boolean foundProblems = IMarker.SEVERITY_ERROR == iCompilationUnit.getResource()
				.findMaxProblemSeverity(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			if (foundProblems) {
				List<IMarker> markers = Arrays.asList(iCompilationUnit.getResource()
					.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE));
				for (IMarker marker : markers) {
					String message = String.format("Found marker on line %s, with message: %s", //$NON-NLS-1$
							marker.getAttribute(IMarker.LINE_NUMBER), marker.getAttribute(IMarker.MESSAGE));
					logger.info(message);
				}
			}
			return foundProblems;
		} catch (CoreException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}
}
