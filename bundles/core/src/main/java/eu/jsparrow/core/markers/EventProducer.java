package eu.jsparrow.core.markers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.osgi.framework.Version;

import eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver;
import eu.jsparrow.core.markers.visitor.FunctionalInterfaceResolver;
import eu.jsparrow.core.refactorer.WorkingCopyOwnerDecorator;
import eu.jsparrow.core.visitor.functionalinterface.FunctionalInterfaceASTVisitor;
import eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor;
import eu.jsparrow.rules.common.MarkerEvent;
import eu.jsparrow.rules.common.RefactoringEventProducer;
import eu.jsparrow.rules.common.util.JdtCoreVersionBindingUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;

public class EventProducer implements RefactoringEventProducer {

	@Override
	public List<MarkerEvent> generateEvents(ICompilationUnit iCompilationUnit) {
		
		List<MarkerEvent> allEvents = new ArrayList<>();
		
		CompilationUnit cu = RefactoringUtil.parse(iCompilationUnit);
		final ASTRewrite astRewrite = ASTRewrite.create(cu.getAST());
		FunctionalInterfaceASTVisitor visitor = new FunctionalInterfaceASTVisitor();
		visitor.setASTRewrite(astRewrite);
		cu.accept(visitor);
		allEvents.addAll(visitor.getMarkerEvents());
		
		final ASTRewrite astRewrite2 = ASTRewrite.create(cu.getAST());
		UseComparatorMethodsASTVisitor comparatorVisitor = new UseComparatorMethodsASTVisitor();
		comparatorVisitor.setASTRewrite(astRewrite2);
		cu.accept(comparatorVisitor);
		allEvents.addAll(comparatorVisitor.getMarkerEvents());
		return allEvents;
	}

	@Override
	public void resolve(ICompilationUnit iCompilationUnit, int offset) {
		
		Version jdtVersion = JdtCoreVersionBindingUtil.findCurrentJDTCoreVersion();
		WorkingCopyOwnerDecorator workingCopyOwner = new WorkingCopyOwnerDecorator();
		ICompilationUnit workingCopy;
		try {
			workingCopy = iCompilationUnit.getWorkingCopy(workingCopyOwner, new NullProgressMonitor());
		} catch (JavaModelException e1) {
			e1.printStackTrace();
			return;
		}
		CompilationUnit cu = RefactoringUtil.parse(workingCopy);
		final ASTRewrite astRewrite = ASTRewrite.create(cu.getAST());
		FunctionalInterfaceASTVisitor visitor = new FunctionalInterfaceResolver(offset);
		visitor.setASTRewrite(astRewrite);
		cu.accept(visitor);
		
		TextEdit edits;
		
		try {
			Document document = new Document(workingCopy.getSource());
			edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject().getOptions(true));
			if(edits.hasChildren()) {
				workingCopy.applyTextEdit(edits, new NullProgressMonitor());
				cu = workingCopy.reconcile(JdtCoreVersionBindingUtil.findJLSLevel(jdtVersion), true, null, null);
			}
		} catch (JavaModelException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		

		UseComparatorMethodsASTVisitor useComparatorMethodsVisitor = new UseComparatorMethodsResolver(offset);
		final ASTRewrite astRewrite2 = ASTRewrite.create(cu.getAST());
		useComparatorMethodsVisitor.setASTRewrite(astRewrite2);
		cu.accept(useComparatorMethodsVisitor);
		
		try {
			Document document = new Document(workingCopy.getSource());
			edits = astRewrite2.rewriteAST(document, workingCopy.getJavaProject()
					.getOptions(true));
			if(edits.hasChildren()) {
				workingCopy.applyTextEdit(edits, new NullProgressMonitor());
				cu = workingCopy.reconcile(JdtCoreVersionBindingUtil.findJLSLevel(jdtVersion), true, null, null);
			}
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		
		try {
			workingCopy.commitWorkingCopy(false, new NullProgressMonitor());
			workingCopy.discardWorkingCopy();
			workingCopy.close();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

}
