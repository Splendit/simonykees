package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;

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

	private static final String JAVA_UTIL_COLLECTION = java.util.Collection.class.getName();

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		AddAllAnalysisResult analysisResult = analyzeInvocation(methodInvocation);
		if (analysisResult == null) {
			return true;
		}
		VariableDeclarationStatement variableDeclarationBeforeAddAll = getVariableDeclarationBeforeAddAll(
				analysisResult.addAllStatement);
		if (variableDeclarationBeforeAddAll == null) {
			return true;
		}
		ClassInstanceCreation instanceCreation = analyzeVariableDeclarationBeforeAddAll(variableDeclarationBeforeAddAll,
				analysisResult.addAllExpression);
		if (instanceCreation == null) {
			return true;
		}

		applyRule(analysisResult.addAllArgument, analysisResult.addAllStatement, instanceCreation);
		return true;
	}

	private AddAllAnalysisResult analyzeInvocation(MethodInvocation methodInvocation) {
		Expression invocationExpression = methodInvocation.getExpression();
		if (invocationExpression == null) {
			return null;
		}

		if (invocationExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return null;
		}

		SimpleName addAllExpression = (SimpleName) invocationExpression;
		if (!isCollectionVariable(addAllExpression.resolveTypeBinding())) {
			return null;
		}

		if (!"addAll".equals(methodInvocation.getName() //$NON-NLS-1$
			.getIdentifier())) {
			return null;
		}

		if (methodInvocation.arguments()
			.size() != 1) {
			return null;
		}

		Expression addAllArgument = convertToTypedList(methodInvocation.arguments(), Expression.class).get(0);
		if (addAllArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName variableArgument = (SimpleName) addAllArgument;
			if (variableArgument.getIdentifier()
				.equals(addAllExpression.getIdentifier())) {
				return null;
			}
		}

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return null;
		}
		ExpressionStatement addAllStatement = (ExpressionStatement) methodInvocation.getParent();
		return new AddAllAnalysisResult(addAllExpression, addAllArgument, addAllStatement);
	}

	private VariableDeclarationStatement getVariableDeclarationBeforeAddAll(ExpressionStatement addAllStatement) {

		if (addAllStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}
		Block parentBlock = (Block) addAllStatement.getParent();
		@SuppressWarnings("rawtypes")
		List blockStatemetns = parentBlock.statements();
		int indexOfStatementBefore = blockStatemetns.indexOf(addAllStatement) - 1;
		if (indexOfStatementBefore < 0) {
			return null;
		}

		Statement stmBefore = convertToTypedList(blockStatemetns, Statement.class).get(indexOfStatementBefore);

		if (stmBefore.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			return null;
		}

		return (VariableDeclarationStatement) stmBefore;

	}

	private ClassInstanceCreation analyzeVariableDeclarationBeforeAddAll(
			VariableDeclarationStatement variableDeclarationBeforeAddAll, SimpleName addAllExpression) {

		if (variableDeclarationBeforeAddAll.fragments()
			.size() != 1) {
			return null;
		}

		VariableDeclarationFragment variableDeclarationFragment = convertToTypedList(
				variableDeclarationBeforeAddAll.fragments(), VariableDeclarationFragment.class).get(0);

		String nameOfVariableDeclaredBeforeAddAll = variableDeclarationFragment.getName()
			.getIdentifier();

		if (!nameOfVariableDeclaredBeforeAddAll.equals(addAllExpression.getIdentifier())) {
			return null;
		}

		Expression initializer = variableDeclarationFragment.getInitializer();

		if (initializer == null) {
			return null;
		}

		if (initializer.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return null;
		}

		ClassInstanceCreation instanceCreation = (ClassInstanceCreation) initializer;

		if (!instanceCreation.arguments()
			.isEmpty()) {
			return null;
		}

		if (instanceCreation.getAnonymousClassDeclaration() != null) {
			return null;
		}

		IMethodBinding constructorBinding = instanceCreation.resolveConstructorBinding();
		ITypeBinding declaringClass = constructorBinding.getDeclaringClass();

		if (!declaringClass.getQualifiedName()
			.startsWith("java.util")) { //$NON-NLS-1$
			return null;
		}

		return instanceCreation;
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
		ListRewrite listRewrite = astRewrite.getListRewrite(instanceCreation, ClassInstanceCreation.ARGUMENTS_PROPERTY);

		listRewrite.insertFirst(astRewrite.createCopyTarget(argument), null);
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

	private class AddAllAnalysisResult {

		SimpleName addAllExpression;
		Expression addAllArgument;
		ExpressionStatement addAllStatement;

		public AddAllAnalysisResult(SimpleName addAllExpression, Expression addAllArgument,
				ExpressionStatement addAllStatement) {
			this.addAllExpression = addAllExpression;
			this.addAllArgument = addAllArgument;
			this.addAllStatement = addAllStatement;
		}

	}
}
