package eu.jsparrow.core.markers;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEdit;

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

	@Override
	public void resolve(ICompilationUnit iCompilationUnit, int offset) {
		
		
		ICompilationUnit workingCopy;
		try {
			workingCopy = iCompilationUnit.getWorkingCopy(new NullProgressMonitor());
		} catch (JavaModelException e1) {
			e1.printStackTrace();
			return;
		}
		CompilationUnit cu = RefactoringUtil.parse(iCompilationUnit);
		final ASTRewrite astRewrite = ASTRewrite.create(cu.getAST());
		FunctionalInterfaceASTVisitor visitor = new FunctionalInterfaceASTVisitor();
		visitor.setASTRewrite(astRewrite);
		cu.accept(visitor);
		
		TextEdit edits;
		try {
			edits = astRewrite.rewriteAST();
			workingCopy.applyTextEdit(edits, new NullProgressMonitor());
		} catch (JavaModelException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		try {
			workingCopy.commitWorkingCopy(false, new NullProgressMonitor());
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

}
