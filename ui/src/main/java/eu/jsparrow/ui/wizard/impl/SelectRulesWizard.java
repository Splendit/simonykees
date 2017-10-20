package eu.jsparrow.ui.wizard.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.AbstractRefactoringRule;
import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.ui.preview.RefactoringPreviewWizard;

/**
 * {@link Wizard} holding the {@link AbstractSelectRulesWizardPage}, which
 * contains a list of all selectable rules.
 * 
 * Clicking the OK button either calls the {@link RefactoringPreviewWizard} (if
 * there are changes within the code for the selected rules), or a
 * {@link MessageDialog} informing the user that there are no changes.
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa, Martin Huter, Andreja Sambolec,
 *         Matthias Webhofer
 * @since 0.9
 */
public class SelectRulesWizard extends Wizard {

	private static final Logger logger = LoggerFactory.getLogger(SelectRulesWizard.class);

	private AbstractSelectRulesWizardPage page;
	private SelectRulesWizardPageControler controler;
	private SelectRulesWizardPageModel model;

	private final List<IJavaElement> javaElements;
	private final List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules;

	private RefactoringPipeline refactoringPipeline;

	public SelectRulesWizard(List<IJavaElement> javaElements, RefactoringPipeline refactoringPipeline,
			List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules) {
		super();
		this.javaElements = javaElements;
		this.refactoringPipeline = refactoringPipeline;
		this.rules = rules;
		setNeedsProgressMonitor(true);
	}

	@Override
	public String getWindowTitle() {
		return Messages.SelectRulesWizard_title;
	}

	@Override
	public void addPages() {
		model = new SelectRulesWizardPageModel(rules);
		controler = new SelectRulesWizardPageControler(model);
		page = new SelectRulesWizardPage(model, controler);
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		if (model.getSelectionAsList().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean performFinish() {

		logger.info(NLS.bind(Messages.SelectRulesWizard_start_refactoring, this.getClass().getSimpleName(),
				this.javaElements.get(0).getJavaProject().getElementName()));

		final List<AbstractRefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = model.getSelectionAsList();

		refactoringPipeline.setRules(rules);
		refactoringPipeline.setSourceMap(refactoringPipeline.getInitialSourceMap());

		Rectangle rectangle = Display.getCurrent().getPrimaryMonitor().getBounds();

		Job job = new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					refactoringPipeline.doRefactoring(monitor);
					if (monitor.isCanceled()) {
						refactoringPipeline.clearStates();
						return Status.CANCEL_STATUS;
					}
				} catch (RefactoringException e) {
					WizardMessageDialog.synchronizeWithUIShowInfo(e);
					return Status.CANCEL_STATUS;
				} catch (RuleException e) {
					WizardMessageDialog.synchronizeWithUIShowError(e);
					return Status.CANCEL_STATUS;

				} finally {
					monitor.done();
				}

				return Status.OK_STATUS;
			}
		};

		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult().isOK()) {
					if (refactoringPipeline.hasChanges()) {
						synchronizeWithUIShowRefactoringPreviewWizard(refactoringPipeline, rectangle);
					} else {
						WizardMessageDialog.synchronizeWithUIShowWarningNoRefactoringDialog();
					}
				} else {
					// do nothing if status is canceled, close
					Activator.setRunning(false);
				}
			}
		});

		job.setUser(true);
		job.schedule();

		return true;
	}

	/**
	 * Method used to open RefactoringPreviewWizard from non UI thread
	 */
	private void synchronizeWithUIShowRefactoringPreviewWizard(RefactoringPipeline refactoringPipeline,
			Rectangle rectangle) {

		Display.getDefault().asyncExec(() -> {

			logger.info(NLS.bind(Messages.SelectRulesWizard_end_refactoring, this.getClass().getSimpleName(),
					javaElements.get(0).getJavaProject().getElementName()));
			logger.info(NLS.bind(Messages.SelectRulesWizard_rules_with_changes,
					javaElements.get(0).getJavaProject().getElementName(),
					refactoringPipeline.getRules().stream()
							.filter(rule -> null != refactoringPipeline.getChangesForRule(rule)
									&& !refactoringPipeline.getChangesForRule(rule).isEmpty())
							.map(AbstractRefactoringRule::getName)
							.collect(Collectors.joining("; ")))); //$NON-NLS-1$

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			final WizardDialog dialog = new WizardDialog(shell, new RefactoringPreviewWizard(refactoringPipeline)) {

				@Override
				protected void nextPressed() {
					((RefactoringPreviewWizard) getWizard()).pressedNext();
					super.nextPressed();
				}

				@Override
				protected void backPressed() {
					((RefactoringPreviewWizard) getWizard()).pressedBack();
					super.backPressed();
				}

			};

			// maximizes the RefactoringPreviewWizard
			dialog.setPageSize(rectangle.width, rectangle.height);
			dialog.open();
		});
	}

	/**
	 * Populates the list {@code result} with {@code ICompilationUnit}s found in
	 * {@code javaElements}
	 * 
	 * @param result
	 *            will contain compilation units
	 * @param javaElements
	 *            contains java elements which should be split up into compilation
	 *            units
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs while
	 *             accessing its corresponding resource.
	 * @since 0.9
	 */
	public static void collectICompilationUnits(List<ICompilationUnit> result, List<IJavaElement> javaElements,
			IProgressMonitor monitor) throws JavaModelException {

		/*
		 * Converts the monitor to a SubMonitor and sets name of task on progress
		 * monitor dialog. Size is set to number 100 and then scaled to size of the
		 * javaElements list. Each java element increases worked amount for same size.
		 */
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100).setWorkRemaining(javaElements.size());
		subMonitor.setTaskName(Messages.ProgressMonitor_SimonykeesUtil_collectICompilationUnits_taskName);
		for (IJavaElement javaElement : javaElements) {
			subMonitor.subTask(javaElement.getElementName());
			if (javaElement instanceof ICompilationUnit) {
				ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
				addCompilationUnit(result, compilationUnit);
			} else if (javaElement instanceof IPackageFragment) {
				IPackageFragment packageFragment = (IPackageFragment) javaElement;
				if (SimonykeesPreferenceManager.getResolvePackagesRecursively()) {
					resolveSubPackages(result, packageFragment);
				}
				addCompilationUnit(result, packageFragment.getCompilationUnits());
			} else if (javaElement instanceof IPackageFragmentRoot) {
				IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) javaElement;
				List<IPackageFragment> packageFragments = ASTNodeUtil
						.convertToTypedList(Arrays.asList(packageFragmentRoot.getChildren()), IPackageFragment.class);
				addCompilationUnits(result, packageFragments);
			} else if (javaElement instanceof IJavaProject) {
				IJavaProject javaProject = (IJavaProject) javaElement;
				addCompilationUnits(result, Arrays.asList(javaProject.getPackageFragments()));
			}

			/*
			 * If cancel is pressed on progress monitor, abort all and return, else continue
			 */
			if (subMonitor.isCanceled()) {
				return;
			} else {
				subMonitor.worked(1);
			}
		}
	}

	/**
	 * 
	 * 
	 * @param result
	 *            List of {@link ICompilationUnit} where all the
	 *            {@link ICompilationUnit}s from the sub-packages are added
	 * @param packageFragment
	 *            the current {@link IPackageFragment} from which the sub-packages
	 *            will be resolved
	 * @throws JavaModelException
	 */
	private static void resolveSubPackages(List<ICompilationUnit> result, IPackageFragment packageFragment)
			throws JavaModelException {
		String packageName = packageFragment.getElementName();
		IJavaElement parent = packageFragment.getParent();
		if (parent != null && parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT && !packageName.isEmpty()) {
			IPackageFragmentRoot root = (IPackageFragmentRoot) parent;
			for (IJavaElement packageElement : root.getChildren()) {
				if (packageElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
					IPackageFragment pkg = (IPackageFragment) packageElement;
					if (!pkg.getElementName().equals(packageName) && pkg.getElementName().startsWith(packageName)) {
						addCompilationUnit(result, pkg.getCompilationUnits());
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param result
	 *            List of {@link ICompilationUnit} where the {@code compilationUnit}
	 *            is added
	 * @param packageFragments
	 * @throws JavaModelException
	 */
	private static void addCompilationUnits(List<ICompilationUnit> result, List<IPackageFragment> packageFragments)
			throws JavaModelException {
		for (IPackageFragment fragment : packageFragments) {
			addCompilationUnit(result, fragment.getCompilationUnits());
		}
	}

	/**
	 * 
	 * @param result
	 *            List of {@link ICompilationUnit} where the {@code compilationUnit}
	 *            is added
	 * @param compilationUnit
	 *            {@link ICompilationUnit} that is tested for consistency and write
	 *            access.
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs while
	 *             accessing its corresponding resource.
	 * @since 0.9
	 */

	private static void addCompilationUnit(List<ICompilationUnit> result, ICompilationUnit compilationUnit)
			throws JavaModelException {
		if (!compilationUnit.isConsistent()) {
			compilationUnit.makeConsistent(null);
		}
		if (!compilationUnit.isReadOnly()) {
			result.add(compilationUnit);
		}
	}

	/**
	 * 
	 * @param result
	 *            List of {@link ICompilationUnit} where the
	 *            {@code compilationUnits} are added
	 * @param compilationUnits
	 *            array of {@link ICompilationUnit} which are loaded
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs while
	 *             accessing its corresponding resource.
	 * @since 0.9
	 */
	private static void addCompilationUnit(List<ICompilationUnit> result, ICompilationUnit[] compilationUnits)
			throws JavaModelException {
		for (ICompilationUnit compilationUnit : compilationUnits) {
			addCompilationUnit(result, compilationUnit);
		}
	}
}
