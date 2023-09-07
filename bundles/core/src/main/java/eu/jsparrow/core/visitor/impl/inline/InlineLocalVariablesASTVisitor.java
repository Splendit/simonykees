package eu.jsparrow.core.visitor.impl.inline;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.markers.common.InlineLocalVariablesEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * A visitor that searches for declarations of local variables which are used
 * exactly once and can be in-lined.
 * 
 * @since 4.19.0
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

		if (isCommentProhibitingTransformation(initializer)) {
			return true;
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

		if (hasAnnotations(declarationStatement)) {
			return true;
		}

		if (!checkBindingsForFragmentAndInitializer(declarationFragment, initializer)) {
			return true;
		}

		Statement statementAfterDeclaration = ASTNodeUtil
			.findSubsequentStatementInBlock(declarationStatement, Statement.class)
			.orElse(null);
		if (statementAfterDeclaration == null) {
			return true;
		}

		SupportedReferenceAnalyzer supportedReferenceAnalyzer = new SupportedReferenceAnalyzer(
				statementAfterDeclaration, initializer);

		UniqueLocalVariableReferenceVisitor uniqueLocalVariableReferenceVisitor = new UniqueLocalVariableReferenceVisitor(
				getCompilationUnit(), declarationFragment, supportedReferenceAnalyzer);

		declarationStatement.getParent()
			.accept(uniqueLocalVariableReferenceVisitor);
		SimpleName usageToReplace = uniqueLocalVariableReferenceVisitor.getUniqueLocalVariableReference()
			.orElse(null);

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
		saveComments(declarationStatement, initializer, usageToReplace);

		ASTNode usageReplacement = usageReplacementSupplier.get();
		astRewrite.replace(usageToReplace, usageReplacement, null);
		astRewrite.remove(declarationStatement, null);
		addMarkerEvent(declarationFragment);
		onRewrite();
		return false;
	}

	private boolean isCommentProhibitingTransformation(Expression initializer) {
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> initializerRelatedComments = commentRewriter.findTrailingComments(initializer);
		int lastIndex = initializerRelatedComments.size() - 1;
		if (lastIndex >= 0) {
			Comment lastInitializerRelatedComment = initializerRelatedComments.get(lastIndex);
			if (lastInitializerRelatedComment.isLineComment()) {
				return true;
			}
		}
		return false;
	}

	private boolean hasAnnotations(VariableDeclarationStatement declarationStatement) {
		return !ASTNodeUtil.convertToTypedList(declarationStatement.modifiers(), Annotation.class)
			.isEmpty();
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

	private void saveComments(VariableDeclarationStatement variableDeclarationStatement, Expression initializer,
			SimpleName usage) {
		Statement statementWithInlinedVariable = ASTNodeUtil.getSpecificAncestor(usage, Statement.class);
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> comments = commentRewriter.findRelatedComments(variableDeclarationStatement);
		List<Comment> initializerRelatedComments = commentRewriter.findRelatedComments(initializer);

		comments.removeAll(initializerRelatedComments);
		commentRewriter.saveBeforeStatement(statementWithInlinedVariable, comments);
		List<Comment> commentsRelatedToUsage = commentRewriter.findRelatedComments(usage);
		commentRewriter.saveBeforeStatement(statementWithInlinedVariable, commentsRelatedToUsage);
	}

}