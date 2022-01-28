package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class UnusedFieldsCandidatesVisitor extends ASTVisitor {
	
	private CompilationUnit compilationUnit;
	private Map<String, Boolean> options;
	
	private List<UnusedFieldWrapper> unusedPrivateFields = new ArrayList<>();
	private List<SimpleName> internalReassignments = new ArrayList<>();
	private List<VariableDeclarationFragment> nonPrivateCandidates = new ArrayList<>();
	
	public UnusedFieldsCandidatesVisitor(Map<String, Boolean>options) {
		this.options = options;
	}
	
	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
	}
	
	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		/*
		 * TODO
		 * For private fields:
		 * - use ReferencesVisitor to find if they are unused. 
		 * - if yes, add them to unusedPrivateFields
		 * 
		 * For non-private fields:
		 * - use referecsVisitor to find if they are used within the compilation unit.
		 * 		-- if yes, discard them
		 * 		-- if not, add them to the nonPrivateCandidates.
		 */
		return true;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public List<UnusedFieldWrapper> getUnusedPrivateFields() {
		return unusedPrivateFields;
	}

	public List<VariableDeclarationFragment> getNonPrivateCandidates() {
		return nonPrivateCandidates;
	}

	public List<SimpleName> getInternalReassignments() {
		return this.internalReassignments;
	}
	
}
