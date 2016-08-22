package at.splendit.simonykees.core.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.visitor.DescriptiveRewriteASTVisitor;


public class DescriptiveRewriteHandler extends AbstractSimonykeesHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IJavaElement> selectedJavaElements = getSelectedJavaElements(event);
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		
		try {
			getCompilationUnits(compilationUnits, selectedJavaElements);
			
			if (compilationUnits.isEmpty()) {
				Activator.log(Status.WARNING, "No compilation units found", null);
				return null;
			}
			
			for (ICompilationUnit compilationUnit : compilationUnits) {
				ICompilationUnit workingCopy;
				
				workingCopy = compilationUnit.getWorkingCopy(null);
				
				final ASTParser astParser = ASTParser.newParser(AST.JLS8);
				
				resetParser(workingCopy, astParser);
				
				CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);
				
				ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
				
				astRoot.accept(new DescriptiveRewriteASTVisitor(astRewrite));
				
				String source = workingCopy.getSource();
				Document document = new Document(source);
				TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject().getOptions(true));
				
				workingCopy.applyTextEdit(edits, null);
			    workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
			    workingCopy.commitWorkingCopy(false, null);
			    workingCopy.discardWorkingCopy();
			}
			
			
		} catch (JavaModelException e) {
			Activator.log(Status.ERROR, e.getMessage(), null);
		}
		
		return null;
	}

}
