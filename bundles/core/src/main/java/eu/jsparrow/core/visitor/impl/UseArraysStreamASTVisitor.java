package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseArraysStreamASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		boolean hasRightType = isMethodOnType(methodInvocation, "asList", java.util.Arrays.class.getName());
		if (!hasRightType) {
			return true;
		}
		StructuralPropertyDescriptor propertyDescriptor = methodInvocation.getLocationInParent();
		if (propertyDescriptor != MethodInvocation.EXPRESSION_PROPERTY) {
			return true;
		}
		MethodInvocation parent = (MethodInvocation) methodInvocation.getParent();
		if (!isMethodOnType(parent, "stream", java.util.Collection.class.getName())) {
			return false;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.isEmpty()) {
			return true;
		}
		Expression argument = arguments.get(0);
		ITypeBinding argumentTypeBinding = argument.resolveTypeBinding();
		if (arguments.size() > 1 || !argumentTypeBinding.isArray()) {
			if (hasSpecializedStream(argumentTypeBinding)) {
				List<MethodInvocation> methodChain = extractInvocationChain(parent);
				// 1. the last element in the method chain should be forEach
				// 2. every method in the chain should be a higher order function
				// 3. the tricky part: the parameter should never be used as a boxed primitive. otherwise, there will be a compilation error. 
				return false;
			}

			/*
			 * FIXME: doesn't seem like a good idea to convert:
			 * 		Arrays.asList("1", "2", "3").stream() 
			 * to: 
			 * 		Arrays.stream(new String[]{"1", "2", "3"}) 
			 * 
			 * The new code just looks longer.
			 * Additionally, it is not trivial how to derive/import/treat the
			 * type of the components type of the array creation. Afterall,
			 * there is no performance benefit unless we avoid implicit boxing,
			 * which is handled in the previous if block.
			 */

			AST ast = methodInvocation.getAST();
			ArrayCreation arrayCreation = ast.newArrayCreation();
			// FIXME: make sure the type of the argument is a simle type,
			// without wildcards, boundaries, etc...
			ArrayType arrayType = ast.newArrayType(ast.newSimpleType(ast.newName(argumentTypeBinding.getName())));
			arrayCreation.setType(arrayType);
			ArrayInitializer initializer = ast.newArrayInitializer();
			@SuppressWarnings("unchecked")
			List<Expression> initializerExpressions = initializer.expressions();
			arguments.stream()
				.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
				.forEach(initializerExpressions::add);
			arrayCreation.setInitializer(initializer);

			Expression experssion = methodInvocation.getExpression();
			ListRewrite listRewrite = astRewrite.getListRewrite(parent, MethodInvocation.ARGUMENTS_PROPERTY);
			listRewrite.insertFirst(arrayCreation, null);
			astRewrite.replace(parent.getExpression(), astRewrite.createCopyTarget(experssion), null);
			onRewrite();
			/*
			 * TODO: create a new array for the parameter of Arrays.stream()
			 */
		} else {
			ITypeBinding componentType = argumentTypeBinding.getComponentType();
			if (hasSpecializedStream(componentType)) {
				return false;
			}

			/*
			 * TODO: make a simple replacement. No new array creation is needed.
			 * 
			 */
			// FIXME: consider the case when the expression is missing. (e.g. is
			// in static imports)
			Expression experssion = methodInvocation.getExpression();
			ListRewrite listRewrite = astRewrite.getListRewrite(parent, MethodInvocation.ARGUMENTS_PROPERTY);
			listRewrite.insertFirst(astRewrite.createCopyTarget(argument), null);
			astRewrite.replace(parent.getExpression(), astRewrite.createCopyTarget(experssion), null);
			onRewrite();
		}
		/*
		 * TODO: 1. Check type and name to match with Arrays.asList 2. Check
		 * that the method invocation is the expression property of stream()
		 * invocation 3. Verify the parameters of the asList() invocation: - Can
		 * we do this with all types -
		 * 
		 */
		return true;
	}

	private List<MethodInvocation> extractInvocationChain(MethodInvocation methodInvocation) {
		List<MethodInvocation> chain = new ArrayList<>();
		
		StructuralPropertyDescriptor locationInParent = methodInvocation.getLocationInParent();
		if(locationInParent == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation parent = (MethodInvocation)methodInvocation.getParent();
			chain.add(parent);
			chain.addAll(extractInvocationChain(parent));
		}
		
		return chain;
	}

	private boolean hasSpecializedStream(ITypeBinding argumentTypeBinding) {
		String typeName = argumentTypeBinding.getName();
		switch (typeName) {
		case "int":
		case "double":
		case "long":
			return true;
		default:
			return false;
		}
	}

	private boolean isMethodOnType(MethodInvocation methodInvocation, String expectedName, String expectedType) {
		SimpleName name = methodInvocation.getName();
		if (!expectedName.equals(name.getIdentifier())) {
			return false;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfType(declaringClass, expectedType);
	}
}
