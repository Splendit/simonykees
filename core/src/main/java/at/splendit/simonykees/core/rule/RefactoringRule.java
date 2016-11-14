package at.splendit.simonykees.core.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Wrapper Class for {@link AbstractASTRewriteASTVisitor} that holds UI name,
 * description, if its enabled and the document changes for
 * {@link ICompilationUnit} that are processed
 * 
 * @author Martin Huter, Hannes Schweithofer, Ludwig Werzowa
 *
 * @param <T>
 *            is the {@link AbstractASTRewriteASTVisitor} implementation that is
 *            applied by this rule
 */
public abstract class RefactoringRule<T extends AbstractASTRewriteASTVisitor> {

	protected String id;
	
	protected String name = Messages.RefactoringRule_default_name;

	protected String description = Messages.RefactoringRule_default_description;

	protected boolean enabled = true;
	
	protected boolean defaultRule = false;

	private Class<T> visitor;

	private Map<ICompilationUnit, DocumentChange> changes = new HashMap<ICompilationUnit, DocumentChange>();

	public RefactoringRule(Class<T> visitor) {
		this.visitor = visitor;
		this.id = this.getClass().getSimpleName(); // FIXME maybe add a better id
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

	public String getId() {
		return id;
	}

	public boolean isDefaultRule() {
		return defaultRule;
	}

	/**
	 * Changes should be generated with {@code generateDocumentChanges} first
	 * 
	 * @return Map containing {@code ICompilationUnit}s as key and corresponding
	 *         {@code DocumentChange}s as value
	 */
	public Map<ICompilationUnit, DocumentChange> getDocumentChanges() {
		return Collections.unmodifiableMap(changes);
	}

	/**
	 * Changes are applied to working copy but <b>not</b> committed
	 * 
	 * @param workingCopies
	 *            List of {@link ICompilationUnit} for which a
	 *            {@link DocumentChange} for each selected rule is created
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 * @throws ReflectiveOperationException
	 *             is thrown if the default constructor of {@link #visitor} is
	 *             not present and the reflective construction fails.
	 */
	public void generateDocumentChanges(List<ICompilationUnit> workingCopies)
			throws JavaModelException, ReflectiveOperationException {
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
