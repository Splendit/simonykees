package at.splendit.simonykees.core.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import at.splendit.simonykees.core.Activator;

public class CustomRefactoringJob extends AbstractRefactoringJob {
	
	public CustomRefactoringJob() {
		super(CustomRefactoringJob.class.getSimpleName());
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Activator.registerJob(this);
		// TODO Auto-generated method stub
		Activator.unregisterJob(this);
		return null;
	}

}
