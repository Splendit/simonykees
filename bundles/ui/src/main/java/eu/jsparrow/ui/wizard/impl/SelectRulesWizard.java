package eu.jsparrow.ui.wizard.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.ui.preview.RefactoringPreviewWizard;
import eu.jsparrow.ui.preview.RefactoringPreviewWizardPage;
import eu.jsparrow.ui.util.ResourceHelper;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;

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
public class SelectRulesWizard extends AbstractRuleWizard {

	private static final Logger logger = LoggerFactory.getLogger(SelectRulesWizard.class);

	private static final String WINDOW_ICON = "icons/jSparrow_active_icon_32.png"; //$NON-NLS-1$

	private SelectRulesWizardPageModel model;

	private final Collection<IJavaProject> javaProjects;
	private final List<RefactoringRule> rules;

	private RefactoringPipeline refactoringPipeline;
	private StandaloneStatisticsMetadata statisticsMetadata;

	public SelectRulesWizard(Collection<IJavaProject> javaProjects, RefactoringPipeline refactoringPipeline,
			List<RefactoringRule> rules) {
		super();
		this.javaProjects = javaProjects;
		this.refactoringPipeline = refactoringPipeline;
		this.rules = rules;
		setNeedsProgressMonitor(true);
		WizardDialog.setDefaultImage(ResourceHelper.createImage(WINDOW_ICON));
	}

	@Override
	public String getWindowTitle() {
		return Messages.SelectRulesWizard_title;
	}

	@Override
	public void addPages() {
		model = new SelectRulesWizardPageModel(rules);
		AbstractSelectRulesWizardPage page = new SelectRulesWizardPage(model,
				new SelectRulesWizardPageControler(model));
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		Activator.setRunning(false);
		refactoringPipeline.clearStates();
		return super.performCancel();
	}

	@Override
	public boolean canFinish() {
		return !model.getSelectionAsList()
			.isEmpty();
	}

	@Override
	public boolean performFinish() {
		String message = NLS.bind(Messages.SelectRulesWizard_start_refactoring, this.getClass()
			.getSimpleName(),
				this.javaProjects.stream()
					.map(IJavaProject::getElementName)
					.collect(Collectors.joining(";"))); //$NON-NLS-1$
		logger.info(message);

		final List<RefactoringRule> selectedRules = model.getSelectionAsList();

		refactoringPipeline.setRules(selectedRules);
		refactoringPipeline.updateInitialSourceMap();

		Rectangle rectangle = Display.getCurrent()
			.getPrimaryMonitor()
			.getBounds();

		Job job = new Job(Messages.ProgressMonitor_calculating_possible_refactorings) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				statisticsMetadata = prepareStatisticsMetadata();

				preRefactoring();
				IStatus refactoringStatus = doRefactoring(monitor, refactoringPipeline);
				postRefactoring();

				return refactoringStatus;
			}
		};

		job.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {

				if (event.getResult()
					.isOK()) {
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

	private StandaloneStatisticsMetadata prepareStatisticsMetadata() {

		String repoName = this.javaProjects.stream()
			.map(IJavaProject::getElementName)
			.collect(Collectors.joining(";")); //$NON-NLS-1$

		return new StandaloneStatisticsMetadata(Instant.now()
			.getEpochSecond(), "Splendit-Internal-Measurement", repoName); //$NON-NLS-1$
	}

	/**
	 * Method used to open RefactoringPreviewWizard from non UI thread
	 */
	private void synchronizeWithUIShowRefactoringPreviewWizard(RefactoringPipeline refactoringPipeline,
			Rectangle rectangle) {

		Display.getDefault()
			.asyncExec(() -> {

				logger.info(NLS.bind(Messages.SelectRulesWizard_end_refactoring, this.getClass()
					.getSimpleName(),
						javaProjects.stream()
							.map(IJavaProject::getElementName)
							.collect(Collectors.joining(";")))); //$NON-NLS-1$
				logger.info(NLS.bind(Messages.SelectRulesWizard_rules_with_changes, javaProjects.stream()
					.map(IJavaProject::getElementName)
					.collect(Collectors.joining(";")), refactoringPipeline.getRulesWithChangesAsString())); //$NON-NLS-1$

				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				RefactoringPreviewWizard previewWizard = new RefactoringPreviewWizard(refactoringPipeline,
						statisticsMetadata);
				final WizardDialog dialog = new WizardDialog(shell, previewWizard) {

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

					@Override
					protected void createButtonsForButtonBar(Composite parent) {
						createButton(parent, 9, Messages.SelectRulesWizard_Summary, false);
						super.createButtonsForButtonBar(parent);
					}

					@Override
					protected void buttonPressed(int buttonId) {
						if (buttonId == 9) {
							summaryButtonPressed();
						} else {
							super.buttonPressed(buttonId);
						}
					}

					@Override
					protected void cancelPressed() {
						previewWizard.performCancel();
						super.cancelPressed();
					}

					private void summaryButtonPressed() {
						if (getCurrentPage() instanceof RefactoringPreviewWizardPage) {
							previewWizard.updateViewsOnNavigation(getCurrentPage());
							((RefactoringPreviewWizardPage) getCurrentPage()).disposeControl();
						}
						showPage(previewWizard.getSummaryPage());
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
	 *            contains java elements which should be split up into
	 *            compilation units
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 * @since 0.9
	 */
	public static void collectICompilationUnits(List<ICompilationUnit> result, List<IJavaElement> javaElements,
			IProgressMonitor monitor) throws JavaModelException {

		/*
		 * Converts the monitor to a SubMonitor and sets name of task on
		 * progress monitor dialog. Size is set to number 100 and then scaled to
		 * size of the javaElements list. Each java element increases worked
		 * amount for same size.
		 */
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100)
			.setWorkRemaining(javaElements.size());
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
			 * If cancel is pressed on progress monitor, abort all and return,
			 * else continue
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
	 *            the current {@link IPackageFragment} from which the
	 *            sub-packages will be resolved
	 * @throws JavaModelException
	 */
	private static void resolveSubPackages(List<ICompilationUnit> result, IPackageFragment packageFragment)
			throws JavaModelException {
		String packageName = packageFragment.getElementName();
		IJavaElement parent = packageFragment.getParent();
		if (parent != null && parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT
				&& !StringUtils.isEmpty(packageName)) {
			IPackageFragmentRoot root = (IPackageFragmentRoot) parent;
			for (IJavaElement packageElement : root.getChildren()) {
				if (packageElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
					IPackageFragment pkg = (IPackageFragment) packageElement;
					if (!pkg.getElementName()
						.equals(packageName) && StringUtils.startsWith(pkg.getElementName(), packageName)) {
						addCompilationUnit(result, pkg.getCompilationUnits());
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param result
	 *            List of {@link ICompilationUnit} where the
	 *            {@code compilationUnit} is added
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
	 *            List of {@link ICompilationUnit} where the
	 *            {@code compilationUnit} is added
	 * @param compilationUnit
	 *            {@link ICompilationUnit} that is tested for consistency and
	 *            write access.
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
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
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 * @since 0.9
	 */
	private static void addCompilationUnit(List<ICompilationUnit> result, ICompilationUnit[] compilationUnits)
			throws JavaModelException {
		for (ICompilationUnit compilationUnit : compilationUnits) {
			addCompilationUnit(result, compilationUnit);
		}
	}
}
