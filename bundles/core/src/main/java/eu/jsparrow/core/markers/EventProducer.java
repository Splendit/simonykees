package eu.jsparrow.core.markers;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import eu.jsparrow.core.visitor.functionalinterface.FunctionalInterfaceASTVisitor;
import eu.jsparrow.rules.common.MarkerEvent;
import eu.jsparrow.rules.common.RefactoringEventProducer;
import eu.jsparrow.rules.common.util.RefactoringUtil;

public class EventProducer implements RefactoringEventProducer {

	@Override
	public List<MarkerEvent> generateEvents(ICompilationUnit iCompilationUnit) {
		
		CompilationUnit cu = RefactoringUtil.parse(iCompilationUnit);
		final ASTRewrite astRewrite = ASTRewrite.create(cu.getAST());
		FunctionalInterfaceASTVisitor visitor = new FunctionalInterfaceASTVisitor();
		visitor.setASTRewrite(astRewrite);
		cu.accept(visitor);
		return visitor.getMarkerEvents();
	}

}
