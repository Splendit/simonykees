package eu.jsparrow.core.visitor.impl.inline;

import java.util.List;
import java.util.Optional;
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
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * A visitor that searches for declarations of local variables which are used
 * exactly once and can be in-lined.
 * 
 * @since 4.19.0
 *
 */
public class InlineLocalVariablesASTVisitor extends AbstractASTRewriteASTVisitor implements InlineLocalVariablesEvent {

	@Override
	public boolean visit(ReturnStatement node) {

		InLineLocalVariablesAnalysisData transformationData = InLineLocalVariablesAnalysisData
			.findAnalysisData(node)
			.filter(this::validateTransformationData)
			.orElse(null);

		if (transformationData == null) {
			return true;
		}

		Supplier<ASTNode> usageReplacementSupplier = findUsageReplacementSupplier(
				transformationData.getLocalVariableDeclarationData()).orElse(null);
		if (usageReplacementSupplier == null) {
			return true;
		}

		transform(transformationData, usageReplacementSupplier);
		addMarkerEvent(node);

		return false;
	}

	@Override
	public boolean visit(ThrowStatement node) {

		InLineLocalVariablesAnalysisData transformationData = InLineLocalVariablesAnalysisData
			.findAnalysisData(node)
			.filter(this::validateTransformationData)
			.orElse(null);

		if (transformationData == null) {
			return true;
		}

		Supplier<ASTNode> usageReplacementSupplier = findUsageReplacementSupplier(
				transformationData.getLocalVariableDeclarationData()).orElse(null);
		if (usageReplacementSupplier == null) {
			return true;
		}

		transform(transformationData, usageReplacementSupplier);
		addMarkerEvent(node);

		return false;
	}

	private boolean validateTransformationData(InLineLocalVariablesAnalysisData transformationData) {

		Expression initializer = transformationData.getLocalVariableDeclarationData()
			.getInitializer();

		if (isCommentProhibitingTransformation(initializer)) {
			return false;
		}

		VariableDeclarationStatement declarationStatement = transformationData.getLocalVariableDeclarationData()
			.getVariableDeclarationStatement();

		if (hasAnnotations(declarationStatement)) {
			return false;
		}

		VariableDeclarationFragment declarationFragment = transformationData.getLocalVariableDeclarationData()
			.getVariableDeclarationFragment();

		if (!checkBindingsForFragmentAndInitializer(declarationFragment, initializer)) {
			return false;
		}

		SimpleName usageToReplace = transformationData.getSimpleNameToReplace();

		UniqueLocalVariableReferenceVisitor uniqueLocalVariableReferenceVisitor = new UniqueLocalVariableReferenceVisitor(
				getCompilationUnit(), declarationFragment, usageToReplace);

		transformationData.getBlock()
			.accept(uniqueLocalVariableReferenceVisitor);

		return !uniqueLocalVariableReferenceVisitor.hasUnsupportedReference();
	}

	private void transform(InLineLocalVariablesAnalysisData transformationData,
			Supplier<ASTNode> usageReplacementSupplier) {
		VariableDeclarationStatement declarationStatement = transformationData.getLocalVariableDeclarationData()
			.getVariableDeclarationStatement();

		SimpleName usageToReplace = transformationData.getSimpleNameToReplace();
		saveComments(transformationData);
		ASTNode usageReplacement = usageReplacementSupplier.get();
		astRewrite.replace(usageToReplace, usageReplacement, null);
		astRewrite.remove(declarationStatement, null);
		onRewrite();
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

	private Optional<Supplier<ASTNode>> findUsageReplacementSupplier(
			LocalVariableDeclarationData localVariableDeclarationData) {

		Expression initializer = localVariableDeclarationData
			.getInitializer();
		if (initializer.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
			VariableDeclarationStatement declarationStatement = localVariableDeclarationData
				.getVariableDeclarationStatement();
			VariableDeclarationFragment declarationFragment = localVariableDeclarationData
				.getVariableDeclarationFragment();

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

	private void saveComments(InLineLocalVariablesAnalysisData transformationData) {

		LocalVariableDeclarationData localVariableDeclarationData = transformationData
			.getLocalVariableDeclarationData();
		VariableDeclarationStatement variableDeclarationStatement = localVariableDeclarationData
			.getVariableDeclarationStatement();
		Expression initializer = localVariableDeclarationData
			.getInitializer();
		Statement statementWithInlinedVariable = transformationData.getStatementWithSimpleNameToReplace();
		SimpleName usage = transformationData.getSimpleNameToReplace();
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> comments = commentRewriter.findRelatedComments(variableDeclarationStatement);
		List<Comment> initializerRelatedComments = commentRewriter.findRelatedComments(initializer);

		comments.removeAll(initializerRelatedComments);
		commentRewriter.saveBeforeStatement(statementWithInlinedVariable, comments);
		List<Comment> commentsRelatedToUsage = commentRewriter.findRelatedComments(usage);
		commentRewriter.saveBeforeStatement(statementWithInlinedVariable, commentsRelatedToUsage);
	}

}