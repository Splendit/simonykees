package eu.jsparrow.core.refactorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * An implementation of {@link IProblemRequestor} which is used by {@link WorkingCopyOwnerDecorator}.
 *
 */
public	class ProblemRequestor implements IProblemRequestor {

	private List<IProblem> problems = new ArrayList<>();

	@Override
	public void acceptProblem(IProblem problem) {
		if (problem.isError()) {
			problems.add(problem);
		}
	}

	@Override
	public void beginReporting() {
		problems.clear();
	}

	@Override
	public void endReporting() {
		// not used
	}

	@Override
	public boolean isActive() {
		return true;
	}
	
	public List<IProblem> getProblems() {
		return problems;
	}

	public void clearPloblems() {
		problems.clear();
		
	}
}