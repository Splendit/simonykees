package eu.jsparrow.standalone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
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
import eu.jsparrow.core.util.RefactoringUtil;
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
	public static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	public static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME"; //$NON-NLS-1$
	public static final String PROJECT_DEPENDENCIES = "PROJECT.DEPENDENCIES"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private static List<Job> jobs = Collections.synchronizedList(new ArrayList<>());

	// Flag is jSparrow is already running
	private static boolean running = false;

	private static BundleContext bundleContext;

	private File directory;
	private TestStandalone test;

	@Override
	public void start(BundleContext context) throws Exception {
		logger.info("Start ACTIVATOR"); //$NON-NLS-1$

		// PREPARE RULES
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules();

		// CREATE REFACTORING PIPELINE AND SET RULES
		RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
		refactoringPipeline.setRules(rules);

		String projectPath = context.getProperty(PROJECT_PATH_CONSTANT);
		logger.info("PATH FROM CONTEXT: " + projectPath);

		String projectName = context.getProperty(PROJECT_NAME_CONSTANT);
		logger.info("NAME FROM CONTEXT: " + projectName);

		String projectDependencies = context.getProperty(PROJECT_DEPENDENCIES);
		logger.info("DEPENDENCIES FROM CONTEXT: " + projectDependencies);
		
		// Set working directory
		String file = System.getProperty("java.io.tmpdir");
		directory = new File(file + File.separator + "temp_jSparrow").getAbsoluteFile();
		if (directory.exists() || directory.mkdirs()) {
			System.setProperty(USER_DIR, directory.getAbsolutePath());
			logger.info("Set user.dir to " + directory.getAbsolutePath());
		}

		test = new TestStandalone(projectName, projectPath, projectDependencies);

		logger.info("Getting compilation units");
		List<ICompilationUnit> compUnits = test.getCompUnits();

		logger.info("Creating refactoring states");
		/*
		 * create refactoring states only from compilation units without
		 * compilation error
		 */

		logger.info("Number compilation units " + compUnits.size());
		refactoringPipeline.createRefactoringStates(compUnits);
		logger.info("Number refactoring states " + refactoringPipeline.getRefactoringStates().size());

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
