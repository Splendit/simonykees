package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import eu.jsparrow.core.visitor.sub.MethodInvocationsVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

/**
 * Visitor for removing unused parameters in private methods.
 * 
 * @since 3.4.0
 *
 */
public class RemoveUnusedParameterASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		if (!ASTNodeUtil.hasModifier(methodDeclaration.modifiers(), Modifier::isPrivate)) {
			return true;
		}

		List<SingleVariableDeclaration> parameters = ASTNodeUtil.convertToTypedList(methodDeclaration.parameters(),
				SingleVariableDeclaration.class);
		if (parameters.isEmpty()) {
			return true;
		}

		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		if (methodBinding == null) {
			return true;
		}

		List<IMethodBinding> overloadedMethods = ClassRelationUtil.findOverloadedMethods(methodBinding,
				methodBinding.getDeclaringClass());
		/*
		 * TODO: skip only overloaded methods that might lead to conflicts
		 */
		if (!overloadedMethods.isEmpty()) {
			/*
			 * Removing parameters in overloaded methods could lead to
			 * conflicts. There is room for improvements.
			 */
			return true;
		}

		for (SingleVariableDeclaration parameter : parameters) {
			analyzeParamter(parameter, methodDeclaration);
		}

		return true;
	}

	private void analyzeParamter(SingleVariableDeclaration parameter, MethodDeclaration methodDeclaration) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(parameter.getName());
		Block methodBody = methodDeclaration.getBody();
		if(methodBody == null) {
			return;
		}
		methodBody.accept(visitor);
		List<SimpleName> references = visitor.getUsages();
		if (!references.isEmpty()) {
			return;
		}

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(methodDeclaration, CompilationUnit.class);
		MethodInvocationsVisitor methodInvocationsVisitor = new MethodInvocationsVisitor(
				methodDeclaration.resolveBinding());
		compilationUnit.accept(methodInvocationsVisitor);
		if (methodInvocationsVisitor.hasUnresolvedBindings()) {
			return;
		}

		List<MethodInvocation> invocations = methodInvocationsVisitor.getMethodInvocations();
		int parameterIndex = methodDeclaration.parameters()
			.indexOf(parameter);

		astRewrite.remove(parameter, null);
		for (MethodInvocation invocation : invocations) {
			Expression invocationParameter = (Expression) invocation.arguments()
				.get(parameterIndex);
			astRewrite.remove(invocationParameter, null);
		}
		onRewrite();
		/*
		 * TODO save comments
		 */

	}

}
