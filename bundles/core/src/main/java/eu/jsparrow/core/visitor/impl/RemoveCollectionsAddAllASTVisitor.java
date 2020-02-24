package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * The visitor first searches the next invocation of the method
 * <p>
 * {@link java.util.Collection#addAll(java.util.Collection)}
 * <p>
 * on a local variable. If the previous statement is the statement declaring the
 * same local variable then the visitor looks for the initializer which must be
 * a constructor invocation without any argument. If such an initializer is
 * found then the visitor copies the argument of the add all invocation to the
 * argument list of the constructor invocation and finally removes the addAll -
 * invocation.
 * <p>
 * Example:
 * <p>
 * {@code List<String> list = new ArrayList<>();} <br>
 * {@code list.addAll(Arrays.asList(\"value1\", \"value2\"));}
 * <p>
 * is transformed to
 * <p>
 * {@code List<String> list = new ArrayList<>(Arrays.asList(\"value1\", \"value2\"));}
 * 
 *
 * 
 * @since 3.14.0
 */
public class RemoveCollectionsAddAllASTVisitor extends AbstractASTRewriteASTVisitor {

	private class AddAllAnalysisResult {

		boolean isValid;

		SimpleName addAllExpression;

		Expression addAllArgument;

		ExpressionStatement addAllStatement;

		// VariableDeclarationFragment variableDeclarationFragment;

		// String varName;

		// Expression initializer;

		ClassInstanceCreation instanceCreation;

	}

	private static final String JAVA_UTIL_COLLECTION = java.util.Collection.class.getName();

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		AddAllAnalysisResult analysisResult = analyzeAddAllInvocationStatement(methodInvocation);
		if (analysisResult.isValid) {
			applyRule(analysisResult.addAllArgument, analysisResult.addAllStatement, analysisResult.instanceCreation);
		}
		return true;
	}

	private AddAllAnalysisResult analyzeAddAllInvocationStatement(MethodInvocation methodInvocation) {

		AddAllAnalysisResult analysisResult = new AddAllAnalysisResult();

		Expression invocationExpression = methodInvocation.getExpression();
		if (invocationExpression == null) {
			return analysisResult;
		}

		if (invocationExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return analysisResult;
		}

		analysisResult.addAllExpression = (SimpleName) invocationExpression;

		if (!isCollectionVariable(analysisResult.addAllExpression.resolveTypeBinding())) {
			return analysisResult;
		}

		if (!"addAll".equals(methodInvocation.getName() //$NON-NLS-1$
			.getIdentifier())) {
			return analysisResult;
		}

		if (methodInvocation.arguments()
			.size() != 1) {
			return analysisResult;
		}

		analysisResult.addAllArgument = ASTNodeUtil
			.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);

		if (analysisResult.addAllArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName variableArgument = (SimpleName) analysisResult.addAllArgument;
			if (variableArgument.getIdentifier()
				.equals(analysisResult.addAllExpression.getIdentifier())) {
				return analysisResult;
			}
		}
		

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return analysisResult;
		}

		analysisResult.addAllStatement = (ExpressionStatement) methodInvocation.getParent();
		if (analysisResult.addAllStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return analysisResult;
		}

		Block addAllStatementParentBlock = (Block) analysisResult.addAllStatement.getParent();

		int indexOfStatementBefore = addAllStatementParentBlock.statements()
			.indexOf(analysisResult.addAllStatement) - 1;

		if (indexOfStatementBefore < 0) {
			return analysisResult;
		}

		Statement stmBefore = ASTNodeUtil
			.convertToTypedList(addAllStatementParentBlock.statements(), Statement.class)
			.get(indexOfStatementBefore);

		if (stmBefore.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			return analysisResult;
		}

		VariableDeclarationStatement variableDeclarationBeforeAddAll = (VariableDeclarationStatement) stmBefore;

		if (variableDeclarationBeforeAddAll.fragments()
			.size() != 1) {
			return analysisResult;
		}

		VariableDeclarationFragment variableDeclarationFragment = ASTNodeUtil
			.convertToTypedList(variableDeclarationBeforeAddAll.fragments(),
					VariableDeclarationFragment.class)
			.get(0);

		String nameOfVariableDeclaredBeforeAddAll = variableDeclarationFragment
			.getName()
			.getIdentifier();

		if (!analysisResult.addAllExpression.getIdentifier()
			.equals(nameOfVariableDeclaredBeforeAddAll)) {
			return analysisResult;
		}

		Expression initializer = variableDeclarationFragment.getInitializer();

		if (initializer == null
				|| initializer.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return analysisResult;
		}

		analysisResult.instanceCreation = (ClassInstanceCreation) initializer;

		if (!analysisResult.instanceCreation.arguments()
			.isEmpty()) {
			return analysisResult;
		}

		if (analysisResult.instanceCreation.getAnonymousClassDeclaration() != null) {
			return analysisResult;
		}

		IMethodBinding constructorBinding = analysisResult.instanceCreation.resolveConstructorBinding();
		ITypeBinding declaringClass = constructorBinding.getDeclaringClass();

		if (!declaringClass.getQualifiedName()
			.startsWith("java.util")) { //$NON-NLS-1$
			return analysisResult;
		}

		analysisResult.isValid = true;
		return analysisResult;
	}

	private boolean isCollectionVariable(ITypeBinding typeBinding) {

		boolean isInheritingCollection = //
				ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Collections.singletonList(JAVA_UTIL_COLLECTION));

		boolean isCollection = ClassRelationUtil.isContentOfType(typeBinding, JAVA_UTIL_COLLECTION);

		return isCollection || isInheritingCollection;
	}

	private void applyRule(Expression argument, ExpressionStatement expressionStatement,
			ClassInstanceCreation instanceCreation) {
		ListRewrite listRewrite = astRewrite.getListRewrite(
				instanceCreation, ClassInstanceCreation.ARGUMENTS_PROPERTY);

		listRewrite.insertFirst(astRewrite.createCopyTarget(argument),
				null);
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> leadingComments = commentRewriter.findLeadingComments(expressionStatement);
		List<Comment> trailingComments = commentRewriter.findTrailingComments(expressionStatement);
		Statement instanceCreationParent = ASTNodeUtil.getSpecificAncestor(instanceCreation, Statement.class);
		Collections.reverse(leadingComments);
		commentRewriter.saveAfterStatement(instanceCreationParent, trailingComments);
		commentRewriter.saveAfterStatement(instanceCreationParent, leadingComments);

		astRewrite.remove(expressionStatement, null);
		onRewrite();
	}

}
