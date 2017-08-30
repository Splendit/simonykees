package at.splendit.simonykees.standalone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Andreja Sambolec,
 *         Matthias Webhofer
 * @since 0.9
 */
// @SuppressWarnings("restriction")
public class Activator extends Plugin {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "jSparrow.standalone"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private static List<Job> jobs = Collections.synchronizedList(new ArrayList<>());

	private long loggingBundleID = 0;

	// Flag is jSparrow is already running
	private static boolean running = false;

	private static BundleContext bundleContext;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Hello World!!");

		// start jSparrow logging bundle
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getSymbolicName().equals("jSparrow.logging") //$NON-NLS-1$
					&& bundle.getState() != Bundle.ACTIVE) {
				bundle.start();
				break;
			}
		}

		// PREPARE RULES
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules();
		
		//CREATE REFACTORING PIPELINE AND SET RULES
		RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
		refactoringPipeline.setRules(rules);

		TestStandalone test = new TestStandalone();
		
		logger.debug("Getting compilation units");
		System.out.println("Getting compilation units");
		List<ICompilationUnit> compUnits = test.getCompUnits();

		logger.debug("Creating refactoring states");
		System.out.println("Creating refactoring states");
		refactoringPipeline.createRefactoringStates(compUnits);

		NullProgressMonitor monitor = new NullProgressMonitor();

		try {
			logger.debug("Starting refactoring proccess");
			System.out.println("Starting refactoring proccess");
			refactoringPipeline.doRefactoring(new NullProgressMonitor());
		} catch (RefactoringException e) {
			return;
		} catch (RuleException e) {
			return;

		}

		try {
			logger.debug("Commiting refactoring changes to compilation units");
			System.out.println("Commiting refactoring changes to compilation units");
			refactoringPipeline.commitRefactoring();
		} catch (RefactoringException e) {
			// TODO exception
			return;
		} catch (ReconcileException e) {
			// TODO exception
			return;
		}

	}

	@Override
	public void stop(BundleContext context) throws Exception {

		running = false;

		// FIXME (see SIM-331) figure out better logging configuration
		// logger.info(Messages.Activator_stop);

		plugin = null;
		bundleContext = null;

		// stop jSparrow.logging
		Bundle loggingBundle = context.getBundle(loggingBundleID);
		if (loggingBundle.getState() == Bundle.ACTIVE) {
			loggingBundle.stop();
		}

		System.out.println("Stop ACTIVATOR");
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

//	public static void main(String[] args) {
//		List<ICompilationUnit> compUnits = getUnit();
//
//		System.out.println(compUnits.get(0));
//	}
}
