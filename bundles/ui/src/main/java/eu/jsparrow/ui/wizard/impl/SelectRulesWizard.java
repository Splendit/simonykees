package eu.jsparrow.ui.wizard.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.ui.dialog.JSparrowPricingLink;
import eu.jsparrow.ui.dialog.ObtainLicenseButtonData;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.ui.preview.RefactoringPreviewWizard;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.ResourceHelper;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;
import eu.jsparrow.ui.wizard.RuleWizardDialog;

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

	private static final String WINDOW_ICON = "icons/jsparrow-icon-16-003.png"; //$NON-NLS-1$

	private SelectRulesWizardPageModel model;
	private SelectRulesWizardPage page;

	private final Collection<IJavaProject> javaProjects;
	private final List<RefactoringRule> rules;

	private Image windowIcon;
	private final List<Runnable> afterLicenseUpdateListeners = new ArrayList<>();
	private final SelectRulesWizardData selectRulesWizardData;

	public static void synchronizeWithUIShowSelectRulesWizard(RefactoringPipeline refactoringPipeline,
			SelectRulesWizardData selectRulesWizardData) {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				SelectRulesWizard selectRulesWizard = new SelectRulesWizard(refactoringPipeline, selectRulesWizardData);

				class SelectRulesWizardDialog extends RuleWizardDialog {

					public SelectRulesWizardDialog(Shell parentShell, SelectRulesWizard newWizard) {
						super(parentShell, newWizard);
						newWizard.addLicenseUpdateListener(this::updateButtonsForButtonBar);
					}

					/**
					 * Creates new shell defined for this wizard. The dialog is
					 * made as big enough to show rule description vertically
					 * and horizontally to avoid two scrollers. Minimum size is
					 * set to avoid loosing components from view.
					 * 
					 * @param newShell
					 */
					@Override
					protected void configureShell(Shell newShell) {
						super.configureShell(newShell);
						newShell.setSize(1000, 1000);
						newShell.setMinimumSize(680, 600);
					}

					@Override
					protected void createButtonsForButtonBar(Composite parent) {
						createButton(parent, ObtainLicenseButtonData.BUTTON_ID_UNLOCK_PREMIUM_RULES,
								ObtainLicenseButtonData.BUTTON_TEXT_UNLOCK_PREMIUM_RULES, false);
						super.createButtonsForButtonBar(parent);

						Button finish = getButton(IDialogConstants.FINISH_ID);
						finish.setText(Messages.SelectRulesWizardHandler_finishButtonText);
						setButtonLayoutData(finish);
						updateButtonsForButtonBar();
					}

					private void updateButtonsForButtonBar() {
						boolean showEnterPremiumLicenseKey = false;
						LicenseUtil licenseUtil = LicenseUtil.get();
						if (licenseUtil.isFreeLicense()) {
							showEnterPremiumLicenseKey = true;
						}
						getButton(ObtainLicenseButtonData.BUTTON_ID_UNLOCK_PREMIUM_RULES)
							.setVisible(showEnterPremiumLicenseKey);
					}

					@Override
					protected void buttonPressed(int buttonId) {
						if (buttonId == ObtainLicenseButtonData.BUTTON_ID_UNLOCK_PREMIUM_RULES) {
							selectRulesWizard
								.showSimonykeesUpdateLicenseDialog(JSparrowPricingLink.UNLOCK_ALL_PREMIUM_RULES);
						} else {
							super.buttonPressed(buttonId);
						}
					}
				}

				SelectRulesWizardDialog dialog = new SelectRulesWizardDialog(shell, selectRulesWizard);
				/*
				 * Creates new shell and wizard.
				 */
				dialog.create();
				dialog.open();
			});

	}

	public static SelectRulesWizardData createSelectRulesWizardData(Set<IJavaProject> javaProjects) {
		List<RefactoringRule> rulesChoice = RulesContainer.getRulesForProjects(javaProjects, false);
		return new SelectRulesWizardData(rulesChoice, javaProjects);
	}

	public SelectRulesWizard(RefactoringPipeline refactoringPipeline, SelectRulesWizardData selectRulesWizardData) {
		super();
		this.javaProjects = selectRulesWizardData.getJavaProjects();
		this.refactoringPipeline = refactoringPipeline;
		this.rules = selectRulesWizardData.getRulesChoice();
		this.selectRulesWizardData = selectRulesWizardData;
		setNeedsProgressMonitor(true);
		windowIcon = ResourceHelper.createImage(WINDOW_ICON);
		Window.setDefaultImage(windowIcon);
	}

	public void addLicenseUpdateListener(Runnable afterLicenseUpdate) {
		afterLicenseUpdateListeners.add(afterLicenseUpdate);
	}

	@Override
	public String getWindowTitle() {
		return Messages.SelectRulesWizard_title;
	}

	@Override
	public void addPages() {
		model = new SelectRulesWizardPageModel(rules);
		page = new SelectRulesWizardPage(model,
				new SelectRulesWizardPageControler(model), selectRulesWizardData);
		afterLicenseUpdateListeners.forEach(page::addLicenseUpdateListener);
		addPage(page);
	}

	public void showSimonykeesUpdateLicenseDialog(JSparrowPricingLink explanation) {
		page.showSimonykeesUpdateLicenseDialog(explanation);
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
		showOptionalLockedRuleSelectionDialog(selectedRules);

		refactoringPipeline.setRules(selectedRules);
		refactoringPipeline.updateInitialSourceMap();

		String selectedProfileId = page.getSelectedProfileId()
			.orElse(null);
		if (selectedProfileId != null) {
			selectRulesWizardData.setSelectedProfileId(selectedProfileId);
		} else {
			selectRulesWizardData.setCustomRulesSelection(selectedRules);
		}

		Display.getCurrent()
			.asyncExec(() -> {

				Job job = createRefactoringJob(refactoringPipeline, javaProjects);

				job.addJobChangeListener(createPreviewWizardJobChangeAdapter(refactoringPipeline, javaProjects,
						selectRulesWizardData));

				job.setUser(true);
				job.schedule();
			});

		return true;
	}

	private void showOptionalLockedRuleSelectionDialog(List<RefactoringRule> selectedRules) {
		LicenseUtil licenseUtil = LicenseUtil.get();
		if (!licenseUtil.isFreeLicense() || selectedRules.isEmpty()) {
			return;
		}

		boolean allRulesFree = selectedRules
			.stream()
			.allMatch(RefactoringRule::isFree);

		if (!allRulesFree) {
			showSimonykeesUpdateLicenseDialog(JSparrowPricingLink.SELECTION_CONTAINS_LOCKED_RULES);
		}
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

	@Override
	public void dispose() {
		windowIcon.dispose();
		super.dispose();
	}
}
