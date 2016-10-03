package at.splendit.simonykees.core.job;

import org.eclipse.core.runtime.jobs.Job;

public abstract class AbstractRefactoringJob extends Job {

	public AbstractRefactoringJob() {
		this(AbstractRefactoringJob.class.getSimpleName());
	}

	public AbstractRefactoringJob(String name) {
		super(name);
	}

}
