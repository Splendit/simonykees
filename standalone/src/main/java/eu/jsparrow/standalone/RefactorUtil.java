package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.config.YAMLConfigUtil;
import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.4.0
 */
public class RefactorUtil {
	private static final Logger logger = LoggerFactory.getLogger(RefactorUtil.class);

	// CONSTANTS
	protected static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	private static final String JAVA_TMP = "java.io.tmpdir"; //$NON-NLS-1$
	private static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	private static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$
	private static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	private static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	protected static final String DEPENDENCIES_FOLDER_CONSTANT = "deps"; //$NON-NLS-1$
	protected static final String MAVEN_NATURE_CONSTANT = "org.eclipse.m2e.core.maven2Nature"; //$NON-NLS-1$
	protected static final String PROJECT_DESCRIPTION_CONSTANT = ".project"; //$NON-NLS-1$
	protected static final String PROJECT_JAVA_VERSION = "PROJECT.JAVA.VERSION"; //$NON-NLS-1$
	private static final String MAVEN_HOME_KEY = "MAVEN.HOME"; //$NON-NLS-1$

	protected StandaloneConfig standaloneConfig;
	private File directory;

	/**
	 * prepare and start the refactoring process
	 * 
	 * @param context
	 * @throws YAMLConfigException
	 * @throws MavenInvocationException
	 * @throws CoreException
	 * @throws IOException
	 */
	public void startRefactoring(BundleContext context, RefactoringPipeline refactoringPipeline)
			throws YAMLConfigException, CoreException, MavenInvocationException, IOException {
		String loggerInfo;

		YAMLConfig config = getConfiguration(context);
		prepareWorkingDirectory();

		loadStandaloneConfig(context);

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> projectRules = getProjectRules();
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> selectedRules = getSelectedRules(config,
				projectRules);
		if (selectedRules != null && !selectedRules.isEmpty()) {
			// Create refactoring pipeline and set rules
			refactoringPipeline.setRules(selectedRules);

			loggerInfo = NLS.bind(Messages.Activator_standalone_SelectedRules, selectedRules.size(),
					selectedRules.toString());
			logger.info(loggerInfo);

			logger.info(Messages.Activator_debug_collectCompilationUnits);

			List<ICompilationUnit> compUnits = standaloneConfig.getCompUnits();

			loggerInfo = NLS.bind(Messages.Activator_debug_numCompilationUnits, compUnits.size());
			logger.debug(loggerInfo);

			logger.debug(Messages.Activator_debug_createRefactoringStates);

			refactoringPipeline.createRefactoringStates(compUnits);

			loggerInfo = NLS.bind(Messages.Activator_debug_numRefactoringStates,
					refactoringPipeline.getRefactoringStates()
						.size());
			logger.debug(loggerInfo);

			// Do refactoring
			try {
				logger.info(Messages.Activator_debug_startRefactoring);
				refactoringPipeline.doRefactoring(new NullProgressMonitor());
			} catch (RefactoringException | RuleException e) {
				logger.debug(e.getMessage(), e);
				logger.error(e.getMessage());
				return;
			}

			loggerInfo = NLS.bind(Messages.SelectRulesWizard_rules_with_changes, getJavaProject().getElementName(),
					refactoringPipeline.getRulesWithChangesAsString());
			logger.info(loggerInfo);

			// Commit refactoring
			try {
				logger.info(Messages.Activator_debug_commitRefactoring);
				refactoringPipeline.commitRefactoring();
			} catch (RefactoringException | ReconcileException e) {
				logger.debug(e.getMessage(), e);
				logger.error(e.getMessage());
				return;
			}
		} else {
			logger.info(Messages.Activator_standalone_noRulesSelected);
		}
	}

	/**
	 * cleans classpath and temp directory
	 * 
	 * @throws IOException
	 */
	public void cleanUp() throws IOException {
		try {
			if (standaloneConfig != null) {
				standaloneConfig.cleanUp();
			}
		} catch (JavaModelException | MavenInvocationException e) {
			logger.debug(e.getMessage(), e);
			logger.error(e.getMessage());
		}

		// CLEAN
		if (directory != null && directory.exists()) {
			try {
				deleteChildren(directory);
				Files.delete(directory.toPath());
			} catch (IOException e) {
				logger.debug(e.getMessage(), e);
				logger.error(e.getMessage());
			}
		}
	}

	/**
	 * gets the configuration from the given path in context
	 * 
	 * @param context
	 * @return the read configuration
	 * @throws YAMLConfigException
	 */
	private YAMLConfig getConfiguration(BundleContext context) throws YAMLConfigException {
		String configFilePath = context.getProperty(CONFIG_FILE_PATH);
		String profile = context.getProperty(SELECTED_PROFILE);

		String loggerInfo = NLS.bind(Messages.Activator_standalone_LoadingConfiguration, configFilePath);
		logger.info(loggerInfo);

		YAMLConfig config = getYamlConfig(configFilePath, profile);

		String selectedProfile = config.getSelectedProfile();

		loggerInfo = NLS.bind(Messages.Activator_standalone_SelectedProfile,
				(selectedProfile == null) ? Messages.Activator_standalone_None : selectedProfile);
		logger.info(loggerInfo);

		return config;
	}

	private void prepareWorkingDirectory() {
		String file = System.getProperty(JAVA_TMP);
		directory = new File(file + File.separator + JSPARROW_TEMP_FOLDER).getAbsoluteFile();

		if (directory.exists() || directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
		}
	}

	/**
	 * Recursively deletes all sub-folders from received folder.
	 * 
	 * @param parentDirectory
	 *            directory which content is to be deleted
	 * @throws IOException
	 */
	private void deleteChildren(File parentDirectory) {
		String[] children = parentDirectory.list();
		if (children != null) {
			for (String file : Arrays.asList(children)) {
				File currentFile = new File(parentDirectory.getAbsolutePath(), file);
				if (currentFile.isDirectory()) {
					deleteChildren(currentFile);
				}

				try {
					Files.delete(currentFile.toPath());
				} catch (IOException e) {
					logger.debug(e.getMessage(), e);
					logger.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * loads a new {@link StandaloneConfig} with the properties found in
	 * {@link BundleContext}
	 * 
	 * @param context
	 * @throws CoreException
	 * @throws MavenInvocationException
	 * @throws IOException
	 */
	protected void loadStandaloneConfig(BundleContext context)
			throws CoreException, MavenInvocationException, IOException {
		String projectPath = context.getProperty(PROJECT_PATH_CONSTANT);
		String compilerCompliance = context.getProperty(PROJECT_JAVA_VERSION);
		String mavenHome = context.getProperty(MAVEN_HOME_KEY);

		standaloneConfig = new StandaloneConfig(projectPath, compilerCompliance, mavenHome);
	}

	protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getProjectRules() {
		logger.debug(Messages.RefactorUtil_GetEnabledRulesForProject);
		return RulesContainer.getRulesForProject(standaloneConfig.getJavaProject(), true);
	}

	protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getSelectedRules(YAMLConfig config,
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> projectRules) throws YAMLConfigException {
		logger.debug(Messages.RefactorUtil_GetSelectedRules);
		return YAMLConfigUtil.getSelectedRulesFromConfig(config, projectRules);
	}

	protected YAMLConfig getYamlConfig(String configFilePath, String profile) throws YAMLConfigException {
		return YAMLConfigUtil.readConfig(configFilePath, profile);
	}

	protected IJavaProject getJavaProject() {
		return standaloneConfig.getJavaProject();
	}
}
