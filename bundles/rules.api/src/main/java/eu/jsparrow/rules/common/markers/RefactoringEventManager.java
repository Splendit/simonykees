package eu.jsparrow.rules.common.markers;

import org.eclipse.jdt.core.ICompilationUnit;

public interface RefactoringEventManager {

	void discoverRefactoringEvents(ICompilationUnit iCompilationUnit);

	void resolve(ICompilationUnit iCompilationUnit, String resolver, int offset);

}
