package eu.jsparrow.core.refactorer;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

public class WorkingCopyDecorator {
	
	public WorkingCopyOwner createWorkingCopyOwner() {
		ProblemRequestor problemRequestor = new ProblemRequestor();
		return new Owner(problemRequestor);
	}

}

class Owner extends WorkingCopyOwner {
	
	private ProblemRequestor requestor;
	
	public Owner (ProblemRequestor requestor) {
		this.requestor = requestor;
	}

	@Override
	public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
		return requestor;
	}
	
	@Override
	public IBuffer createBuffer(ICompilationUnit workingCopy) {
		ICompilationUnit original = workingCopy.getPrimary();
		
		try {
			return original.getBuffer();
		} catch (JavaModelException e) {
			return super.createBuffer(workingCopy);
		}
	}
	
	public void clearRecordedProblems() {
		requestor.clearPloblems();
	}
	
//	@Override
//	public IBuffer createBuffer(ICompilationUnit workingCopy) {
//		
//		return BufferManager.createNullBuffer(workingCopy);
//		
//		// TODO Auto-generated method stub
////		return super.createBuffer(workingCopy);
//	}
}
