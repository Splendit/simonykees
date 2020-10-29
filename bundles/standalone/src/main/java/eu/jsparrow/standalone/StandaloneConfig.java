package eu.jsparrow.standalone;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.refactorer.StandaloneStatisticsData;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.rule.impl.FieldsRenamingRule;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationVisitorWrapper;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.standalone.exceptions.StandaloneException;
import eu.jsparrow.standalone.renaming.FieldsRenamingInstantiator;

/**
 * Class that contains all configuration needed to run headless version of
 * jSparrow Eclipse plugin.
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.1.1
 */
public class StandaloneConfig {

	private static final Logger logger = LoggerFactory.getLogger(StandaloneConfig.class);

	private static final String POM_FILE_NAME = "pom.xml"; //$NON-NLS-1$
	private static final String SEARCH_SCOPE = "workspace"; //$NON-NLS-1$

	private String path;

	private IJavaProject javaProject = null;
	protected CompilationUnitProvider compilationUnitsProvider;
	private String projectName;
	protected RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
	private boolean abort = false;
	private YAMLConfig yamlConfig;
	protected StandaloneStatisticsMetadata statisticsMetadata;

	// standalone statistics data
	protected StandaloneStatisticsData statisticsData;

	/**
	 * Constructor that calls collecting the compilation units.
	 * 
	 * @param javaProject
	 *            imported project with java sources
	 * @param path
	 *            path to the base directory of the project
	 * @param yamlConfig
	 *            the default yaml configuration file of the project
	 * @param projectName
	 *            name of the eclipse project to be created
	 * @throws CoreException
	 *             if the classpath entries cannot be added or the source files
	 *             cannot be parsed
	 * @throws StandaloneException
	 *             if the project cannot be created
	 */
	public StandaloneConfig(IJavaProject javaProject, String path, YAMLConfig yamlConfig,
			StandaloneStatisticsMetadata statisticsMetadata) throws CoreException, StandaloneException {

		this.javaProject = javaProject;
		this.projectName = javaProject.getProject()
			.getName();
		this.path = path;
		this.yamlConfig = yamlConfig;
		this.statisticsMetadata = statisticsMetadata;
		setUp();
	}

	/**
	 * Finds project's {@link ICompilationUnit}s.
	 * 
	 * @throws CoreException
	 *             the source files cannot be parsed
	 */
	protected void setUp() throws CoreException {
		List<ICompilationUnit> compilationUnits = findProjectCompilationUnits();
		compilationUnitsProvider = new CompilationUnitProvider(compilationUnits, yamlConfig.getExcludes());

		statisticsData = new StandaloneStatisticsData(compilationUnits.size(), projectName, statisticsMetadata,
				refactoringPipeline);
	}

	/**
	 * this method gets all {@link ICompilationUnit}s from the project and
	 * returns them.
	 * 
	 * @return list of {@link ICompilationUnit}s on project
	 * @throws JavaModelException
	 */
	List<ICompilationUnit> findProjectCompilationUnits() throws JavaModelException {
		List<ICompilationUnit> units = new ArrayList<>();

		logger.debug(Messages.StandaloneConfig_collectCompilationUnits);
		List<IPackageFragment> packages = Arrays.asList(javaProject.getPackageFragments());
		for (IPackageFragment mypackage : packages) {
			if (mypackage.containsJavaResources() && 0 != mypackage.getCompilationUnits().length) {
				mypackage.open(new NullProgressMonitor());

				units.addAll(Arrays.asList(mypackage.getCompilationUnits()));
			}
		}
		return units;
	}

	public void createRefactoringStates() throws StandaloneException {

		String loggerInfo = NLS.bind(Messages.Activator_debug_collectCompilationUnits, projectName);
		logger.info(loggerInfo);

		List<ICompilationUnit> compilationUnits = compilationUnitsProvider.getFilteredCompilationUnits();
		loggerInfo = NLS.bind(Messages.Activator_debug_numCompilationUnits, compilationUnits.size());
		logger.debug(loggerInfo);

		logUnusedExcludes();

		logger.debug(Messages.Activator_debug_createRefactoringStates);
		List<ICompilationUnit> containingErrors = new ArrayList<>();
		for (ICompilationUnit icu : compilationUnits) {
			if (abort) {
				String abortMessage = "Abort detected while creating refactoring states "; //$NON-NLS-1$
				throw new StandaloneException(abortMessage);
			}
			try {
				refactoringPipeline.createRefactoringState(icu, containingErrors);
			} catch (JavaModelException e) {
				String message = String.format("Cannot create refactoring states on %s ", projectName); //$NON-NLS-1$
				throw new StandaloneException(message, e);
			}
		}

		loggerInfo = NLS.bind(Messages.Activator_debug_numRefactoringStates, refactoringPipeline.getRefactoringStates()
			.size());

		logger.debug(loggerInfo);
	}

	private void logUnusedExcludes() {
		String loggerInfo;
		Collector<CharSequence, ?, String> collector = Collectors.joining(", "); //$NON-NLS-1$

		Set<String> unusedExcludedPackages = compilationUnitsProvider.getUnusedExcludedPackages();
		if (!unusedExcludedPackages.isEmpty()) {
			loggerInfo = NLS.bind(Messages.StandaloneConfig_unusedPackageExcludesWarning,
					unusedExcludedPackages.stream()
						.collect(collector));
			logger.warn(loggerInfo);
		}

		Set<String> unusedExcludedClasses = compilationUnitsProvider.getUnusedExcludedClasses();
		if (!unusedExcludedClasses.isEmpty()) {
			loggerInfo = NLS.bind(Messages.StandaloneConfig_unusedClassExcludesWarning, unusedExcludedClasses.stream()
				.collect(collector));
			logger.warn(loggerInfo);
		}
	}

	public void computeRefactoring() throws StandaloneException {
		if (!hasRefactoringStates()) {
			String loggerInfo = NLS.bind(Messages.StandaloneConfig_noRefactoringStates, projectName);
			logger.info(loggerInfo);
			return;
		}

		logger.debug(Messages.RefactoringInvoker_GetSelectedRules);
		RuleConfigurationWrapper ruleConfigurationWrapper = new RuleConfigurationWrapper(yamlConfig, getProjectRules());

		List<RefactoringRule> rules = new ArrayList<>();

		if (ruleConfigurationWrapper.isSelectedRule(FieldsRenamingRule.FIELDS_RENAMING_RULE_ID)) {
			Map<String, Boolean> options = ruleConfigurationWrapper.getFieldRenamingRuleConfigurationOptions();
			setUpRenamingRule(options).ifPresent(rules::add);
		}

		if (ruleConfigurationWrapper.isSelectedRule(StandardLoggerRule.STANDARD_LOGGER_RULE_ID)) {
			Map<String, String> options = ruleConfigurationWrapper.getLoggerRuleConfigurationOptions();
			setUpLoggerRule(options).ifPresent(rules::add);
		}

		List<RefactoringRule> selectedAutomaticRules = ruleConfigurationWrapper.getSelectedAutomaticRules();
		rules.addAll(selectedAutomaticRules);

		applyRules(rules);
		statisticsData.setMetricData();
		Instant now = Instant.now();
		statisticsData.setEndTime(now.getEpochSecond());
	}

	private Optional<StandardLoggerRule> setUpLoggerRule(Map<String, String> options) {
		StandardLoggerRule loggerRule = new StandardLoggerRule();
		loggerRule.activateOptions(options);
		loggerRule.calculateEnabledForProject(javaProject);
		return Optional.ofNullable(loggerRule)
			.filter(StandardLoggerRule::isEnabled);
	}

	private Optional<FieldsRenamingRule> setUpRenamingRule(Map<String, Boolean> options) throws StandaloneException {
		FieldsRenamingInstantiator factory = new FieldsRenamingInstantiator(javaProject,
				new FieldDeclarationVisitorWrapper(javaProject, SEARCH_SCOPE));

		List<ICompilationUnit> iCompilationUnits = refactoringPipeline.getRefactoringStates()
			.stream()
			.map(RefactoringState::getWorkingCopy)
			.map(ICompilationUnit::getPrimary)
			.collect(Collectors.toList());
		List<FieldMetaData> metaData = factory.findFields(iCompilationUnits, options);
		FieldsRenamingRule renamingRule = factory.createRule(metaData, compilationUnitsProvider);
		renamingRule.calculateEnabledForProject(javaProject);
		return Optional.of(renamingRule)
			.filter(FieldsRenamingRule::isEnabled);

	}

	private void applyRules(List<RefactoringRule> rules) throws StandaloneException {
		refactoringPipeline.setRules(rules);

		if (!rules.isEmpty()) {

			String loggerInfo = NLS.bind(Messages.Activator_standalone_SelectedRules, rules.size(), rules.toString());
			logger.info(loggerInfo);

			loggerInfo = NLS.bind(Messages.Activator_debug_startRefactoring, projectName);
			logger.info(loggerInfo);
			try {
				refactoringPipeline.doRefactoring(new NullProgressMonitor());
				loggerInfo = NLS.bind(Messages.SelectRulesWizard_rules_with_changes, projectName,
						refactoringPipeline.getRulesWithChangesAsString());
				logger.info(loggerInfo);
			} catch (RuleException e) {
				logger.debug(e.getMessage(), e);
				logger.error(e.getMessage());
			} catch (RefactoringException e) {
				String message = String.format("Cannot compute refactoring on %s.", projectName); //$NON-NLS-1$
				throw new StandaloneException(message, e);
			}
		} else {
			logger.info(Messages.Activator_standalone_noRulesSelected);
		}
	}

	public List<RefactoringRule> getProjectRules() {
		logger.debug(Messages.RefactoringInvoker_GetEnabledRulesForProject);
		return RulesContainer.getRulesForProject(getJavaProject(), true);
	}

	public void commitRefactoring() throws StandaloneException {
		if (!hasRefactoringStates()) {
			return;
		}
		String logInfo = NLS.bind(Messages.Activator_debug_commitRefactoring, projectName);
		logger.info(logInfo);
		try {
			refactoringPipeline.commitRefactoring();
			statisticsData.setEndTime(Instant.now()
				.getEpochSecond());
		} catch (RefactoringException | ReconcileException e) {
			throw new StandaloneException(String.format("Cannot commit refactoring on %s", projectName), e); //$NON-NLS-1$
		}
	}

	protected boolean hasRefactoringStates() {
		if (refactoringPipeline.getRefactoringStates()
			.isEmpty()) {
			logger.debug("No refactoring states on {} ", projectName); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	public void clearPipeline() {
		refactoringPipeline.setRules(new ArrayList<>());
		refactoringPipeline.clearStates();
	}

	/*** HELPER METHODS ***/

	protected IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	protected String getPomFilePath() {
		return path + File.separator + POM_FILE_NAME;
	}

	protected IProject getProject(IWorkspace workspace, String name) {
		return workspace.getRoot()
			.getProject(name);
	}

	public StandaloneStatisticsData getStatisticsData() {
		return statisticsData;
	}

	protected IJavaProject createJavaProject(IProject project) {
		return JavaCore.create(project);
	}

	/**
	 * Getter for IJavaProject
	 * 
	 * @return generated IJavaProject
	 */
	public IJavaProject getJavaProject() {
		return javaProject;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setAbortFlag() {
		this.abort = true;
	}
}
