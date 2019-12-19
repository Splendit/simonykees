package eu.jsparrow.core.visitor.make_final;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * This is an abstract base class for all helper visitors for
 * {@link MakeFieldsAndVariablesFinalASTVisitor}. It provides methods for
 * getting {@link VariableDeclarationFragment}s from {@link IBinding}s.
 *
 * @since 3.12.0
 */
public abstract class AbstractMakeFinalHelperVisitor extends ASTVisitor {

	protected VariableDeclarationFragment extractFieldDeclarationFragmentFromExpression(Expression expression) {
		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(expression, CompilationUnit.class);

		VariableDeclarationFragment variableDeclarationFragment;
		switch (expression.getNodeType()) {
		case ASTNode.FIELD_ACCESS:
			variableDeclarationFragment = getVariableDeclarationFragment(compilationUnit, (FieldAccess) expression);
			break;
		case ASTNode.QUALIFIED_NAME:
		case ASTNode.SIMPLE_NAME:
			variableDeclarationFragment = getVariableDeclarationFragment(compilationUnit, (Name) expression);
			break;
		default:
			variableDeclarationFragment = null;
		}

		return variableDeclarationFragment;
	}

	protected VariableDeclarationFragment getVariableDeclarationFragment(CompilationUnit compilationUnit,
			FieldAccess fieldAccess) {
		IVariableBinding binding = fieldAccess.resolveFieldBinding();
		return getVariableDeclarationFragmentFromBinding(compilationUnit, binding);
	}

	protected VariableDeclarationFragment getVariableDeclarationFragment(CompilationUnit compilationUnit, Name name) {
		IBinding binding = name.resolveBinding();
		return getVariableDeclarationFragmentFromBinding(compilationUnit, binding);
	}

	protected VariableDeclarationFragment getVariableDeclarationFragmentFromBinding(CompilationUnit compilationUnit,
			IBinding binding) {
		if (binding == null) {
			return null;
		}

		ASTNode bindingNode = compilationUnit.findDeclaringNode(binding);
		if (bindingNode instanceof VariableDeclarationFragment) {
			return (VariableDeclarationFragment) bindingNode;
		}

		return null;
	}
}
