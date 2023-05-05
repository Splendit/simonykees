package eu.jsparrow.ui.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;
import eu.jsparrow.ui.wizard.semiautomatic.RemoveUnusedCodeWizard;

/**
 * Collects the selected Java sources and starts the configuration wizard for
 * removing unused code.
 * 
 * @since 4.8.0
 *
 */
public class RemoveUnusedCodeWizardHandler extends AbstractRuleWizardHandler {

	@Override
	protected Optional<Job> createJob(Map<IJavaProject, List<IJavaElement>> selectedJavaElements) {
		if (selectedJavaElements.size() != 1) {
			String title = Messages.RemoveUnusedCodeWizardHandler_multipleProjectsSelected;
			String message = Messages.RemoveUnusedCodeWizardHandler_removeUnusedCodeOneProjectOnly;
			Shell shell = Display.getDefault()
				.getActiveShell();
			synchronizeWithUIShowSelectionErrorMessage(shell, title, message);
			return Optional.empty();
		}

		Job job = new Job("Find dead code") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Function<List<ICompilationUnit>, AbstractRuleWizard> wizardGenerator = RemoveUnusedCodeWizard::new;
				return startRuleWizard(selectedJavaElements, monitor, wizardGenerator);
			}
		};

		return Optional.of(job);
	}
}
