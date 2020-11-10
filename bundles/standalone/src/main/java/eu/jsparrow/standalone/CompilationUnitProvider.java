package eu.jsparrow.standalone;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLExcludes;
import eu.jsparrow.standalone.exceptions.StandaloneException;

/**
 * Provides functionalities for filtering out {@link ICompilationUnit}s that are
 * excluded in the yaml configuration file.
 * 
 * @since 2.6.0
 *
 */
public class CompilationUnitProvider {

	private List<ICompilationUnit> compilationUnits;
	private static final String GLOB_ALL = "glob:**"; //$NON-NLS-1$

	private YAMLExcludes excludes;

	private Set<String> usedExcludedPackages = new HashSet<>();
	private Set<String> usedExcludedClasses = new HashSet<>();
	private List<PathMatcher> selectedSourceMatchers;
	private String selectedSources;

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
	 * @param selectedSources
	 *            glob pattern matchers for specifying the sources to be
	 *            refactored. Use line breaks to separate multiple patterns.
	 */
	public CompilationUnitProvider(List<ICompilationUnit> compilationUnits, YAMLExcludes excludes,
			String selectedSources) {
		this.compilationUnits = compilationUnits;
		this.excludes = excludes;
		this.selectedSources = selectedSources;
		String[] sources = selectedSources.split(System.lineSeparator());
		this.selectedSourceMatchers = Arrays.asList(sources)
			.stream()
			.map(String::trim)
			.filter(pattern -> !pattern.isEmpty())
			.map(source -> String.join(File.separator, GLOB_ALL, source))
			.map(pattern -> FileSystems.getDefault()
				.getPathMatcher(pattern))
			.collect(Collectors.toList());
	}

	/**
	 * Finds the list of {@link ICompilationUnit}s from
	 * {@link #compilationUnits} that are allowed to be refactored.
	 * 
	 * @return the list of compilation units that are allowed to be refactored.
	 */
	public List<ICompilationUnit> getFilteredCompilationUnits() {

		Collector<CharSequence, ?, String> collector = Collectors.joining(","); //$NON-NLS-1$
		logger.info("Selected sources: {}.", selectedSources); //$NON-NLS-1$
		List<String> excludedPackages = Optional.ofNullable(excludes)
			.map(YAMLExcludes::getExcludePackages)
			.orElse(Collections.emptyList());
		String logInfo = excludedPackages.stream()
			.collect(collector);
		logger.debug("Excluded packages: {} ", logInfo); //$NON-NLS-1$

		List<String> exludedClasses = Optional.ofNullable(excludes)
			.map(YAMLExcludes::getExcludeClasses)
			.orElse(Collections.emptyList());
		logInfo = exludedClasses.stream()
			.collect(collector);
		logger.debug("Excluded classes: {} ", logInfo); //$NON-NLS-1$

		return compilationUnits.stream()
			.filter(this::isSelected)
			.filter(compilationUnit -> isIncludedForRefactoring(compilationUnit, excludedPackages, exludedClasses))
			.collect(Collectors.toList());
	}

	private boolean isSelected(ICompilationUnit compilationUnit) {
		if (selectedSourceMatchers.isEmpty()) {
			return false;
		}
		IPath compUnitPath = compilationUnit.getPath();
		IPath iPath = compUnitPath.makeRelative();
		File file = iPath.toFile();
		Path path = file.toPath();
		return this.selectedSourceMatchers.stream()
			.anyMatch(matcher -> matcher.matches(path));
	}

	private boolean isIncludedForRefactoring(ICompilationUnit compUnit, List<String> exludedPackages,
			List<String> exludedClasses) {
		try {
			String packageName = computePackageName(compUnit);
			String className = computeCompilationUnitName(compUnit);

			boolean isExcludedPackage = exludedPackages.contains(packageName);
			boolean isExcludedClass = exludedClasses.contains(className);

			/*
			 * Bugfix SIM-1338
			 */
			boolean isInfoFile = "module-info.java".equalsIgnoreCase(compUnit.getElementName()) //$NON-NLS-1$
					|| "package-info.java".equalsIgnoreCase(compUnit.getElementName()); //$NON-NLS-1$

			boolean isIncluded = !isExcludedPackage && !isExcludedClass && !isInfoFile;
			if (!isIncluded) {
				logger.debug("Excluding compilation unit {}", className); //$NON-NLS-1$
				if (isExcludedPackage) {
					usedExcludedPackages.add(packageName);
				}
				if (isExcludedClass || isInfoFile) {
					usedExcludedClasses.add(className);
				}
			}
			return isIncluded;
		} catch (StandaloneException e) {
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

	/**
	 * Checks if any of the compilation units in the given list belongs to the
	 * excluded packages or files.
	 * 
	 * @param compilationUnitsWithReferences
	 *            list of compilation units to be checked.
	 * @return {@code true} if the above condition is satisfied or {@code false}
	 *         otherwise.
	 * @throws StandaloneException
	 *             if the name of a compilation unit or its package could not be
	 *             found.
	 */
	public boolean containsExcludedFiles(List<ICompilationUnit> compilationUnitsWithReferences)
			throws StandaloneException {
		List<String> excludedClasses = excludes.getExcludeClasses();
		List<String> excludedPackages = excludes.getExcludePackages();

		List<String> compilationUnitNames = new ArrayList<>();

		for (ICompilationUnit iCompilationUnit : compilationUnitsWithReferences) {
			String name = computeCompilationUnitName(iCompilationUnit);
			compilationUnitNames.add(name);
		}

		Set<String> packages = new HashSet<>();
		for (ICompilationUnit iCompilationUnit : compilationUnitsWithReferences) {
			String packageName = computePackageName(iCompilationUnit);
			packages.add(packageName);
		}

		boolean hasReferencesOnExcludedClasses = excludedClasses.stream()
			.anyMatch(compilationUnitNames::contains);
		if (hasReferencesOnExcludedClasses) {
			return true;
		}
		return excludedPackages.stream()
			.anyMatch(packages::contains);
	}

	private String computeCompilationUnitName(ICompilationUnit icu) throws StandaloneException {
		String name = icu.getElementName();
		String packageName = computePackageName(icu);
		if (packageName.isEmpty()) {
			return name;
		}
		return packageName + "." + name; //$NON-NLS-1$
	}

	private String computePackageName(ICompilationUnit icu) throws StandaloneException {
		String packageName = ""; //$NON-NLS-1$

		IPackageDeclaration[] packages;
		try {
			packages = icu.getPackageDeclarations();
		} catch (JavaModelException e) {
			throw new StandaloneException(
					String.format("Cannot find package declaration of the compilation unit %s", icu.getElementName())); //$NON-NLS-1$
		}

		if (packages.length != 0) {
			packageName = packages[0].getElementName();
		}

		return packageName;
	}
}
