package eu.jsparrow.standalone;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLExcludes;

/**
 * Provides functionalities for filtering out {@link ICompilationUnit}s that are
 * excluded in the yaml configuration file.
 * 
 * @since 2.6.0
 *
 */
public class CompilationUnitProvider {

	private List<ICompilationUnit> compilationUnits;

	private YAMLExcludes excludes;

	private static final Logger logger = LoggerFactory.getLogger(CompilationUnitProvider.class);

	/**
	 * Creates an instance of {@link CompilationUnitProvider} from the list of
	 * all {@link ICompilationUnit} of a project and an instance of
	 * {@link YAMLExcludes} which contains the modules, packages and the classes
	 * that should not be refactored.
	 * 
	 * @param compilationUnits
	 *            list of the {@link ICompilationUnit}s of a project
	 * @param excludes
	 *            an instance of {@link YAMLExcludes} representing the modules,
	 *            packages and classes that should be excluded from refactoring.
	 */
	public CompilationUnitProvider(List<ICompilationUnit> compilationUnits, YAMLExcludes excludes) {
		this.compilationUnits = compilationUnits;
		this.excludes = excludes;
	}

	/**
	 * Finds the list of {@link ICompilationUnit}s from
	 * {@link #compilationUnits} that are allowed to be refactored.
	 * 
	 * @return the list of compilation units that are allowed to be refactored.
	 */
	public List<ICompilationUnit> getFilteredCompilationUnits() {

		Collector<CharSequence, ?, String> collector = Collectors.joining("\n", ",\n", "."); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		List<String> excludedPackages = excludes.getExcludePackages();
		String logInfo = excludedPackages.stream()
			.collect(collector);
		logger.debug("Exclueded packages: {} ", logInfo); //$NON-NLS-1$

		List<String> exludedClasses = excludes.getExcludeClasses();
		logInfo = exludedClasses.stream()
			.collect(collector);
		logger.debug("Excluded classes: {} ", logInfo); //$NON-NLS-1$

		return compilationUnits.stream()
			.filter(compilationUnit -> isIncludedForRefactoring(compilationUnit, excludedPackages, exludedClasses))
			.collect(Collectors.toList());
	}

	private boolean isIncludedForRefactoring(ICompilationUnit compUnit, List<String> exludedPackages,
			List<String> exludedClasses) {
		try {
			IPackageDeclaration[] packageDeclarations = compUnit.getPackageDeclarations();
			String packageName = ""; //$NON-NLS-1$
			String className = compUnit.getElementName();
			if (packageDeclarations.length != 0) {
				packageName = packageDeclarations[0].getElementName();
				className = packageName + "." + className; //$NON-NLS-1$
			}
			boolean isIncluded = !exludedPackages.contains(packageName) && !exludedClasses.contains(className);
			if (!isIncluded) {
				logger.debug("Excluding compilation unit {}", className); //$NON-NLS-1$
			}
			return isIncluded;
		} catch (JavaModelException e) {
			logger.warn("Error occurred while trying to get package declarations", e); //$NON-NLS-1$
			return false;
		}
	}

}
