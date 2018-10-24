package eu.jsparrow.core.visitor.impl;

import java.util.Collections;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseStringBuilderAppendASTVisitor extends AbstractASTRewriteASTVisitor {
	
	private static final InfixExpression.Operator PLUS = InfixExpression.Operator.PLUS;
	
	@Override
	public boolean visit(InfixExpression infixExpression) {
		
		ITypeBinding typeBinding = infixExpression.resolveTypeBinding();
		if(!ClassRelationUtil.isContentOfTypes(typeBinding, Collections.singletonList(java.lang.String.class.getName()))) {
			return false;
		}
		
		InfixExpression.Operator operator = infixExpression.getOperator();
		if(!PLUS.equals(operator)) {
			return false;
		}
		
		/*
		 * TODO check recursively for nested operands.
		 */
		AST ast = infixExpression.getAST();
		MethodInvocation toString = ast.newMethodInvocation();
		toString.setName(ast.newSimpleName("toString"));
		
		ClassInstanceCreation stringBuilder = ast.newClassInstanceCreation();
		stringBuilder.setType(ast.newSimpleType(ast.newName(StringBuilder.class.getSimpleName())));
		
		MethodInvocation appendLeft = ast.newMethodInvocation();
		appendLeft.setName(ast.newSimpleName("append"));
		appendLeft.arguments().add(astRewrite.createCopyTarget(infixExpression.getLeftOperand()));
		appendLeft.setExpression(stringBuilder);
		
		MethodInvocation appendRight = ast.newMethodInvocation();
		appendRight.setName(ast.newSimpleName("append"));
		appendRight.arguments().add(astRewrite.createCopyTarget(infixExpression.getRightOperand()));
		appendRight.setExpression(appendLeft);
		
		toString.setExpression(appendRight);
		
		astRewrite.replace(infixExpression, toString, null);
		
		return false;
	}

}
