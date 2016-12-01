package at.splendit.simonykees.core.job;

import org.eclipse.core.runtime.jobs.Job;

/**
 * TODO SIM-103 class description
 * 
 * @author Hannes Schweighofer
 * @since 0.9
 */
public abstract class AbstractRefactoringJob extends Job {

	public AbstractRefactoringJob() {
		this(AbstractRefactoringJob.class.getSimpleName());
	}

	public AbstractRefactoringJob(String name) {
		super(name);
	}

}
