package eu.jsparrow.core.refactorer;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.WorkingCopyOwner;

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
}


