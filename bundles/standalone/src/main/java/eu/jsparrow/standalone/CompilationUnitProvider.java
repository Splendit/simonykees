package eu.jsparrow.standalone;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLExcludes;
import eu.jsparrow.i18n.Messages;

public class CompilationUnitProvider {

	private StandaloneConfig standaloneConfig;

	private YAMLExcludes excludes;

	private static final Logger logger = LoggerFactory.getLogger(CompilationUnitProvider.class);

	public CompilationUnitProvider(StandaloneConfig standaloneConfig, YAMLExcludes excludes) {
		this.standaloneConfig = standaloneConfig;
		this.excludes = excludes;
	}

	public List<ICompilationUnit> getFilteredCompilationUnits() {
		logger.info(Messages.Activator_debug_collectCompilationUnits);
		List<ICompilationUnit> compUnits = standaloneConfig.getICompilationUnits();

		String loggerInfo = NLS.bind(Messages.Activator_debug_numCompilationUnits, compUnits.size());
		logger.debug(loggerInfo);

		return compUnits.stream()
			.filter(compUnit -> {
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
			})
			.collect(Collectors.toList());
	}

}
