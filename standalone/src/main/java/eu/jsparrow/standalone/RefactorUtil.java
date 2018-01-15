package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
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
	private static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME"; //$NON-NLS-1$
	private static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$
	private static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	private static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	protected static final String DEPENDENCIES_FOLDER_CONSTANT = "deps"; //$NON-NLS-1$
	protected static final String MAVEN_NATURE_CONSTANT = "org.eclipse.m2e.core.maven2Nature"; //$NON-NLS-1$
	protected static final String PROJECT_DESCRIPTION_CONSTANT = ".project"; //$NON-NLS-1$

	private static StandaloneConfig standaloneConfig;
	private static File directory;

	private RefactorUtil() {

	}

	/**
	 * prepare and start the refactoring process
	 * 
	 * @param context
	 * @throws YAMLConfigException
	 */
	public static void startRefactoring(BundleContext context) throws YAMLConfigException {
		String loggerInfo;

		YAMLConfig config = getConfiguration(context);
		prepareWorkingDirectory();

		String projectPath = context.getProperty(PROJECT_PATH_CONSTANT);
		String projectName = context.getProperty(PROJECT_NAME_CONSTANT);

		standaloneConfig = new StandaloneConfig(projectName, projectPath);

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> projectRules = RulesContainer
			.getRulesForProject(standaloneConfig.getJavaProject(), true);
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> selectedRules = YAMLConfigUtil
			.getSelectedRulesFromConfig(config, projectRules);

		if (selectedRules != null && !selectedRules.isEmpty()) {
			// Create refactoring pipeline and set rules
			RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
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
				logger.error(e.getMessage(), e);
				return;
			}

			// Commit refactoring
			try {
				logger.info(Messages.Activator_debug_commitRefactoring);
				refactoringPipeline.commitRefactoring();
			} catch (RefactoringException | ReconcileException e) {
				logger.error(e.getMessage(), e);
				return;
			}
		} else {
			logger.info(Messages.Activator_standalone_noRulesSelected);
		}
	}

	/**
	 * cleans classpath and temp directory
	 */
	public static void cleanUp() {
		try {
			standaloneConfig.cleanUp();
		} catch (JavaModelException | IOException e) {
			logger.error(e.getMessage(), e);
		}

		// CLEAN
		if (directory.exists()) {
			deleteChildren(directory);
		}
	}

	/**
	 * gets the configuration from the given path in context
	 * 
	 * @param context
	 * @return the read configuration
	 * @throws YAMLConfigException
	 */
	private static YAMLConfig getConfiguration(BundleContext context) throws YAMLConfigException {
		String configFilePath = context.getProperty(CONFIG_FILE_PATH);
		String profile = context.getProperty(SELECTED_PROFILE);

		String loggerInfo = NLS.bind(Messages.Activator_standalone_LoadingConfiguration, configFilePath);
		logger.info(loggerInfo);

		YAMLConfig config = YAMLConfigUtil.readConfig(configFilePath, profile);

		String selectedProfile = config.getSelectedProfile();
		loggerInfo = NLS.bind(Messages.Activator_standalone_SelectedProfile,
				(selectedProfile == null) ? Messages.Activator_standalone_None : selectedProfile);
		logger.info(loggerInfo);

		return config;
	}

	private static void prepareWorkingDirectory() {
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
	private static void deleteChildren(File parentDirectory) {
		String[] children = parentDirectory.list();
		if (children != null) {
			for (String file : Arrays.asList(children)) {
				File currentFile = new File(parentDirectory.getAbsolutePath(), file);
				if (currentFile.isDirectory() && !("target".equals(currentFile.getName()))) { //$NON-NLS-1$
					deleteChildren(currentFile);
				}

				try {
					if (!"target".equals(currentFile.getName())) { //$NON-NLS-1$
						Files.delete(currentFile.toPath());
					}
				} catch (IOException e) {
					logger.debug(e.getMessage(), e);
					logger.error(e.getMessage());
				}
			}
		}
	}
}
