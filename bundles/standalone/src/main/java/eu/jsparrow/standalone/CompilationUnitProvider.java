package eu.jsparrow.standalone;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLExcludes;

public class CompilationUnitProvider {

	private List<ICompilationUnit> compilationUnits;

	private YAMLExcludes excludes;

	private static final Logger logger = LoggerFactory.getLogger(CompilationUnitProvider.class);

	public CompilationUnitProvider(List<ICompilationUnit> compilationUnits, YAMLExcludes excludes) {
		this.compilationUnits = compilationUnits;
		this.excludes = excludes;
	}

	public List<ICompilationUnit> getFilteredCompilationUnits() {

		return compilationUnits.stream()
			.filter(this::isIncludedForRefactoring)
			.collect(Collectors.toList());
	}

	private boolean isIncludedForRefactoring(ICompilationUnit compUnit) {
		try {
			String cuPackage = compUnit.getPackageDeclarations()[0].getElementName();
			return !excludes.getExcludePackages()
				.contains(cuPackage)
					&& !excludes.getExcludeClasses()
						.contains(cuPackage + "." + compUnit.getElementName()); //$NON-NLS-1$
		} catch (JavaModelException e) {
			logger.warn("Error occurred while trying to get package declarations", e); //$NON-NLS-1$
			return false;
		}
	}

}
