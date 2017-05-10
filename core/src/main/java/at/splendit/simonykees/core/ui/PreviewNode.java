package at.splendit.simonykees.core.ui;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * A representation of a {@link RefactoringRule} with all the
 * {@link DocumentChange}s of the selected {@link ICompilationUnit}s. <br>
 * 
 * 
 * @author Ludwig Werzowa
 * @since 1.2
 */
public class PreviewNode {

	private RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule;
	// TODO do we even need the compilation unit?
	private Map<ICompilationUnit, DocumentChange> changes;

	public PreviewNode(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule,
			Map<ICompilationUnit, DocumentChange> changes) {
		super();
		this.rule = rule;
		this.changes = changes;
	}

	public RefactoringRule<? extends AbstractASTRewriteASTVisitor> getRule() {
		return rule;
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

}
