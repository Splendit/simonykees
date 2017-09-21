package eu.jsparrow.core.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import eu.jsparrow.core.Activator;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter, Hannes Schweighofer
 * @since 0.9
 */
public class FullRefactoringJob extends AbstractRefactoringJob {

	public FullRefactoringJob() {
		super(FullRefactoringJob.class.getSimpleName());
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Activator.registerJob(this);
		Activator.unregisterJob(this);
		return null;
	}

}
