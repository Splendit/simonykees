package eu.jsparrow.rules.common;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;

public interface RefactoringEventProducer {
	
	List<MarkerEvent> generateEvents(ICompilationUnit iCompilationUnit);

}
