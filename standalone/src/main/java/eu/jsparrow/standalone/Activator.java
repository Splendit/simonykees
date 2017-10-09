package eu.jsparrow.standalone;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Andreja Sambolec
 * @since 2.1.1
 */
// @SuppressWarnings("restriction")
public class Activator implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "eu.jsparrow.standalone"; //$NON-NLS-1$

	public static final String USER_DIR = "user.dir"; //$NON-NLS-1$
	public static final String JAVA_TMP = "java.io.tmpdir"; //$NON-NLS-1$
	public static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	public static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME"; //$NON-NLS-1$
	public static final String JSPARROW_TEMP_FOLDER = "temp_jSparrow"; //$NON-NLS-1$
	public static final String DEPENDENCIES_FOLDER_CONSTANT = "deps"; //$NON-NLS-1$
	public static final String MAVEN_NATURE_CONSTANT = "org.eclipse.m2e.core.maven2Nature"; //$NON-NLS-1$
	public static final String PROJECT_DESCRIPTION_CONSTANT = ".project"; //$NON-NLS-1$

	private StandaloneConfig standaloneConfig;

	@Override
	public void start(BundleContext context) throws Exception {
		logger.info(Messages.Activator_start);

		// PREPARE RULES
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules();

		// CREATE REFACTORING PIPELINE AND SET RULES
		RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
		refactoringPipeline.setRules(rules);

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

		logger.info(Messages.Activator_debug_collectCompilationUnits);
		List<ICompilationUnit> compUnits = standaloneConfig.getCompUnits();

		logger.debug(Messages.Activator_debug_createRefactoringStates);

		logger.debug(Messages.Activator_debug_numCompilationUnits + compUnits.size());
		refactoringPipeline.createRefactoringStates(compUnits);
		logger.debug(Messages.Activator_debug_numRefactoringStates + refactoringPipeline.getRefactoringStates().size());

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

	@Override
	public void stop(BundleContext context) {
		try {
			/* Unregister as a save participant */
			if (ResourcesPlugin.getWorkspace() != null) {
				ResourcesPlugin.getWorkspace().forgetSavedTree(PLUGIN_ID);
				ResourcesPlugin.getWorkspace().removeSaveParticipant(PLUGIN_ID);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			standaloneConfig.cleanUp();
		}

		logger.info(Messages.Activator_stop);
	}
}
