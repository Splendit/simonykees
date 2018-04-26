package eu.jsparrow.core.refactorer;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.WorkingCopyOwner;

/**
 * The owner of the working copies on each {@link RefactoringState}. 
 *
 */
public class WorkingCopyOwnerDecorator extends WorkingCopyOwner {
	
	public static final WorkingCopyOwnerDecorator OWNER = new WorkingCopyOwnerDecorator();
	
	private ProblemRequestor requestor = new ProblemRequestor();

	@Override
	public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
		return requestor;
	}
	
	public void clearRecordedProblems() {
		requestor.clearPloblems();
	}
	
	@Override
	public String toString() {
		return "jSparrow working copy owner"; //$NON-NLS-1$
	}
}


