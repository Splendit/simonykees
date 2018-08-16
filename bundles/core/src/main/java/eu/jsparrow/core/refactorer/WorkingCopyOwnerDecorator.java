package eu.jsparrow.core.refactorer;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.WorkingCopyOwner;

/**
 * The owner of the working copies on each {@link RefactoringState}.
 * 
 * Instances of {@link WorkingCopyOwner} are not garbage collected, see
 * SIM-1238. The reason why this consist a separate class is to minimize the
 * retained size of the objects that are not removed by garbage collector.
 *
 */
public class WorkingCopyOwnerDecorator extends WorkingCopyOwner {

	private ProblemRequestor requestor = new ProblemRequestor();

	@Override
	public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
		return requestor;
	}
}
