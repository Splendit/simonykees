package eu.jsparrow.standalone;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

	private Set<String> usedExcludedPackages = new HashSet<>();
	private Set<String> usedExcludedClasses = new HashSet<>();

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

		Collector<CharSequence, ?, String> collector = Collectors.joining(","); //$NON-NLS-1$

		List<String> excludedPackages = excludes.getExcludePackages();
		String logInfo = excludedPackages.stream()
			.collect(collector);
		logger.debug("Excluded packages: {} ", logInfo); //$NON-NLS-1$

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
			boolean isExcludedPackage = exludedPackages.contains(packageName);
			boolean isExcludedClass = exludedClasses.contains(className);
			boolean isIncluded = !isExcludedPackage && !isExcludedClass;
			if (!isIncluded) {
				logger.debug("Excluding compilation unit {}", className); //$NON-NLS-1$
				if (isExcludedPackage) {
					usedExcludedPackages.add(packageName);
				}
				if (isExcludedClass) {
					usedExcludedClasses.add(className);
				}
			}
			return isIncluded;
		} catch (JavaModelException e) {
			logger.warn("Error occurred while trying to get package declarations", e); //$NON-NLS-1$
			return false;
		}
	}

	/**
	 * Filters all excluded classes defined in yaml file with class names that
	 * occurred at least once in list of compilation units
	 * 
	 * @return classes defined in excludes section of yaml file that did not
	 *         occur in project
	 */
	public Set<String> getUnusedExcludedClasses() {
		return excludes.getExcludeClasses()
			.stream()
			.filter(excludedClass -> !usedExcludedClasses.contains(excludedClass))
			.collect(Collectors.toSet());
	}

	/**
	 * Filters all excluded packages defined in yaml file with package names
	 * that occurred at least once as package declaration in list of compilation
	 * units
	 * 
	 * @return packages defined in excludes section of yaml file that did not
	 *         occur in project
	 */
	public Set<String> getUnusedExcludedPackages() {
		return excludes.getExcludePackages()
			.stream()
			.filter(excludedPackage -> !usedExcludedPackages.contains(excludedPackage))
			.collect(Collectors.toSet());
	}
}
