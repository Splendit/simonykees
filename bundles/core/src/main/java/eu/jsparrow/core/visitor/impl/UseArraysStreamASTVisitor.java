package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseArraysStreamASTVisitor extends AbstractASTRewriteASTVisitor {

	
	@SuppressWarnings("nls")
	private static final List<String> BLOCKED_LIST = Collections.unmodifiableList(Arrays.asList(
			"mapToInt",
			"mapToLong",
			"mapToDouble",
			"flatMap",
			"flatMapToInt",
			"flatMapToLong",
			"flatMapToDouble",
			"toArray",
			"sorted",
			"toArray",
			"reduce",
			"collect",
			"findFirst",
			"findAny",
			"iterate",
			"generate",
			"concat"));

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		boolean hasRightType = isMethodDeclaredOnType(methodInvocation, "asList", java.util.Arrays.class.getName()); //$NON-NLS-1$
		if (!hasRightType) {
			return true;
		}
		StructuralPropertyDescriptor propertyDescriptor = methodInvocation.getLocationInParent();
		if (propertyDescriptor != MethodInvocation.EXPRESSION_PROPERTY) {
			return true;
		}
		MethodInvocation parent = (MethodInvocation) methodInvocation.getParent();
		if (!isMethodDeclaredOnType(parent, "stream", java.util.Collection.class.getName())) { //$NON-NLS-1$
			return false;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.isEmpty()) {
			return true;
		}
		Expression argument = arguments.get(0);
		ITypeBinding argumentTypeBinding = argument.resolveTypeBinding();
		if (!argumentTypeBinding.isArray() && hasSpecializedStream(argumentTypeBinding)) {
			List<MethodInvocation> methodChain = extractStreamChain(parent);
			if (!isCompatibleWithSpecializedStream(methodChain, parent)) {
				return false;
			}
			/*
			 * HERE create the new array and make the proper replacement
			 */
			AST ast = methodInvocation.getAST();
			ArrayCreation arrayCreation = ast.newArrayCreation();
			// FIXME: make sure the type of the argument is a simple type,
			// without wildcards, boundaries, etc...
			Code primitiveType = PrimitiveType.toCode(argumentTypeBinding.getName());
			ArrayType arrayType = ast.newArrayType(ast.newPrimitiveType(primitiveType));
			arrayCreation.setType(arrayType);
			ArrayInitializer initializer = ast.newArrayInitializer();
			@SuppressWarnings("unchecked")
			List<Expression> initializerExpressions = initializer.expressions();
			arguments.stream()
				.map(arg -> (Expression) astRewrite.createMoveTarget(arg))
				.forEach(initializerExpressions::add);
			arrayCreation.setInitializer(initializer);

			
			ListRewrite listRewrite = astRewrite.getListRewrite(parent, MethodInvocation.ARGUMENTS_PROPERTY);
			listRewrite.insertFirst(arrayCreation, null);
			Expression experssion = methodInvocation.getExpression();//FIXMEexpression can be null
			astRewrite.replace(parent.getExpression(), astRewrite.createCopyTarget(experssion), null);
			onRewrite();
			
			return true;
		} else {

			AST ast = methodInvocation.getAST();
			Expression expression = ast.newSimpleName(java.util.stream.Stream.class.getSimpleName());
			ListRewrite listRewrite = astRewrite.getListRewrite(parent, MethodInvocation.ARGUMENTS_PROPERTY);
			arguments.forEach(arg -> listRewrite.insertLast(astRewrite.createMoveTarget(arg), null));

			// FIXME: make sure that java.util.Stream is imported.
			astRewrite.replace(parent.getExpression(), expression, null);
			astRewrite.replace(parent.getName(), ast.newSimpleName("of"), null); //$NON-NLS-1$
			onRewrite();
			return true;

		}
	}

	private boolean isCompatibleWithSpecializedStream(List<MethodInvocation> methodChain, MethodInvocation parent) {
		ITypeBinding typeBinding = parent.resolveTypeBinding();
		boolean compatible = true;
		
		for (MethodInvocation method : methodChain) {
			boolean compatibleWithUnboxed = isCompatibleWithUnboxedParameter(method);
			if (!compatibleWithUnboxed) {
				compatible = false;
			}
			ITypeBinding newStreamType = method.resolveTypeBinding();
			if (!compatible || ClassRelationUtil.compareITypeBinding(typeBinding, newStreamType)) {
				break;
			}
		}
		return compatible;
	}

	private boolean isCompatibleWithUnboxedParameter(MethodInvocation method) {
		SimpleName name = method.getName();
		if (BLOCKED_LIST.contains(name.getIdentifier())) {
			return false;
		}
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(method.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return false;
		}
		Expression argument = arguments.get(0);
		if (argument.getNodeType() != ASTNode.LAMBDA_EXPRESSION) {
			return false;
		}

		LambdaExpression lambdaExpression = (LambdaExpression) argument;
		ASTNode lambdaBody = lambdaExpression.getBody();
		UnboxCompatibilityVisitor visitor = new UnboxCompatibilityVisitor(name);
		lambdaBody.accept(visitor);
		
		/*
		 * 1. Make sure the parameter is a lambda expression and it is the only
		 * parameter
		 * 
		 * 2. Create a visitor for the body of the lambda expression. Make sure
		 * the parameter is never used as expression of a method invocation. ->
		 * The big question: when is a boxed primitive not compatible with the
		 * primitive value
		 */
		return !visitor.isIncompatible();
	}

	private boolean oldImplementation(MethodInvocation methodInvocation) {
		boolean hasRightType = isMethodDeclaredOnType(methodInvocation, "asList", java.util.Arrays.class.getName());
		if (!hasRightType) {
			return true;
		}
		StructuralPropertyDescriptor propertyDescriptor = methodInvocation.getLocationInParent();
		if (propertyDescriptor != MethodInvocation.EXPRESSION_PROPERTY) {
			return true;
		}
		MethodInvocation parent = (MethodInvocation) methodInvocation.getParent();
		if (!isMethodDeclaredOnType(parent, "stream", java.util.Collection.class.getName())) {
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
				List<MethodInvocation> methodChain = extractStreamChain(parent);

				// 1. the last element in the method chain should be forEach
				// 2. every method in the chain should be a higher order
				// function
				// 3. the tricky part: the parameter should never be used as a
				// boxed primitive. otherwise, there will be a compilation
				// error.
				return false;
			}

			/*
			 * FIXME: doesn't seem like a good idea to convert:
			 * Arrays.asList("1", "2", "3").stream() to: Arrays.stream(new
			 * String[]{"1", "2", "3"})
			 * 
			 * The new code just looks longer. Additionally, it is not trivial
			 * how to derive/import/treat the type of the components type of the
			 * array creation. Afterall, there is no performance benefit unless
			 * we avoid implicit boxing, which is handled in the previous if
			 * block.
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

			// FIXME: consider the case when the expression is missing. (e.g. is
			// in static imports)
			// FIXME: make sure that java.util.Stream is imported.
			Expression experssion = methodInvocation.getExpression();
			ListRewrite listRewrite = astRewrite.getListRewrite(parent, MethodInvocation.ARGUMENTS_PROPERTY);
			listRewrite.insertLast(astRewrite.createCopyTarget(argument), null);
			astRewrite.replace(parent.getExpression(), astRewrite.createCopyTarget(experssion), null);
			onRewrite();
		}
		return true;
	}

	private List<MethodInvocation> extractStreamChain(MethodInvocation methodInvocation) {
		List<MethodInvocation> chain = new ArrayList<>();

		StructuralPropertyDescriptor locationInParent = methodInvocation.getLocationInParent();
		if (locationInParent == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation parent = (MethodInvocation) methodInvocation.getParent();
			IMethodBinding parentMethodBinding = parent.resolveMethodBinding();
			ITypeBinding declaringClass = parentMethodBinding.getDeclaringClass();
			boolean isStreamMethod = ClassRelationUtil.isContentOfType(declaringClass,
					java.util.stream.Stream.class.getName());
			if (isStreamMethod) {
				chain.add(parent);
				chain.addAll(extractStreamChain(parent));
			}
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

	private boolean isMethodDeclaredOnType(MethodInvocation methodInvocation, String expectedName,
			String expectedType) {
		SimpleName name = methodInvocation.getName();
		if (!expectedName.equals(name.getIdentifier())) {
			return false;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfType(declaringClass, expectedType);
	}
	
	private boolean checkMethodChain(List<MethodInvocation> methodChain) {
		if (!methodChain.isEmpty()) {

			MethodInvocation last = methodChain.get(methodChain.size() - 1);
			IMethodBinding lastMethodBinding = last.resolveMethodBinding();
			ITypeBinding lastReturnType = lastMethodBinding.getReturnType();
			boolean isVoid = "void".equals(lastReturnType.getName());

			for (MethodInvocation method : methodChain) {
				ITypeBinding typeBinding = method.resolveTypeBinding();
				ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
				if (typeArguments.length == 1) {
					ITypeBinding typeArgument = typeArguments[0];
					if (hasSpecializedStream(typeArgument)) {
						boolean compatible = isCompatibleWithUnboxedParameter(method);
						if (!compatible) {
							return true;
						}
					}
				}
			}

		}
		return false;
	}

	class UnboxCompatibilityVisitor extends ASTVisitor {

		private SimpleName parameter;
		private boolean incompatible;

		public UnboxCompatibilityVisitor(SimpleName parameter) {
			this.parameter = parameter;
		}

		@Override
		public boolean visit(SimpleName simpleName) {
			String identifier = simpleName.getIdentifier();
			if (!identifier.equals(parameter.getIdentifier())) {
				return false;
			}
			IBinding binding = simpleName.resolveBinding();
			if (binding.getKind() != IBinding.VARIABLE) {
				return false;
			}

			List<StructuralPropertyDescriptor> properties = findStructuralProperties(simpleName);
			incompatible = properties.stream().anyMatch(property -> property == MethodInvocation.EXPRESSION_PROPERTY);

			return false;
		}

		private List<StructuralPropertyDescriptor> findStructuralProperties(ASTNode simpleName) {
			List<StructuralPropertyDescriptor> properties = new ArrayList<>();
			properties.add(simpleName.getLocationInParent());
			ASTNode parent = simpleName.getParent();

			if (parent != null
					&& parent.getNodeType() != ASTNode.LAMBDA_EXPRESSION
					&& !(parent instanceof Statement)) {
				properties.addAll(findStructuralProperties(parent));
			}
			return properties;
		}
		
		public boolean isIncompatible() {
			return incompatible;
		}
	}
}
