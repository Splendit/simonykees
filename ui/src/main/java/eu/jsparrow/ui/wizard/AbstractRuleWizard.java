package eu.jsparrow.ui.wizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.Wizard;

import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.RuleException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.ui.util.StopWatchUtil;
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

}
