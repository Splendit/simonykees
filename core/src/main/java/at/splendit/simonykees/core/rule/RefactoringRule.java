package at.splendit.simonykees.core.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import at.splendit.simonykees.core.util.SimonykeesUtil;

public abstract class RefactoringRule<T extends ASTVisitor> {
	
	protected String name = "Missing";
	
	protected String description = "Please set name and description";
	
	protected boolean enabled = true;
	
	private Class<T> visitor;
	
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
	 * Changes are applied to working copy but <b>not</b> committed
	 * 
	 * @param workingCopies
	 * @return
	 * @throws JavaModelException
	 * @throws ReflectiveOperationException
	 */
	public Map<ICompilationUnit, DocumentChange> generateDocumentChanges(List<ICompilationUnit> workingCopies) throws JavaModelException, ReflectiveOperationException {
		Map<ICompilationUnit, DocumentChange> changes = new HashMap<ICompilationUnit, DocumentChange>();
		for (ICompilationUnit wc : workingCopies) {
			changes.put(wc, SimonykeesUtil.applyRule(wc, visitor));
		}
		return changes;
	}
	
}
