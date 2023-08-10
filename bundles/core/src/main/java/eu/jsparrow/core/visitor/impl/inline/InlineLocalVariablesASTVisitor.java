package eu.jsparrow.core.visitor.impl.inline;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
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

	private final Set<VariableDeclarationFragment> transformedFragments = new HashSet<>();

	@Override
	public boolean visit(VariableDeclarationFragment declarationFragment) {

		Expression initializer = declarationFragment.getInitializer();
		if (initializer == null) {
			return false;
		}

		if (transformedFragments.contains(declarationFragment)) {
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

		SimpleName usageToReplace = findSingleUsageToInline(declarationStatement, declarationFragment).orElse(null);
		if (usageToReplace == null) {
			return true;
		}

		Supplier<ASTNode> usageReplacementSupplier = findUsageReplacementSupplier(declarationStatement,
				declarationFragment, initializer).orElse(null);
		if (usageReplacementSupplier == null) {
			return true;
		}

		if (usageToReplace.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment fragmentAsTransformedParent = (VariableDeclarationFragment) usageToReplace
				.getParent();
			transformedFragments.add(fragmentAsTransformedParent);
		}

		ASTNode usageReplacement = usageReplacementSupplier.get();
		astRewrite.replace(usageToReplace, usageReplacement, null);
		astRewrite.remove(declarationStatement, null);
		addMarkerEvent(declarationFragment);
		onRewrite();
		return false;
	}

	private Optional<SimpleName> findSingleUsageToInline(VariableDeclarationStatement declarationStatement,
			VariableDeclarationFragment declarationFragment) {
		SimpleName fragmentName = declarationFragment.getName();
		LocalVariableUsagesVisitor usageVisitor = new LocalVariableUsagesVisitor(fragmentName);
		declarationStatement.getParent()
			.accept(usageVisitor);

		List<SimpleName> usages = usageVisitor.getUsages();
		usages.remove(fragmentName);
		if (usages.size() != 1) {
			return Optional.empty();
		}
		return Optional.of(usages.get(0))
			.filter(usage -> isSupportedUsage(declarationStatement, usage));
	}

	private boolean isSupportedUsage(VariableDeclarationStatement declarationStatement,
			SimpleName usageToReplace) {

		Statement statementWithSupportedUsage = findStatementWithSupportedUsage(usageToReplace).orElse(null);

		if (statementWithSupportedUsage == null
				|| statementWithSupportedUsage.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return false;
		}

		Block block = (Block) statementWithSupportedUsage.getParent();

		VariableDeclarationStatement statementExpectedToBeDeclaration = ASTNodeUtil
			.findListElementBefore(block.statements(), statementWithSupportedUsage, VariableDeclarationStatement.class)
			.orElse(null);
		return statementExpectedToBeDeclaration == declarationStatement;

	}

	private Optional<Statement> findStatementWithSupportedUsage(SimpleName usageToReplace) {

		if (usageToReplace.getLocationInParent() == ReturnStatement.EXPRESSION_PROPERTY) {
			return Optional.of((ReturnStatement) usageToReplace.getParent());
		}

		if (usageToReplace.getLocationInParent() == ThrowStatement.EXPRESSION_PROPERTY) {
			return Optional.of((ThrowStatement) usageToReplace.getParent());
		}

		if (usageToReplace.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) usageToReplace.getParent();
			if (assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				return Optional.of((ExpressionStatement) assignment.getParent());
			}
			return Optional.empty();
		}

		if (usageToReplace.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) usageToReplace.getParent();
			if (declarationFragment.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				return Optional.of((VariableDeclarationStatement) declarationFragment.getParent());
			}
			return Optional.empty();
		}
		return Optional.empty();

	}

	private Optional<Supplier<ASTNode>> findUsageReplacementSupplier(VariableDeclarationStatement declarationStatement,
			VariableDeclarationFragment declarationFragment, Expression initializer) {

		if (initializer.getNodeType() == ASTNode.ARRAY_INITIALIZER) {

			ArrayInitializer arrayInitializer = (ArrayInitializer) initializer;
			Type declarationStatementType = declarationStatement.getType();
			int dimensions = calculateDimensions(declarationStatementType, declarationFragment);
			if (dimensions < 1) {
				return Optional.empty();
			}
			Type elementType = findElementType(declarationStatementType);
			return Optional.of(() -> createArrayCreationAsReplacement(elementType, dimensions, arrayInitializer));
		}
		return Optional.of(() -> astRewrite.createMoveTarget(initializer));
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

	private int calculateDimensions(Type type, VariableDeclarationFragment declarationFragment) {
		int extraDimensions = declarationFragment.getExtraDimensions();
		if (type.isArrayType()) {
			return ((ArrayType) type).getDimensions() + extraDimensions;
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