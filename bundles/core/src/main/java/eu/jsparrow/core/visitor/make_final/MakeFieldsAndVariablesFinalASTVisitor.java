package eu.jsparrow.core.visitor.make_final;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.markers.common.MakeFieldsAndVariablesFinalEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This rule checks local variables and {@code private} fields for declaring
 * them {@code final}. The preconditions for final fields are documented in
 * {@link FinalInitializerCheckASTVisitor}. For local variables
 * {@link IVariableBinding#isEffectivelyFinal()} is used.
 * 
 * This rule is currently limited to local variables and {@code private} fields
 * only but it should be extended to also cover fields with other modifiers.
 *
 * @since 3.12.0
 */
public class MakeFieldsAndVariablesFinalASTVisitor extends AbstractASTRewriteASTVisitor implements MakeFieldsAndVariablesFinalEvent {

	private Set<FieldDeclaration> finalCandidateFields = new HashSet<>();

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		ASTNode enclosingNode = findEnclosingType(typeDeclaration);
		FinalInitializerCheckASTVisitor finalInitializerCheckVisitor = new FinalInitializerCheckASTVisitor();
		typeDeclaration.accept(finalInitializerCheckVisitor);

		PrivateFieldAssignmentASTVisitor privateFieldAssignmentVisitor = new PrivateFieldAssignmentASTVisitor(
				typeDeclaration);
		enclosingNode.accept(privateFieldAssignmentVisitor);

		List<VariableDeclarationFragment> assignedFragments = privateFieldAssignmentVisitor
			.getAssignedVariableDeclarationFragments();

		finalCandidateFields = finalInitializerCheckVisitor.getFinalCandidates()
			.stream()
			.filter((FieldDeclaration candidate) -> ASTNodeUtil.hasModifier(candidate.modifiers(), Modifier::isPrivate))
			.flatMap(fieldDeclaration -> ASTNodeUtil
				.convertToTypedList(fieldDeclaration.fragments(), VariableDeclarationFragment.class)
				.stream())
			.filter(variableDeclarationFragment -> !assignedFragments.contains(variableDeclarationFragment))
			.map(fragment -> ASTNodeUtil.getSpecificAncestor(fragment, FieldDeclaration.class))
			.collect(Collectors.toSet());

		return true;
	}

	private ASTNode findEnclosingType(TypeDeclaration typeDeclaration) {
		ASTNode enclosingNode;
		if (typeDeclaration.isMemberTypeDeclaration()) {
			enclosingNode = typeDeclaration.getParent();
			while (enclosingNode.getParent() != null && enclosingNode.getParent()
				.getNodeType() != ASTNode.COMPILATION_UNIT) {
				enclosingNode = enclosingNode.getParent();
			}
		} else if (typeDeclaration.isLocalTypeDeclaration()) {
			TypeDeclarationStatement parent = (TypeDeclarationStatement) typeDeclaration.getParent();
			enclosingNode = parent.getParent();
		} else {
			enclosingNode = typeDeclaration;
		}
		return enclosingNode;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		if (!finalCandidateFields.contains(fieldDeclaration)) {
			return false;
		}

		astRewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY)
			.insertLast(createFinalModifier(), null);
		onRewrite();
		addMarkerEvent(fieldDeclaration);

		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement variableDeclarationStatement) {
		List<VariableDeclarationFragment> variableDeclarationFragments = ASTNodeUtil
			.convertToTypedList(variableDeclarationStatement.fragments(), VariableDeclarationFragment.class);

		boolean isEffectivelyFinal = variableDeclarationFragments.stream()
			.map(VariableDeclarationFragment::resolveBinding)
			.allMatch(IVariableBinding::isEffectivelyFinal);

		if (!isEffectivelyFinal) {
			return true;
		}

		astRewrite.getListRewrite(variableDeclarationStatement, VariableDeclarationStatement.MODIFIERS2_PROPERTY)
			.insertLast(createFinalModifier(), null);
		onRewrite();
		addMarkerEvent(variableDeclarationStatement);

		return true;
	}

	private Modifier createFinalModifier() {
		AST ast = astRewrite.getAST();
		return ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
	}
}
