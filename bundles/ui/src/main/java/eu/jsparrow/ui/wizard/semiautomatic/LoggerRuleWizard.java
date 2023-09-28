package eu.jsparrow.ui.wizard.semiautomatic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.util.ResourceHelper;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;
import eu.jsparrow.ui.wizard.RuleWizardDialog;

/**
 * Wizard for configuring logger rule when applying to selected resources
 * 
 * @author Andreja Sambolec
 * @since 1.2
 *
 */
public class LoggerRuleWizard extends AbstractRuleWizard {

	private static final Logger logger = LoggerFactory.getLogger(LoggerRuleWizard.class);

	private static final String WINDOW_ICON = "icons/jsparrow-icon-16-003.png"; //$NON-NLS-1$

	private LoggerRuleWizardPageModel model;

	private IJavaProject selectedJavaProjekt;
	private final StandardLoggerRule rule;
	private final LoggerRuleWizardData loggerRuleWizardData;

	/**
	 * Method used to open SelectRulesWizard from non UI thread
	 */
	public static void synchronizeWithUIShowLoggerRuleWizard(RefactoringPipeline refactoringPipeline,
			LoggerRuleWizardData loggerRuleWizardData) {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				// HandlerUtil.getActiveShell(event)
				final RuleWizardDialog dialog = new RuleWizardDialog(shell,
						new LoggerRuleWizard(refactoringPipeline, loggerRuleWizardData));

				dialog.open();
			});
	}

	public LoggerRuleWizard(RefactoringPipeline refactoringPipeline, LoggerRuleWizardData loggerRuleWizardData) {
		super();
		this.loggerRuleWizardData = loggerRuleWizardData;
		this.selectedJavaProjekt = loggerRuleWizardData.getSelectedJavaProject();
		this.refactoringPipeline = refactoringPipeline;
		this.rule = loggerRuleWizardData.getRule();
		setNeedsProgressMonitor(true);
		Window.setDefaultImage(ResourceHelper.createImage(WINDOW_ICON));
	}

	@Override
	public String getWindowTitle() {
		return Messages.LoggerRuleWizard_title;
	}

	@Override
	public void addPages() {
		model = new LoggerRuleWizardPageModel(rule);
		addPage(new LoggerRuleWizardPage(model));
	}

	@Override
	public boolean canFinish() {
		return model.getSelectionStatus().isEmpty();
	}

	@Override
	public boolean performFinish() {
			String bind = NLS.bind(Messages.SelectRulesWizard_start_refactoring, this.getClass()
			.getSimpleName(), selectedJavaProjekt.getElementName());
		logger.info(bind);

		rule.activateOptions(model.getCurrentSelectionMap());

		final List<RefactoringRule> selectedRules = Arrays.asList(rule);
		Collection<IJavaProject> javaProjects = Arrays.asList(selectedJavaProjekt);
		proceedToRefactoringPreviewWizard(javaProjects, selectedRules, null, loggerRuleWizardData);

		return true;
	}
}
