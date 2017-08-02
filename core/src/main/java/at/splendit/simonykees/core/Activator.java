package at.splendit.simonykees.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.license.api.LicenseValidationService;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Andreja Sambolec,
 *         Matthias Webhofer
 * @since 0.9
 */
public class Activator extends AbstractUIPlugin implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "jSparrow.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	// is used for configuring the test fragment
	private static BundleActivator testFragmentActivator;

	private static List<Job> jobs = Collections.synchronizedList(new ArrayList<>());

	private long loggingBundleID = 0;

	// Flag is jSparrow is already running
	private static boolean running = false;

	private static BundleContext bundleContext;
	private static IEclipseContext eclipseContext;

//	@Inject
//	private LicenseValidationService licenseValidationService;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	private MyThread myThread;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		System.out.println("Hello World!!");

		// PREPARE RULES
		RefactoringPipeline refactoringPipeline = new RefactoringPipeline();

		// TODO collect compilation units from defined path
		// List<IJavaElement> selectedJavaElements = new ArrayList<>();
		ICompilationUnit compUnits = getUnit();
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		compilationUnits.add((ICompilationUnit) compUnits);
		refactoringPipeline.createRefactoringStates(compilationUnits);
		refactoringPipeline.setRules(RulesContainer.getAllRules());

		// IJavaProject selectedJavaProjekt = compUnits.;
		//
		// try {
		NullProgressMonitor monitor = new NullProgressMonitor();
		// refactoringPipeline.prepareRefactoring(selectedJavaElements,
		// monitor);
		//
		// final List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>>
		// rules = RulesContainer
		// .getRulesForProject(selectedJavaProjekt);
		//
		// refactoringPipeline.setRules(rules);
		//
		// } catch (RefactoringException e) {
		// return;
		// }

		try {
			refactoringPipeline.doRefactoring(monitor);
		} catch (RefactoringException e) {
			return;
		} catch (RuleException e) {
			return;

		}

		try {
			refactoringPipeline.commitRefactoring();

		} catch (RefactoringException e) {
			// TODO exception
			return;
		} catch (ReconcileException e) {
			// TODO exception
			return;
		}

		System.out.println(compilationUnits.get(0).getSource());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {

		running = false;
//
//		// FIXME (see SIM-331) figure out better logging configuration
//		logger.info(Messages.Activator_stop);
//
//		plugin = null;
//		bundleContext = null;
//
//		synchronized (jobs) {
//			jobs.forEach(job -> job.cancel());
//			jobs.clear();
//		}
//
//		// stop test fragment pseudo-activator
//		if (testFragmentActivator != null) {
//			testFragmentActivator.stop(context);
//		}
//
//		// stop jSparrow.logging
//		Bundle loggingBundle = context.getBundle(loggingBundleID);
//		if (loggingBundle.getState() == Bundle.ACTIVE) {
//			loggingBundle.stop();
//		}
//
////		System.out.println("Stopping com.vogella.osgi.firstbundle");
////		myThread.stopThread();
////		myThread.join();
//
		System.out.println("Stop ACTIVATOR");
		super.stop(context);
	}

	public org.eclipse.jdt.core.ICompilationUnit getCompUnit() {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(
				"public class A { int i = 9;  \n int j; \n ArrayList<Integer> al = new ArrayList<Integer>();j=1000; }"
						.toCharArray());
		// parser.setSource("/*abc*/".toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		// ASTNode node = parser.createAST(null);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new ASTVisitor() {

			Set names = new HashSet();

			public boolean visit(VariableDeclarationFragment node) {
				SimpleName name = node.getName();
				this.names.add(name.getIdentifier());
				System.out.println("Declaration of '" + name + "' at line" + cu.getLineNumber(name.getStartPosition()));
				return false; // do not continue to avoid usage info
			}

			public boolean visit(SimpleName node) {
				if (this.names.contains(node.getIdentifier())) {
					System.out.println("Usage of '" + node + "' at line " + cu.getLineNumber(node.getStartPosition()));
				}
				return true;
			}

		});

		return (ICompilationUnit) cu.getJavaElement();
	}

	public ICompilationUnit getUnit() {
//		IWorkspace workspace = ResourcesPlugin.getWorkspace();
//		IWorkspaceRoot root = workspace.getRoot();
//		// Get all projects in the workspace
//		IProject[] projects = root.getProjects();
//		IProject firstProject = projects[0];
//		try {
//			firstProject.open(null);
//		} catch (CoreException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
		List<IPackageFragment> packages = new ArrayList<>();
		List<ICompilationUnit> units = new ArrayList<>();
		
		TestStandalone test = new TestStandalone();
		try {
			//TODO open the project first
//			packages = JavaCore.create(firstProject).getPackageFragments();
			packages = Arrays.asList(test.getTestproject().getPackageFragments());

			for (IPackageFragment mypackage : packages) {
				if (mypackage.containsJavaResources() && 0 != mypackage.getCompilationUnits().length) {
					mypackage.open(null);
					// IPackageFragment mypackage = packages[0]; // implement
					// your own
					// logic to select
					// package
					units = Arrays.asList(mypackage.getCompilationUnits());
				}
			}

		units.get(0).open(null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return units.get(0);

		// ASTParser parser = ASTParser.newParser(AST.JLS3);
		// parser.setKind(ASTParser.K_COMPILATION_UNIT);
		// parser.setSource(units);
		// parser.setResolveBindings(true);
		// CompilationUnit cUnit = parser.createAST(null);

	}

//	/**
//	 * starts the license validation service after it has been injected
//	 */
//	@PostConstruct
//	private void startValidation() {
//		licenseValidationService.startValidation();
//	}
//
//	/**
//	 * stops the license validation service before it gets uninjected
//	 */
//	@PreDestroy
//	private void stopValidation() {
//		licenseValidationService.stopValidation();
//	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
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

	public static IEclipseContext getEclipseContext() {
		return eclipseContext;
	}
}

class MyThread extends Thread {
	private volatile boolean active = true;

	public void run() {
		while (active) {
			System.out.println("Hello OSGi console");
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				System.out.println("Thread interrupted " + e.getMessage());
			}
		}
	}

	public void stopThread() {
		active = false;
	}

}
