package eu.jsparrow.core.visitor.impl.inline;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.markers.common.InlineLocalVariablesEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * A visitor that searches for nested {@link IfStatement} and collapses them to
 * a single one if possible. Introduces a boolean variable to store the
 * condition if it contains more than 2 components.
 * 
 * @since 3.2.0
 *
 */
public class InlineLocalVariablesASTVisitor extends AbstractASTRewriteASTVisitor implements InlineLocalVariablesEvent {

	@Override
	public boolean visit(VariableDeclarationFragment declarationFragment) {

		Expression initializer = declarationFragment.getInitializer();
		if (initializer == null) {
			return false;
		}

		if (declarationFragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return true;
		}

		VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) declarationFragment
			.getParent();

		if (declarationStatement.fragments()
			.size() != 1) {
			return true;
		}

		if (!checkBindingsForFragmentAndInitializer(declarationFragment, initializer)) {
			return true;
		}

		SimpleName fragmentName = declarationFragment.getName();
		LocalVariableUsagesVisitor usageVisitor = new LocalVariableUsagesVisitor(fragmentName);
		declarationStatement.getParent()
			.accept(usageVisitor);

		List<SimpleName> usages = usageVisitor.getUsages();
		usages.remove(fragmentName);
		if (usages.size() != 1) {
			return true;
		}
		SimpleName usageToReplace = usages.get(0);

		if (usageToReplace.getLocationInParent() == ReturnStatement.EXPRESSION_PROPERTY ||
				usageToReplace.getLocationInParent() == ThrowStatement.EXPRESSION_PROPERTY) {

			Statement statement = (Statement) usageToReplace.getParent();
			if (isStatementFollowingVariableDeclaration(declarationStatement, statement)) {

				Supplier<ASTNode> replacementSuplier;
				if (initializer.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
					ArrayInitializer arrayInitializer = (ArrayInitializer) initializer;
					Type declarationStatementType = declarationStatement.getType();
					int dimensions = calculateDimensions(declarationStatementType,
							declarationFragment.getExtraDimensions());
					if (dimensions < 1) {
						return true;
					}
					Type elementType = findElementType(declarationStatementType);

					replacementSuplier = () -> createArrayCreationAsReplacement(elementType, dimensions,
							arrayInitializer);

				} else {
					replacementSuplier = () -> astRewrite.createMoveTarget(initializer);
				}

				ASTNode variableReplacement = replacementSuplier.get();

				astRewrite.replace(usageToReplace, variableReplacement, null);
				astRewrite.remove(declarationStatement, null);

				addMarkerEvent(declarationFragment);
				onRewrite();

			}

		}
		return true;
	}

	private boolean checkBindingsForFragmentAndInitializer(VariableDeclarationFragment declarationFragment,
			Expression initializer) {
		IVariableBinding variableBinding = declarationFragment.resolveBinding();
		if (variableBinding == null) {
			return false;
		}

		ITypeBinding declarationFragmentTypeBinding = variableBinding.getType();
		if (declarationFragmentTypeBinding == null) {
			return false;
		}

		ITypeBinding initializerTypeBinding = initializer.resolveTypeBinding();
		if (initializerTypeBinding == null) {
			return false;
		}

		if (initializerTypeBinding.isPrimitive() || declarationFragmentTypeBinding.isPrimitive()) {
			return ClassRelationUtil.compareITypeBinding(declarationFragmentTypeBinding, initializerTypeBinding);
		}

		return true;
	}

	private boolean isStatementFollowingVariableDeclaration(VariableDeclarationStatement variableDeclarationStatement,
			Statement statementToFollow) {

		if (statementToFollow.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return false;
		}

		Block block = (Block) statementToFollow.getParent();

		return ASTNodeUtil
			.findListElementBefore(block.statements(), statementToFollow, VariableDeclarationStatement.class)
			.orElse(null) == variableDeclarationStatement;
	}

	private int calculateDimensions(Type type, int extraDimensions) {
		if (type.isArrayType()) {
			ArrayType arrayType = (ArrayType) type;
			return arrayType.getDimensions() + extraDimensions;
		}
		return extraDimensions;
	}

	private Type findElementType(Type type) {
		if (type.isArrayType()) {
			ArrayType arrayType = (ArrayType) type;
			return arrayType.getElementType();
		}
		return type;
	}

	private ArrayCreation createArrayCreationAsReplacement(Type elementType, int dimensions,
			ArrayInitializer arrayInitializer) {

		AST ast = astRewrite.getAST();

		Type newElementType = (Type) astRewrite.createCopyTarget(elementType);
		ArrayType newArrayType = ast.newArrayType(newElementType, dimensions);

		ArrayInitializer newArrayInitializer = (ArrayInitializer) astRewrite.createMoveTarget(arrayInitializer);
		ArrayCreation newArrayCreation = ast.newArrayCreation();
		newArrayCreation.setType(newArrayType);
		newArrayCreation.setInitializer(newArrayInitializer);
		return newArrayCreation;
	}

}