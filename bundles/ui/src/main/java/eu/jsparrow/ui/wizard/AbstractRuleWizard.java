package eu.jsparrow.ui.wizard;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.Wizard;

import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.StandaloneStatisticsMetadata;
import eu.jsparrow.core.statistic.StopWatchUtil;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * A parent for all rule wizards.
 * 
 * @author Ardit Ymeri
 * @since 2.3.1
 *
 */
public abstract class AbstractRuleWizard extends Wizard {

	protected void preRefactoring() {
		StopWatchUtil.start();
	}

	protected void postRefactoring() {
		StopWatchUtil.stop();
	}

	protected IStatus doRefactoring(IProgressMonitor monitor, RefactoringPipeline refactoringPipeline) {
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

	protected StandaloneStatisticsMetadata prepareStatisticsMetadata(Collection<IJavaProject> javaProjects) {

		String repoName = javaProjects.stream()
			.map(IJavaProject::getElementName)
			.collect(Collectors.joining(";")); //$NON-NLS-1$

		return new StandaloneStatisticsMetadata(Instant.now()
			.getEpochSecond(), "Splendit-Internal-Measurement", repoName); //$NON-NLS-1$
	}
}
