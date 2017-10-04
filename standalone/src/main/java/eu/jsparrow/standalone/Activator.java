package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.config.YAMLConfigUtil;
import eu.jsparrow.core.config.YAMLProfile;
import eu.jsparrow.core.exception.ReconcileException;
import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 2.1.1
 */
// @SuppressWarnings("restriction")
public class Activator implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "eu.jsparrow.standalone"; //$NON-NLS-1$

	public static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	public static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$

	public static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	public static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private static List<Job> jobs = Collections.synchronizedList(new ArrayList<>());

	// Flag is jSparrow is already running
	private static boolean running = false;

	private static BundleContext bundleContext;

	private File directory;
	private TestStandalone test;

	@SuppressWarnings("nls")
	@Override
	public void start(BundleContext context) throws Exception {
		logger.info("Start ACTIVATOR"); //$NON-NLS-1$

		// String configFilePath = context.getProperty(CONFIG_FILE_PATH);
		String configFilePath = "/home/matthias/workspace/runtime-EclipseApplication/weka/weka/jsparrow.yml";
		logger.info("loading configuration from: " + configFilePath);

		// String profile = context.getProperty(SELECTED_PROFILE);
		String profile = "";
		logger.info("selected profile: " + profile);

		YAMLConfig config = readConfig(configFilePath, profile);

		// String projectPath = context.getProperty(PROJECT_PATH_CONSTANT);
		String projectPath = "/home/matthias/workspace/runtime-EclipseApplication/weka/weka/";

		logger.info("PATH FROM CONTEXT: " + projectPath);

		// Set working directory
		String file = System.getProperty("java.io.tmpdir");
		directory = new File(file + File.separator + "temp_jSparrow").getAbsoluteFile();
		if (directory.exists() || directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
			logger.info("Set user.dir to " + directory.getAbsolutePath());
		}

		test = new TestStandalone(projectPath);

		logger.info("Getting compilation units");
		List<ICompilationUnit> compUnits = test.getCompUnits();

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> selectedRules = getSelectedRulesFromConfig(config,
				compUnits.get(0).getJavaProject());

		// CREATE REFACTORING PIPELINE AND SET RULES
		RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
		refactoringPipeline.setRules(selectedRules);
		logger.info("selected rules (" + selectedRules.size() + "): " + selectedRules.toString());

		logger.info("Creating refactoring states");
		refactoringPipeline.createRefactoringStates(compUnits);

		try {
			logger.info("Starting refactoring proccess");
			refactoringPipeline.doRefactoring(new NullProgressMonitor());

			logger.info("Has changes: " + refactoringPipeline.hasChanges());
		} catch (RefactoringException | RuleException e) {
			logger.error(e.getMessage(), e);
			return;
		}

		try {
			logger.info("Commiting refactoring changes to compilation units");
			refactoringPipeline.commitRefactoring();
		} catch (RefactoringException | ReconcileException e) {
			logger.error(e.getMessage(), e);
			return;
		}
	}

	@Override
	public void stop(BundleContext context) {

		running = false;

		try {
			/* Unregister as a save participant */
			if (ResourcesPlugin.getWorkspace() != null) {
				ResourcesPlugin.getWorkspace().forgetSavedTree(PLUGIN_ID);
				ResourcesPlugin.getWorkspace().removeSaveParticipant(PLUGIN_ID);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			logger.info("Clean directory " + directory.getAbsolutePath());
			try {
				deleteChildren(new File(directory.getAbsolutePath() + File.separator + ".metadata"));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			plugin = null;
			bundleContext = null;
		}

		logger.info(Messages.Activator_stop);
	}

	/**
	 * this method selects the rules to be applied. for all rules it will be checked
	 * if they are available in general and for the current project. if a rule does
	 * not meet the criteria, it will be filtered.
	 * 
	 * <ul>
	 * <li>the defaultProfile is checked and if it exists its rules will be
	 * used</li>
	 * <li>if no defaultProfile is set the rules in the rules-section of the
	 * configuration file will be used</li>
	 * <li>if the given defaultProfile is not specified or a selected rule does not
	 * exist a {@link YAMLConfigException} will be thrown</li>
	 * </ul>
	 * 
	 * @param config
	 *            configuration
	 * @param javaProject
	 *            the current {@link IJavaElement}
	 * @return a list of rules to be applied on the project
	 * @throws YAMLConfigException
	 */
	private List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getSelectedRulesFromConfig(YAMLConfig config,
			IJavaProject javaProject) throws YAMLConfigException {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> result = new LinkedList<>();

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> projectRules = RulesContainer
				.getRulesForProject(javaProject);

		String defaultProfile = config.getDefaultProfile();
		if (defaultProfile != null && !defaultProfile.isEmpty()) {
			if (checkProfileExistence(config, defaultProfile)) {
				Optional<YAMLProfile> configProfile = config.getProfiles().stream()
						.filter(profile -> profile.getName().equals(defaultProfile)).findFirst();

				if (configProfile.isPresent()) {
					List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> profileRules = getConfigRules(
							configProfile.get().getRules());

					result = projectRules.stream().filter(rule -> rule.isEnabled())
							.filter(rule -> profileRules.contains(rule)).collect(Collectors.toList());
				} else {
					throw new YAMLConfigException("the given defaultProfile " + defaultProfile + " does not exist!");
				}
			} else {
				throw new YAMLConfigException("the given defaultProfile " + defaultProfile + " does not exist!");
			}
		} else { // use all rules from config file
			List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> configSelectedRules = getConfigRules(
					config.getRules());

			result = projectRules.stream().filter(rule -> rule.isEnabled())
					.filter(rule -> configSelectedRules.contains(rule)).collect(Collectors.toList());
		}

		return result;
	}

	/**
	 * this method takes a list of rule IDs and produces a list of rules
	 * 
	 * @param configRules
	 *            rule IDs
	 * @return list of rules ({@link RefactoringRule})
	 * @throws YAMLConfigException
	 *             is thrown if a given rule ID does not exist
	 */
	private List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getConfigRules(List<String> configRules)
			throws YAMLConfigException {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules();
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> configSelectedRules = new LinkedList<>();
		List<String> nonExistentRules = new LinkedList<>();

		for (String configRule : configRules) {
			Optional<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> currentRule = rules.stream()
					.filter(rule -> rule.getId().equals(configRule)).findFirst();
			if (currentRule.isPresent()) {
				configSelectedRules.add(currentRule.get());
			} else {
				nonExistentRules.add(configRule);
			}
		}

		if (!nonExistentRules.isEmpty()) {
			throw new YAMLConfigException("the following rules do not exist: " + nonExistentRules.toString());
		}

		return configSelectedRules;
	}

	/**
	 * reads the configuration file and modifies it according to maven flags. if no
	 * configuration file is specified the default configuration will be used. if a
	 * profile is chosen via maven flags there is a check if the profile exists.
	 * 
	 * @param configFilePath
	 *            path to the configuration file
	 * @param profile
	 *            selected profile
	 * @return jsparrow configuration
	 * @throws YAMLConfigException
	 *             if an error occurs during loading of the file or if the profile
	 *             does not exist
	 */
	private YAMLConfig readConfig(String configFilePath, String profile) throws YAMLConfigException {
		YAMLConfig config = null;
		if (configFilePath != null && !configFilePath.isEmpty()) {
			File configFile = new File(configFilePath);
			if (configFile.exists() && !configFile.isDirectory()) {
				config = YAMLConfigUtil.loadConfiguration(configFile);
				logger.info("config file read successfully from " + configFilePath + " !");
				logger.debug(config.toString());
			}
		}

		if (config == null) {
			config = YAMLConfig.getDefaultConfig();
			logger.warn("using default configuration...");
		}

		if (profile != null && !profile.isEmpty()) {
			if (checkProfileExistence(config, profile)) {
				config.setDefaultProfile(profile);
			} else {
				throw new YAMLConfigException("profile " + profile + " does not exist!");
			}
		}

		return config;
	}

	/**
	 * checks if the given profile exists in the configuration
	 * 
	 * @param config
	 *            jsparrow configuration
	 * @param profile
	 *            selected profile
	 * @return true, if the profile exists, false otherwise
	 */
	private boolean checkProfileExistence(YAMLConfig config, String profile) {
		return config.getProfiles().stream().anyMatch(configProfile -> configProfile.getName().equals(profile));
	}

	private void deleteChildren(File parentDirectory) throws IOException {
		for (String file : Arrays.asList(parentDirectory.list())) {
			File currentFile = new File(parentDirectory.getAbsolutePath(), file);
			if (currentFile.isDirectory()) {
				deleteChildren(currentFile);
			}
			currentFile.delete();
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void registerJob(Job job) {
		synchronized (jobs) {
			jobs.add(job);
		}
	}

	public static void unregisterJob(Job job) {
		synchronized (jobs) {
			jobs.remove(job);
		}
	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean isRunning) {
		running = isRunning;
	}

	public static BundleContext getBundleContext() {
		return bundleContext;
	}
}
