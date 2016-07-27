package at.splendit.simonykees.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class RefactoringJob extends Job {

	public RefactoringJob() {
		super(RefactoringJob.class.getSimpleName());
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		return Status.OK_STATUS;
	}

}
