package eu.jsparrow.rules.common;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.rules.common.util.RefactoringUtil;
import eu.jsparrow.rules.common.visitor.marker.AnonymousClassVisitor;

public class EventGenerator {
	
	public static List<MarkerEvent> generateAnonymousClassEvents(ICompilationUnit icu) {
		CompilationUnit cu = RefactoringUtil.parse(icu);
		AnonymousClassVisitor visitor = new AnonymousClassVisitor();
		cu.accept(visitor);
		return visitor.getEvents();
	}

}
