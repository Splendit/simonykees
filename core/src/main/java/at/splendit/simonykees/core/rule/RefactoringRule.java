package at.splendit.simonykees.core.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.util.SimonykeesUtil;

public abstract class RefactoringRule<T extends ASTVisitor> {
	
	protected String name = "Missing";
	
	protected String description = "Please set name and description";
	
	protected boolean enabled = true;
	
	private Class<T> visitor;
	
	private Map<ICompilationUnit, DocumentChange> changes = new HashMap<ICompilationUnit, DocumentChange>();
	
	public RefactoringRule(Class <T> visitor) {
		this.visitor = visitor;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Class<T> getVisitor() {
		return visitor;
	}
	
	/**
	 * Changes should be generated with {@code generateDocumentChanges} first  
	 * @return Map containing {@code ICompilationUnit}s as key and corresponding {@code DocumentChange}s as value
	 */
	public Map<ICompilationUnit, DocumentChange> getDocumentChanges() {
		return changes;
	}
	
	/**
	 * Changes are applied to working copy but <b>not</b> committed
	 * 
	 * @param workingCopies
	 * @throws JavaModelException
	 * @throws ReflectiveOperationException
	 */
	public void generateDocumentChanges(List<ICompilationUnit> workingCopies) throws JavaModelException, ReflectiveOperationException {
		for (ICompilationUnit wc : workingCopies) {
			applyRule(wc);
		}
	}
	
	private void applyRule(ICompilationUnit workingCopy) throws JavaModelException, ReflectiveOperationException {
		if (changes.containsKey(workingCopy)) {
			// already have changes
			Activator.log("working copy alread contains changes for this rule [" + this.name + "]");
		} else {
			DocumentChange documentChange = SimonykeesUtil.applyRule(workingCopy, visitor);
			if (documentChange != null) {
				changes.put(workingCopy, documentChange);
			} else {
				// no changes
			}
		}
	}
}
