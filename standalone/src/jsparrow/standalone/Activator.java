package jsparrow.standalone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.refactorer.RefactoringPipeline;

@SuppressWarnings("restriction")
public class Activator implements BundleActivator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		System.out.println("Hello World!!");

		// PREPARE RULES
		RefactoringPipeline refactoringPipeline = new RefactoringPipeline();

		// TODO collect compilation units from defined path
		// List<IJavaElement> selectedJavaElements = new ArrayList<>();
		org.eclipse.jdt.core.ICompilationUnit compUnits = getCompUnit();
		List<org.eclipse.jdt.core.ICompilationUnit> compilationUnits = new ArrayList<>();
		compilationUnits.add((org.eclipse.jdt.core.ICompilationUnit) compUnits);
		refactoringPipeline.createRefactoringStates(compilationUnits);
		
		// IJavaProject selectedJavaProjekt = compUnits.;
		//
		// try {
		// NullProgressMonitor monitor = new NullProgressMonitor();
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

		Job job = new Job("Apply rules") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IProgressMonitor nullMonitor = new NullProgressMonitor();
				try {
					refactoringPipeline.doRefactoring(monitor);
				} catch (RefactoringException e) {
					return Status.CANCEL_STATUS;
				} catch (RuleException e) {
					return Status.CANCEL_STATUS;

				}
				return Status.OK_STATUS;
			}
		};

		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult().isOK()) {
					try {
						refactoringPipeline.commitRefactoring();
					} catch (RefactoringException e) {
						// TODO exception
						return;
					} catch (ReconcileException e) {
						// TODO exception
						return;
					}
				}
			}
		});

		job.setUser(true);
		job.schedule();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		System.out.println("Goodbye World!!");
	}

	public IJavaProject createJavaProject() throws CoreException {
		IProgressMonitor progressMonitor = new NullProgressMonitor();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("MyProject");
		project.create(progressMonitor);
		project.open(progressMonitor);

		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = JavaCore.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, progressMonitor);

		IJavaProject javaProject = JavaCore.create(project);

		return javaProject;
	}

	public org.eclipse.jdt.core.ICompilationUnit getCompUnit() {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
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

		return (ICompilationUnit) cu;
	}
}
