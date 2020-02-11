package eu.jsparrow.core.visitor.impl;

import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
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

/**
 * The visitor first searches the next invocation of the method
 * {@link java.util.Collection#addAll(java.util.Collection)} on a local
 * variable.<br>
 * If the previous statement is the statement declaring the same local variable
 * then the visitor looks for the initializer which must be a constructor
 * invocation without any argument.<br>
 * If such an initializer is found then the visitor copies the argument of the
 * add all invocation to the argument list of the constructor invocation and
 * finally removes the addAll-invocation.
 * 
 *
 * 
 */
public class RemoveCollectionsAddAllASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String JAVA_UTIL_COLLECTION = java.util.Collection.class.getName();

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		if (!isCollectionAddAllMethod(methodInvocation)) {
			return true;
		}

		if (methodInvocation.getExpression()
			.getNodeType() != ASTNode.SIMPLE_NAME) {
			return true;
		}
		SimpleName simpleInvocationQualifier = (SimpleName) methodInvocation.getExpression();

		if (methodInvocation.getParent()
			.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return true;
		}

		if (methodInvocation.getParent()
			.getParent()
			.getNodeType() != ASTNode.BLOCK) {
			return true;
		}

		ExpressionStatement expressionStatement = (ExpressionStatement) methodInvocation.getParent();
		Block block = (Block) expressionStatement.getParent();

		int indexOfStatementBefore = block.statements()
			.indexOf(expressionStatement) - 1;

		if (indexOfStatementBefore < 0) {
			return true;
		}

		Statement stmBefore = ASTNodeUtil
			.convertToTypedList(block.statements(), Statement.class)
			.get(indexOfStatementBefore);

		if (stmBefore.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			return true;
		}
		VariableDeclarationStatement varDeclStm = (VariableDeclarationStatement) stmBefore;

		if (varDeclStm.fragments()
			.size() != 1) {
			return true;
		}
		VariableDeclarationFragment variableDeclarationFragment = ASTNodeUtil
			.convertToTypedList(varDeclStm.fragments(), VariableDeclarationFragment.class)
			.get(0);

		String varName = variableDeclarationFragment
			.getName()
			.getIdentifier();

		if (!simpleInvocationQualifier.getIdentifier()
			.equals(varName)) {
			return true;
		}

		Expression initializer = variableDeclarationFragment.getInitializer();

		if (initializer == null || initializer.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return true;
		}

		ClassInstanceCreation instanceCreation = (ClassInstanceCreation) initializer;
		if (!instanceCreation.arguments()
			.isEmpty()) {
			return true;

		}
		Expression argument = ASTNodeUtil
			.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);
		applyRule(argument, expressionStatement, instanceCreation);

		return true;
	}

	private void applyRule(Expression argument, ExpressionStatement expressionStatement,
			ClassInstanceCreation instanceCreation) {
		ListRewrite listRewrite = astRewrite.getListRewrite(
				instanceCreation, ClassInstanceCreation.ARGUMENTS_PROPERTY);

		listRewrite.insertFirst(astRewrite.createCopyTarget(argument),
				null);
		astRewrite.remove(expressionStatement, null);
		onRewrite();
	}

	private static boolean isCollectionAddAllMethod(MethodInvocation methodInvocation) {

		String identifier = methodInvocation.getName()
			.getIdentifier();
		if (!"addAll".equals(identifier)) { //$NON-NLS-1$
			return false;
		}

		if (methodInvocation.arguments()
			.size() != 1) {
			return false;
		}

		return isCollectionTypeBinding(methodInvocation.getExpression());
	}

	private static boolean isCollectionTypeBinding(Expression expression) {
		if (expression == null) {
			return false;
		}
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		boolean isInheritingCollection = //
				ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Collections.singletonList(JAVA_UTIL_COLLECTION));

		boolean isCollection = ClassRelationUtil.isContentOfType(typeBinding, JAVA_UTIL_COLLECTION);

		return isCollection || isInheritingCollection;
	}

}
