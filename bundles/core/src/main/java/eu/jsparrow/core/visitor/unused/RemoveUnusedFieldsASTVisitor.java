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
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Deletes the fields represented by the list of {@link UnusedFieldWrapper}s.
 * Uses node positions for matching relevant nodes with the provided ones.
 * 
 * @since 4.8.0
 *
 */
public class RemoveUnusedFieldsASTVisitor extends AbstractASTRewriteASTVisitor {

	private List<UnusedFieldWrapper> unusedFields;
	private Map<VariableDeclarationFragment, UnusedFieldWrapper> relevantFragments;
	private Map<ExpressionStatement, UnusedFieldWrapper> relevantReassignments;

	public RemoveUnusedFieldsASTVisitor(List<UnusedFieldWrapper> unusedFields) {
		this.unusedFields = unusedFields;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		IPath currentPath = compilationUnit.getJavaElement()
			.getPath();
		List<UnusedFieldWrapper> relevantUnusedFields = new ArrayList<>();
		Map<VariableDeclarationFragment, UnusedFieldWrapper> fragments = new HashMap<>();
		Map<ExpressionStatement, UnusedFieldWrapper> reassignments = new HashMap<>();
		for (UnusedFieldWrapper unusedField : unusedFields) {
			IPath path = unusedField.getDeclarationPath();
			if (path.equals(currentPath)) {
				relevantUnusedFields.add(unusedField);
				VariableDeclarationFragment fragment = unusedField.getFragment();
				fragments.put(fragment, unusedField);
				Map<ExpressionStatement, UnusedFieldWrapper> internal = findAllInternalReassignment(unusedField);
				reassignments.putAll(internal);

			} else {
				Map<ExpressionStatement, UnusedFieldWrapper> reassignmentsOfExternalfields = findReassignmentsOfExternalField(
						unusedField, currentPath);
				reassignments.putAll(reassignmentsOfExternalfields);
			}

		}
		this.relevantFragments = fragments;
		this.relevantReassignments = reassignments;
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);
		for (VariableDeclarationFragment declaratingFragment : fragments) {
			isDesignatedForRemoval(declaratingFragment).ifPresent(unusedField -> {
				TextEditGroup editGroup = unusedField.getTextEditGroup((ICompilationUnit) getCompilationUnit()
					.getJavaElement());
				if (fragments.size() == 1) {
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
		if (assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
			ExpressionStatement statement = (ExpressionStatement) assignment.getParent();
			isDesignatedForRemoval(statement).ifPresent(unusedField -> {
				TextEditGroup editGroup = unusedField.getTextEditGroup((ICompilationUnit) this.getCompilationUnit()
					.getJavaElement());
				astRewrite.remove(assignment.getParent(), editGroup);
			});
		}

		return true;
	}
	
	@Override
	public boolean visit(PrefixExpression prefixExpression) {
		if (prefixExpression.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
			ExpressionStatement statement = (ExpressionStatement) prefixExpression.getParent();
			isDesignatedForRemoval(statement).ifPresent(unusedField -> {
				TextEditGroup editGroup = unusedField.getTextEditGroup((ICompilationUnit) this.getCompilationUnit()
					.getJavaElement());
				astRewrite.remove(prefixExpression.getParent(), editGroup);
			});
		}

		return true;
	}
	
	@Override
	public boolean visit(PostfixExpression postfixExpression) {
		if (postfixExpression.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
			ExpressionStatement statement = (ExpressionStatement) postfixExpression.getParent();
			isDesignatedForRemoval(statement).ifPresent(unusedField -> {
				TextEditGroup editGroup = unusedField.getTextEditGroup((ICompilationUnit) this.getCompilationUnit()
					.getJavaElement());
				astRewrite.remove(postfixExpression.getParent(), editGroup);
			});
		}

		return true;
	}

	private Map<ExpressionStatement, UnusedFieldWrapper> findReassignmentsOfExternalField(
			UnusedFieldWrapper unusedField, IPath currentPath) {
		Map<ExpressionStatement, UnusedFieldWrapper> reassignmentsOfExternalFields = new HashMap<>();
		for (UnusedExternalReferences reference : unusedField.getUnusedExternalReferences()) {
			ICompilationUnit targetICU = reference.getICompilationUnit();
			if (currentPath.equals(targetICU.getPath())) {
				for (ExpressionStatement reassignment : reference.getUnusedReassignments()) {
					reassignmentsOfExternalFields.put(reassignment, unusedField);
				}

			}
		}
		return reassignmentsOfExternalFields;
	}

	private Map<ExpressionStatement, UnusedFieldWrapper> findAllInternalReassignment(UnusedFieldWrapper unusedField) {
		Map<ExpressionStatement, UnusedFieldWrapper> internal = new HashMap<>();
		List<ExpressionStatement> internalReassignment = unusedField.getUnusedReassignments();
		for (ExpressionStatement reassignment : internalReassignment) {
			internal.put(reassignment, unusedField);
		}
		return internal;
	}

	private Optional<UnusedFieldWrapper> isDesignatedForRemoval(VariableDeclarationFragment fragment) {
		int startPosition = fragment.getStartPosition();
		int length = fragment.getLength();
		for (Map.Entry<VariableDeclarationFragment, UnusedFieldWrapper> entry : relevantFragments.entrySet()) {
			VariableDeclarationFragment candidate = entry.getKey();
			int candidateStart = candidate.getStartPosition();
			int candidateLength = candidate.getLength();
			if (candidateStart == startPosition && candidateLength == length) {
				return Optional.of(entry.getValue());
			}
		}
		return Optional.empty();
	}

	private Optional<UnusedFieldWrapper> isDesignatedForRemoval(ExpressionStatement statement) {
		int startPosition = statement.getStartPosition();
		int length = statement.getLength();
		for (Map.Entry<ExpressionStatement, UnusedFieldWrapper> entry : relevantReassignments.entrySet()) {
			ExpressionStatement candidate = entry.getKey();
			int candidateStart = candidate.getStartPosition();
			int candidateLength = candidate.getLength();
			if (candidateStart == startPosition && candidateLength == length) {
				return Optional.of(entry.getValue());
			}
		}
		return Optional.empty();
	}

}
