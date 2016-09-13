package at.splendit.simonykees.core.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.util.SimonykeesUtil;

public abstract class RefactoringRule<T extends ASTVisitor> {
	
	protected String name = Messages.RefactoringRule_default_name;
	
	protected String description = Messages.RefactoringRule_default_description;
	
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
		return Collections.unmodifiableMap(changes);
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
			Activator.log(NLS.bind(Messages.RefactoringRule_warning_workingcopy_already_present, this.name));
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
