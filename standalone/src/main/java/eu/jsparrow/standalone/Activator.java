package eu.jsparrow.standalone;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleActivator;
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

	protected static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	protected static final String JAVA_TMP = "java.io.tmpdir"; //$NON-NLS-1$
	protected static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	protected static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME"; //$NON-NLS-1$
	protected static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$
	protected static final String DEPENDENCIES_FOLDER_CONSTANT = "deps"; //$NON-NLS-1$
	protected static final String MAVEN_NATURE_CONSTANT = "org.eclipse.m2e.core.maven2Nature"; //$NON-NLS-1$
	protected static final String PROJECT_DESCRIPTION_CONSTANT = ".project"; //$NON-NLS-1$
	protected static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	protected static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	protected static final String LIST_RULES = "LIST.RULES"; //$NON-NLS-1$
	protected static final String LIST_RULES_SHORT = "LIST.RULES.SHORT"; //$NON-NLS-1$
	protected static final String LIST_RULES_SELECTED_ID = "LIST.RULES.SELECTED.ID"; //$NON-NLS-1$

	private StandaloneConfig standaloneConfig;

	@Override
	public void start(BundleContext context) throws Exception {
		logger.info(Messages.Activator_start);

		boolean listRules = Boolean.parseBoolean(context.getProperty(LIST_RULES));
		boolean listRulesShort = Boolean.parseBoolean(context.getProperty(LIST_RULES_SHORT));
		String listRulesId = context.getProperty(LIST_RULES_SELECTED_ID);

		if (listRules) {
			if (listRulesId != null && !listRulesId.isEmpty()) {
				ListRulesUtil.listRules(listRulesId);
			} else {
				ListRulesUtil.listRules();
			}
		} else if (listRulesShort) {
			ListRulesUtil.listRulesShort();
		} else {
			startRefactoring(context);
		}
	}

	@Override
	public void stop(BundleContext context) {
		try {
			/* Unregister as a save participant */
			if (ResourcesPlugin.getWorkspace() != null) {
				ResourcesPlugin.getWorkspace()
					.forgetSavedTree(PLUGIN_ID);
				ResourcesPlugin.getWorkspace()
					.removeSaveParticipant(PLUGIN_ID);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			standaloneConfig.cleanUp();
		}

		logger.info(Messages.Activator_stop);
	}

	private void startRefactoring(BundleContext context) throws YAMLConfigException {
		String configFilePath = context.getProperty(CONFIG_FILE_PATH);

		String loggerInfo = NLS.bind(Messages.Activator_standalone_LoadingConfiguration, configFilePath);
		logger.info(loggerInfo);

		String profile = context.getProperty(SELECTED_PROFILE);
		loggerInfo = NLS.bind(Messages.Activator_standalone_SelectedProfile, profile);
		logger.info(loggerInfo);

		YAMLConfig config = YAMLConfigUtil.readConfig(configFilePath, profile);

		// get project path and name from context
		String projectPath = context.getProperty(PROJECT_PATH_CONSTANT);
		String projectName = context.getProperty(PROJECT_NAME_CONSTANT);

		// Set working directory to temp_jSparrow in java tmp folder
		String file = System.getProperty(JAVA_TMP);
		File directory = new File(file + File.separator + JSPARROW_TEMP_FOLDER).getAbsoluteFile();
		if (directory.exists() || directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
		}

		standaloneConfig = new StandaloneConfig(projectName, projectPath);

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> projectRules = RulesContainer
			.getRulesForProject(standaloneConfig.getJavaProject(), true);
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> selectedRules = YAMLConfigUtil
			.getSelectedRulesFromConfig(config, projectRules);
		if (selectedRules == null) {
			selectedRules = new LinkedList<>();
		}

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
		loggerInfo = NLS.bind(Messages.Activator_debug_numRefactoringStates, refactoringPipeline.getRefactoringStates()
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
	}

}
