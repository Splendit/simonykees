package at.splendit.simonykees.core.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public class CustomRefactoringJob extends AbstractRefactoringJob {
	
	public CustomRefactoringJob() {
		super(CustomRefactoringJob.class.getSimpleName());
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

}
