package eu.jsparrow.rules.common.markers;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;

public interface RefactoringEventManager {

	List<MarkerEvent> generateEvents(ICompilationUnit iCompilationUnit);

	void resolve(ICompilationUnit iCompilationUnit, int offset);

}
