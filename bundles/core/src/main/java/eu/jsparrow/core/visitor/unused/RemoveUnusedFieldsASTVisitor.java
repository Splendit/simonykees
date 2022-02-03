package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedFieldsASTVisitor extends AbstractASTRewriteASTVisitor {
	
	private List<UnusedFieldWrapper> unusedFields;
	private Map<VariableDeclarationFragment, UnusedFieldWrapper> relevantFragments;
	private Map<ExpressionStatement, UnusedFieldWrapper> relevantReassignments;
	
	
	public RemoveUnusedFieldsASTVisitor(List<UnusedFieldWrapper> unusedFields) {
		this.unusedFields = unusedFields;
	}
	
	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		IPath currentPath = compilationUnit.getJavaElement().getPath();
		List<UnusedFieldWrapper> relevantUnusedFields = new ArrayList<>();
		Map<VariableDeclarationFragment, UnusedFieldWrapper> fragments = new HashMap<>();
		Map<ExpressionStatement, UnusedFieldWrapper> reassignments = new HashMap<>();
		for(UnusedFieldWrapper unusedField : unusedFields) {
			IPath path = unusedField.getDeclarationPath();
			if (path.equals(currentPath)) {
				relevantUnusedFields.add(unusedField);
				VariableDeclarationFragment fragment = unusedField.getFragment();
				fragments.put(fragment, unusedField);
				List<ExpressionStatement> internalReassignment = unusedField.getUnusedReassignments();
				for(ExpressionStatement reassignment : internalReassignment) {
					reassignments.put(reassignment, unusedField);
				}
				
			} else {
				for(UnusedExternalReferences reference : unusedField.getUnusedExternalReferences()) {
					ICompilationUnit targetICU = reference.getICompilationUnit();
					if(currentPath.equals(targetICU.getPath())) {
						for(ExpressionStatement reassignment : reference.getUnusedReassignments()) {
							reassignments.put(reassignment, unusedField);
						}
						
					}
				}
			}
			
		}
		this.relevantFragments = fragments;
		this.relevantReassignments = reassignments;
		return super.visit(compilationUnit);
	}
	
	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(), VariableDeclarationFragment.class);
		for(VariableDeclarationFragment declaratingFragment : fragments) {
			isDesignatedForRemoval(declaratingFragment).ifPresent(unusedField -> {
				TextEditGroup editGroup = unusedField.getTextEditGroup((ICompilationUnit)this.getCompilationUnit().getJavaElement());
				if(fragments.size() == 1) {
					astRewrite.remove(fieldDeclaration, editGroup);
					
				} else {
					astRewrite.remove(declaratingFragment, editGroup);
				}
				onRewrite();
			});
		}
			

		return true;
	}

	@Override
	public boolean visit(Assignment assignment) {
		if(assignment.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		
		ExpressionStatement statement = (ExpressionStatement)assignment.getParent();
		isDesignatedForRemoval(statement).ifPresent(unusedField -> {
			TextEditGroup editGroup = unusedField.getTextEditGroup((ICompilationUnit)this.getCompilationUnit().getJavaElement());
			astRewrite.remove(assignment.getParent(), editGroup);
		});

		return true;
	}
	
	private Optional<UnusedFieldWrapper> isDesignatedForRemoval(VariableDeclarationFragment fragment) {
		int startPosition = fragment.getStartPosition();
		int length = fragment.getLength();
		for(Map.Entry<VariableDeclarationFragment, UnusedFieldWrapper> entry : relevantFragments.entrySet()) {
			VariableDeclarationFragment candidate = entry.getKey();
			int candidateStart = candidate.getStartPosition();
			int candidateLength = candidate.getLength();
			if(candidateStart == startPosition && candidateLength == length) {
				return Optional.of(entry.getValue());
			}
		}
		return Optional.empty();
	}
	
	private Optional<UnusedFieldWrapper> isDesignatedForRemoval(ExpressionStatement statement) {
		int startPosition = statement.getStartPosition();
		int length = statement.getLength();
		for(Map.Entry<ExpressionStatement, UnusedFieldWrapper> entry : relevantReassignments.entrySet()) {
			ExpressionStatement candidate = entry.getKey();
			int candidateStart = candidate.getStartPosition();
			int candidateLength = candidate.getLength();
			if(candidateStart == startPosition && candidateLength == length) {
				return Optional.of(entry.getValue());
			}
		}
		return Optional.empty();
	}

}
