package eu.jsparrow.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;

/**
 * Utility class for Simonykees
 * 
 * @author Hannes Schweighofer, Andreja Sambolec, Hans-Jörg Schrödl
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
	 * Populates the list {@code result} with {@code ICompilationUnit}s found in
	 * {@code javaElements}
	 * 
	 * @param result
	 *            will contain compilation units
	 * @param javaElements
	 *            contains java elements which should be split up into
	 *            compilation units
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 * @since 0.9
	 */
	public static void collectICompilationUnits(List<ICompilationUnit> result, List<IJavaElement> javaElements,
			IProgressMonitor monitor) throws JavaModelException {

		/*
		 * Converts the monitor to a SubMonitor and sets name of task on
		 * progress monitor dialog. Size is set to number 100 and then scaled to
		 * size of the javaElements list. Each java element increases worked
		 * amount for same size.
		 */
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100).setWorkRemaining(javaElements.size());
		subMonitor.setTaskName(Messages.ProgressMonitor_SimonykeesUtil_collectICompilationUnits_taskName);
		for (IJavaElement javaElement : javaElements) {
			subMonitor.subTask(javaElement.getElementName());
			if (javaElement instanceof ICompilationUnit) {
				ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
				addCompilationUnit(result, compilationUnit);
			} else if (javaElement instanceof IPackageFragment) {
				IPackageFragment packageFragment = (IPackageFragment) javaElement;
				addCompilationUnit(result, packageFragment.getCompilationUnits());
			} else if (javaElement instanceof IPackageFragmentRoot) {
				IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) javaElement;
				collectICompilationUnits(result, Arrays.asList(packageFragmentRoot.getChildren()), subMonitor);
			} else if (javaElement instanceof IJavaProject) {
				IJavaProject javaProject = (IJavaProject) javaElement;
				for (IPackageFragment packageFragment : javaProject.getPackageFragments()) {
					addCompilationUnit(result, packageFragment.getCompilationUnits());
				}
			}

			/*
			 * If cancel is pressed on progress monitor, abort all and return,
			 * else continue
			 */
			if (subMonitor.isCanceled()) {
				return;
			} else {
				subMonitor.worked(1);
			}
		}
	}

	/**
	 *
	 * @return List[PackageFragment]
	 */
	@SuppressWarnings("unused")
	private static List<IJavaElement> getSubPackages(IPackageFragment p) {
		List<IJavaElement> result = new ArrayList<>();
		List<IJavaElement> packages;
		if (p.getParent() != null && p.getParent() instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) p.getParent();
			try {
				packages = Arrays.asList(fragmentRoot.getChildren());
				for (IJavaElement packageElement : packages) {
					if (packageElement.getElementName().startsWith(p.getElementName())
							&& !packageElement.getElementName().equals(p.getElementName())) {
						result.add(packageElement);
						logger.debug("Subpackage found:" + packageElement.getElementName()); //$NON-NLS-1$
					}

				}
			} catch (JavaModelException e) {
				logger.debug("Java Model Exception", e); //$NON-NLS-1$
			}
		}
		return result;
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
	 * @since 0.9
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
	 * @since 0.9
	 */
	private static void addCompilationUnit(List<ICompilationUnit> result, ICompilationUnit[] compilationUnits)
			throws JavaModelException {
		for (ICompilationUnit compilationUnit : compilationUnits) {
			addCompilationUnit(result, compilationUnit);
		}
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
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setSource(compilationUnit);
		astParser.setResolveBindings(true);
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		astParser.setCompilerOptions(options);
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
	 * @return returns true if no error exists, otherwise false
	 * @since 1.2
	 * 
	 */
	public static boolean checkForSyntaxErrors(ICompilationUnit iCompilationUnit) {
		try {
			/**
			 * findMaxProblemSeverity returns the SEVERITY-Level of the highest
			 * order.
			 */

			boolean foundProblems = IMarker.SEVERITY_ERROR == iCompilationUnit.getResource().findMaxProblemSeverity(
					IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			if (foundProblems) {
				logger.info("Check markers"); //$NON-NLS-1$
				List<IMarker> markers = Arrays.asList(iCompilationUnit.getResource()
						.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE));
				for (IMarker marker : markers) {
					String message = String.format("Found marker on line %s, with message: %s", //$NON-NLS-1$
							marker.getAttribute(IMarker.LOCATION), marker.getAttribute(IMarker.MESSAGE));
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
