package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IDocElement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TagElement;

import eu.jsparrow.core.visitor.sub.MethodInvocationsVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
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

		if (methodDeclaration.isConstructor()) {
			return true;
		}

		List<SingleVariableDeclaration> parameters = convertToTypedList(methodDeclaration.parameters(),
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
		if (methodBody == null) {
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

		List<ExpressionMethodReference> methodReferences = methodInvocationsVisitor.getExpressionMethodReferences();
		if (!methodReferences.isEmpty()) {
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
			CommentRewriter commentRewriter = getCommentRewriter();
			List<Comment> comments = commentRewriter.findLeadingComments(invocationParameter);
			comments.addAll(commentRewriter.findTrailingComments(invocationParameter));
			Statement parentStatement = ASTNodeUtil.getSpecificAncestor(invocationParameter, Statement.class);
			commentRewriter.saveBeforeStatement(parentStatement, comments);
		}

		updateJavaDoc(methodDeclaration, parameter);
		onRewrite();

	}

	private void updateJavaDoc(MethodDeclaration methodDeclaration, SingleVariableDeclaration parameter) {
		Javadoc javaDoc = methodDeclaration.getJavadoc();
		if (javaDoc == null) {
			return;
		}

		SimpleName parameterName = parameter.getName();
		String parameterIdentifier = parameterName.getIdentifier();

		convertToTypedList(javaDoc.tags(), TagElement.class).stream()
			.filter(tag -> "@param".equals(tag.getTagName())) //$NON-NLS-1$
			.forEach(tag -> convertToTypedList(tag.fragments(), IDocElement.class).stream()
				.filter(fragment -> fragment instanceof SimpleName)
				.filter(fragment -> parameterIdentifier.equals(((SimpleName) fragment).getIdentifier()))
				.findFirst()
				.ifPresent(fragment -> astRewrite.remove(tag, null)));
	}

}
