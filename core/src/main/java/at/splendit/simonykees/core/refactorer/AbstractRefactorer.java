package at.splendit.simonykees.core.refactorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.TextEdit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.util.SimonykeesUtil;

public abstract class AbstractRefactorer {
	
	protected List<IJavaElement> javaElements;
	protected List<Class<? extends ASTVisitor>> rules;
	protected Multimap<IPath, DocumentChange> documentChanges = ArrayListMultimap.create();
	
	public AbstractRefactorer(List<IJavaElement> javaElements, List<Class<? extends ASTVisitor>> rules) {
		this.javaElements = javaElements;
		this.rules = rules;
	}
	
	public DocumentChange applyRule(ICompilationUnit workingCopy, Class<? extends ASTVisitor> ruleClazz) throws ReflectiveOperationException, JavaModelException {
		final ASTParser astParser = ASTParser.newParser(AST.JLS8);
		SimonykeesUtil.resetParser(workingCopy, astParser, workingCopy.getJavaProject().getOptions(true));
		final CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);
		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		
		Activator.log("Init rule [" + ruleClazz.getName() + "]");
		ASTVisitor rule = ruleClazz.getConstructor(ASTRewrite.class).newInstance(astRewrite);
		astRoot.accept(rule);
		
		String source = workingCopy.getSource();
		Document document = new Document(source);
		TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject().getOptions(true));
		
		workingCopy.applyTextEdit(edits, null);
		workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		
		return SimonykeesUtil.generateDocumentChange(ruleClazz.getSimpleName(), document, edits);
	}
	
	public void doRefactoring() {
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		
		try {
			SimonykeesUtil.collectICompilationUnits(compilationUnits, javaElements);
			
			if (compilationUnits.isEmpty()) {
				Activator.log(Status.WARNING, "No compilation units found", null);
				return;
			}
			
			for (ICompilationUnit compilationUnit : compilationUnits) {
				for (Class<? extends ASTVisitor> ruleClazz : rules) {
					ICompilationUnit workingCopy = compilationUnit.getWorkingCopy(null);
					
					try {
						documentChanges.put(workingCopy.getPath(), applyRule(workingCopy, ruleClazz));
					} catch (ReflectiveOperationException e) {
						Activator.log(Status.ERROR, "Cannot init rule [" + ruleClazz.getName() + "]", e);
					}
					
					commitRefactoring(workingCopy);
				}
				
			}
			
		} catch (JavaModelException e) {
			Activator.log(Status.ERROR, e.getMessage(), null);
			// FIXME should also throw an exception
		}
	}
	
	public void commitRefactoring(ICompilationUnit workingCopy) throws JavaModelException {
		workingCopy.commitWorkingCopy(false, null);
		workingCopy.discardWorkingCopy();
	}
	
	public Multimap<IPath, DocumentChange> getDocumentChanges() {
		return documentChanges;
	}
	
}
