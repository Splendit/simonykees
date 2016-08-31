package at.splendit.simonykees.core.refactorer;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.TextEdit;

import at.splendit.simonykees.core.Activator;

public abstract class AbstractRefactorer {
	
	protected List<IJavaElement> javaElements;
	protected List<Class<? extends ASTVisitor>> rules;
	
	public AbstractRefactorer(List<IJavaElement> javaElements, List<Class<? extends ASTVisitor>> rules) {
		this.javaElements = javaElements;
		this.rules = rules;
	}
	
	protected ICompilationUnit getWorkingCopy(ICompilationUnit compilationUnit) throws JavaModelException {
		return compilationUnit.getWorkingCopy(null);
	}
	
	protected ASTRewrite applyRuleToWorkingCopy(ICompilationUnit workingCopy, Class<? extends ASTVisitor> ruleClazz) throws ReflectiveOperationException {
		final ASTParser astParser = ASTParser.newParser(AST.JLS8);
		resetParser(workingCopy, astParser);
		final CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);
		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		
		Activator.log("Init rule [" + ruleClazz.getName() + "]");
		ASTVisitor rule = ruleClazz.getConstructor(ASTRewrite.class).newInstance(astRewrite);
		astRoot.accept(rule);
		
		return astRewrite;
	}
	
	protected TextEdit getTextEdit(ICompilationUnit workingCopy, ASTRewrite astRewrite) throws JavaModelException {
		String source = workingCopy.getSource();
		Document document = new Document(source);
		TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject().getOptions(true));
		
		return edits;
	}
	
	protected void applyToWorkingCopy(ICompilationUnit workingCopy, TextEdit edits) throws JavaModelException {
		workingCopy.applyTextEdit(edits, null);
		workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		workingCopy.commitWorkingCopy(false, null);
		workingCopy.discardWorkingCopy();
	}
	
	protected static void resetParser(ICompilationUnit compilationUnit, ASTParser astParser) {
		astParser.setSource(compilationUnit);
		astParser.setResolveBindings(true);
//		astParser.setCompilerOptions(null);
	}
	
	protected static DocumentChange generateDocumentChange(String name, Document document, TextEdit edits) {
		DocumentChange documentChange = new DocumentChange(name, document);
		documentChange.setEdit(edits);
		return documentChange;
	}

}
