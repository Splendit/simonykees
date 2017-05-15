package at.splendit.simonykees.core.ui.preview;

import java.util.Collections;
import java.util.HashMap;
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
	private Map<ICompilationUnit, DocumentChange> changes;
	private Map<ICompilationUnit, Boolean> selections = new HashMap<>();

	public PreviewNode(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule,
			Map<ICompilationUnit, DocumentChange> changes) {
		super();
		this.rule = rule;
		this.changes = changes;
		this.changes.keySet().stream().forEach(key -> selections.put(key, true));
	}

	public RefactoringRule<? extends AbstractASTRewriteASTVisitor> getRule() {
		return rule;
	}
	
	/**
	 * Used to be in {@link RefactoringRule}. 
	 * 
	 * Changes should be generated with {@code generateDocumentChanges} first
	 * 
	 * @return Map containing {@code ICompilationUnit}s as key and corresponding
	 *         {@code DocumentChange}s as value
	 */
	public Map<ICompilationUnit, DocumentChange> getDocumentChanges() {
		return Collections.unmodifiableMap(changes);
	}

	public Map<ICompilationUnit, Boolean> getSelections() {
		return selections;
	}
	
}
