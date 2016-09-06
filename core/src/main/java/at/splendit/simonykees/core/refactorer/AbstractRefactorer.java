package at.splendit.simonykees.core.refactorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.ltk.core.refactoring.DocumentChange;

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
						documentChanges.put(workingCopy.getPath(), SimonykeesUtil.applyRule(workingCopy, ruleClazz));
					} catch (ReflectiveOperationException e) {
						Activator.log(Status.ERROR, "Cannot init rule [" + ruleClazz.getName() + "]", e);
					}
					
					SimonykeesUtil.commitAndDiscardWorkingCopy(workingCopy);
				}
				
			}
			
		} catch (JavaModelException e) {
			Activator.log(Status.ERROR, e.getMessage(), null);
			// FIXME should also throw an exception
		}
	}
	
	public Multimap<IPath, DocumentChange> getDocumentChanges() {
		return documentChanges;
	}
	
}
