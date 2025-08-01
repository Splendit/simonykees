package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.compareBoxedITypeBinding;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.compareITypeBinding;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.findInheretedMethods;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.findOverloadedMethods;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isOverloadedOnParameter;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isVisibleIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.LambdaToMethodReferenceEvent;
import eu.jsparrow.core.visitor.utils.LambdaNodeUtil;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * converts lambda expressions to method references of the form
 * {@code <Expression>::<MethodName>}. statement lambdas have to be converted to
 * expression lambdas first, using {@link StatementLambdaToExpressionASTVisitor}
 * 
 * @author Matthias Webhofer, Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaToMethodReferenceASTVisitor extends AbstractAddImportASTVisitor
		implements LambdaToMethodReferenceEvent {

	private Set<String> newImports = new HashSet<>();

	@Override
	public void endVisit(CompilationUnit cu) {
		super.addAlreadyVerifiedImports(newImports);
		super.endVisit(cu);
	}

	@Override
	public boolean visit(LambdaExpression lambdaExpressionNode) {

		ITypeBinding contextType = LambdaNodeUtil.findContextType(lambdaExpressionNode)
			.orElse(null);
		if (contextType == null) {
			return true;
		}
		IMethodBinding expectedFunctionalInterface = contextType.getFunctionalInterfaceMethod();
		IMethodBinding lambdaBinding = lambdaExpressionNode.resolveMethodBinding();
		if (areIncompatibleFunctionalInterfaces(expectedFunctionalInterface, lambdaBinding)) {
			return true;
		}
		Expression body = extractSingleBodyExpression(lambdaExpressionNode).orElse(null);
		// work only with expression lambdas
		if (body == null) {
			return true;
		}

		List<VariableDeclaration> lambdaParams = ASTNodeUtil.convertToTypedList(lambdaExpressionNode.parameters(),
				VariableDeclaration.class);

		// only single method invocations are relevant for cases 1, 2 and 3
		if (ASTNode.METHOD_INVOCATION == body.getNodeType()) {
			MethodInvocation methodInvocation = (MethodInvocation) body;
			List<Expression> methodArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
					Expression.class);
			Expression methodInvocationExpression = methodInvocation.getExpression();

			if (methodInvocation.resolveMethodBinding() == null) {
				return true;
			}

			if (isWrappedInOverloadedMethod(lambdaExpressionNode, methodInvocation)) {
				return true;
			}

			/*
			 * case 1: reference to static method
			 * 
			 * case 2: reference to instance method i.e.
			 * 
			 * personList.forEach(element -> System.out.println(element))
			 * 
			 * becomes
			 * 
			 * personList.forEach(System.out::println)
			 * 
			 * case 3: reference to 'this' i.e.
			 * 
			 * personList.forEach(person -> doSomething(person))
			 * 
			 * becomes
			 * 
			 * personList.forEach(this::doSomething)
			 */
			if (methodArguments.size() == lambdaParams.size()
					&& checkMethodParameters(lambdaParams, methodArguments)) {

				IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();

				List<ITypeBinding> ambParams = methodArguments.stream()
					.skip(1)
					.map(Expression::resolveTypeBinding)
					.collect(Collectors.toList());
				if (isAmbiguousMethodReference(methodInvocation, ambParams)) {
					return true;
				}

				ExpressionMethodReference ref = astRewrite.getAST()
					.newExpressionMethodReference();
				Expression refExpression = null;

				// save type arguments
				saveTypeArguments(methodInvocation, ref);

				boolean isReferenceExpressionSet = false;

				// no expression present -> assume 'this'
				if (methodInvocationExpression == null) {

					/*
					 * Ensure that the lambda expression is enclosed in the same
					 * class as the method which is being referenced. We have to
					 * check this, because the method could be declared in the
					 * outer class.
					 */
					ITypeBinding methodsDeclaringClass = methodBinding.getDeclaringClass();
					AbstractTypeDeclaration lambdaEnclosing = ASTNodeUtil.getSpecificAncestor(lambdaExpressionNode,
							AbstractTypeDeclaration.class);
					ITypeBinding lambdaEnclosingType = lambdaEnclosing.resolveBinding();

					if (Modifier.isStatic(methodBinding.getModifiers())) {
						ITypeBinding declaringClassErasure = methodsDeclaringClass.getErasure();
						newImports.add(declaringClassErasure.getQualifiedName());
						SimpleName staticClassName = astRewrite.getAST()
							.newSimpleName(declaringClassErasure.getName());
						ref.setExpression(staticClassName);
						isReferenceExpressionSet = true;
					} else if (compareITypeBinding(methodsDeclaringClass, lambdaEnclosingType)) {
						ClassInstanceCreation enclosingAnonymousInnerClass = ASTNodeUtil
							.getSpecificAncestor(lambdaExpressionNode, ClassInstanceCreation.class);
						if (enclosingAnonymousInnerClass == null) {
							ThisExpression thisExpression = astRewrite.getAST()
								.newThisExpression();
							ref.setExpression(thisExpression);
							isReferenceExpressionSet = true;
						}
					}

				}
				// simple name, qualified name or 'this'
				else if (methodInvocationExpression instanceof Name
						|| ASTNode.THIS_EXPRESSION == methodInvocationExpression.getNodeType()) {

					boolean paramUserForMethodInvocation = false;
					if (methodInvocationExpression instanceof Name) {
						Name name = (Name) methodInvocationExpression;
						String nameStr = name.getFullyQualifiedName();
						paramUserForMethodInvocation = this.containsName(methodArguments, nameStr);
					}

					if (!paramUserForMethodInvocation) {
						Expression newMethodInvocationExpression = (Expression) astRewrite
							.createCopyTarget(methodInvocation.getExpression());
						ref.setExpression(newMethodInvocationExpression);
						refExpression = methodInvocation.getExpression();
						isReferenceExpressionSet = true;
					}
				}

				if (isReferenceExpressionSet) {
					SimpleName methodName = (SimpleName) astRewrite.createCopyTarget(methodInvocation.getName());
					ref.setName(methodName);
					astRewrite.replace(lambdaExpressionNode, ref, null);
					getCommentRewriter().saveCommentsInParentStatement(lambdaExpressionNode);
					onRewrite();
					refExpression = refExpression == null ? ref.getExpression() : refExpression;
					addMarkerEvent(lambdaExpressionNode, refExpression, methodInvocation.getName());
				}
			}

			/*
			 * case 4: reference to instance method of arbitrary type i.e.
			 * 
			 * Arrays.sort(stringArray, (a, b) -> a.compareToIgnoreCase(b))
			 * 
			 * becomes
			 * 
			 * Arrays.sort(stringArray, String::compareToIgnoreCase)
			 */
			else if ((lambdaParams.size() - 1) == methodArguments.size() && methodInvocationExpression != null) {
				replaceWithExpressionMethodReference(lambdaExpressionNode, lambdaParams, methodInvocation,
						methodArguments);
			}
		}

		/*
		 * case 5: reference to class instance creation (new) i.e.
		 * 
		 * Set<Person> persSet2 = transferElements(personList, () -> new
		 * HashSet<>())
		 * 
		 * becomes
		 * 
		 * Set<Person> persSet3 = transferElements(personList,
		 * HashSet<Person>::new)
		 */
		else if (ASTNode.CLASS_INSTANCE_CREATION == body.getNodeType()) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) body;
			List<Expression> classInstanceCreationArguments = ASTNodeUtil
				.convertToTypedList(classInstanceCreation.arguments(), Expression.class);

			AnonymousClassDeclaration annonymousClass = classInstanceCreation.getAnonymousClassDeclaration();
			if (annonymousClass == null && lambdaParams.size() == classInstanceCreation.arguments()
				.size() && checkMethodParameters(lambdaParams, classInstanceCreationArguments)) {
				Type classInstanceCreationType = classInstanceCreation.getType();

				CreationReference ref = astRewrite.getAST()
					.newCreationReference();
				if (ASTNode.PARAMETERIZED_TYPE == classInstanceCreationType.getNodeType()
						&& ((ParameterizedType) classInstanceCreationType).typeArguments()
							.isEmpty()) {
					ref.setType((Type) astRewrite
						.createMoveTarget(((ParameterizedType) classInstanceCreationType).getType()));
				} else {
					ref.setType((Type) astRewrite.createCopyTarget(classInstanceCreationType));
				}

				astRewrite.replace(lambdaExpressionNode, ref, null);
				getCommentRewriter().saveCommentsInParentStatement(lambdaExpressionNode);
				onRewrite();
				addMarkerEvent(lambdaExpressionNode, classInstanceCreationType);
			}
		}

		return true;
	}

	private boolean isWrappedInOverloadedMethod(LambdaExpression lambdaExpressionNode,
			MethodInvocation methodInvocation) {
		ASTNode parent = lambdaExpressionNode.getParent();
		if (parent.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}

		MethodInvocation wrapperMethod = (MethodInvocation) parent;
		List<Expression> wrapperMethodParameters = ASTNodeUtil.convertToTypedList(wrapperMethod.arguments(),
				Expression.class);
		int index = wrapperMethodParameters.indexOf(lambdaExpressionNode);

		List<IMethodBinding> overloadedWrapperMethods = findOverloadedMethods(wrapperMethod).stream()
			.filter(method -> Modifier.isPublic(method.getModifiers()))
			.collect(Collectors.toList());
		IMethodBinding wrapperMethodBinding = wrapperMethod.resolveMethodBinding();
		if (wrapperMethodBinding == null) {
			return false;
		}

		boolean isOverloadedWrapperMethod = overloadedWrapperMethods.stream()
			.anyMatch(method -> isOverloadedOnParameter(wrapperMethodBinding, method, index));

		if (isOverloadedWrapperMethod && discardsReturnedType(methodInvocation)) {
			return true;
		}

		Expression expression = methodInvocation.getExpression();
		if (expression != null) {
			ITypeBinding expressionBinidng = expression.resolveTypeBinding();
			if (expressionBinidng != null && expressionBinidng.isRawType() && isOverloadedWrapperMethod) {
				return true;
			}
		}

		List<IMethodBinding> publicOverloadedMethods = findOverloadedMethods(methodInvocation).stream()
			.filter(method -> isVisibleIn(method, wrapperMethod))
			.collect(Collectors.toList());

		if (publicOverloadedMethods.isEmpty()) {
			return false;
		}

		return isOverloadedWrapperMethod;
	}

	private boolean discardsReturnedType(MethodInvocation methodInvocation) {
		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return false;
		}

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		ITypeBinding returnType = methodBinding.getReturnType();
		if (returnType == null) {
			return false;
		}

		String typeName = returnType.getName();
		return !typeName.equals(PrimitiveType.VOID.toString());
	}

	/**
	 * Checks if the transformation of a lambda expression to a method reference
	 * will cause an ambiguity for the java compiler. For example
	 * {@code Integer::toString} causes an ambiguity because it can point to
	 * both {@link Integer#toString()} and {@link Integer#toString(int)}, where
	 * the former is a static method and the latter is an instance method.
	 * 
	 * @param methodInvocation
	 *            a node representing a method invocation
	 * @param params
	 *            the type of the parameters that can cause ambiguity
	 * @return {@code true} if an ambiguity is detected of if the binding of the
	 *         type where the method is declared cannot be resolved, and
	 *         {@code false} otherwise.
	 */
	private boolean isAmbiguousMethodReference(MethodInvocation methodInvocation, List<ITypeBinding> params) {
		Expression expression = methodInvocation.getExpression();
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return true;
		}
		ITypeBinding type;
		List<IMethodBinding> methods = new ArrayList<>();
		if (expression != null) {
			type = expression.resolveTypeBinding();
		} else {
			type = methodBinding.getDeclaringClass();
		}
		if (type == null) {
			return true;
		}

		methods.addAll(Arrays.asList(type.getDeclaredMethods()));
		methods.addAll(findInheretedMethods(type));
		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();

		ITypeBinding[] paramsArray = params.stream()
			.toArray(ITypeBinding[]::new);
		if (Modifier.isStatic(methodBinding.getModifiers())) {
			/*
			 * static methods can cause ambiguity with non-static methods
			 */
			return methods.stream()
				.anyMatch(
						method -> methodIdentifier.equals(method.getName()) && !Modifier.isStatic(method.getModifiers())
								&& compareBoxedITypeBinding(method.getParameterTypes(), paramsArray));
		} else {
			/*
			 * non-static methods can cause ambiguity with static methods
			 */
			return methods.stream()
				.anyMatch(
						method -> methodIdentifier.equals(method.getName()) && Modifier.isStatic(method.getModifiers())
								&& compareBoxedITypeBinding(method.getParameterTypes(), paramsArray));
		}

	}

	/**
	 * Inserts the existing type arguments to the method reference.
	 * 
	 * @param methodInvocation
	 *            original method invocation with possibly nonempty list of type
	 *            arguments
	 * @param ref
	 *            the new {@link ExpressionMethodReference}
	 */
	private void saveTypeArguments(MethodInvocation methodInvocation, ExpressionMethodReference ref) {
		List<Type> typeArguments = ASTNodeUtil.convertToTypedList(methodInvocation.typeArguments(), Type.class);
		ListRewrite typeArgumentsRewrite = astRewrite.getListRewrite(ref,
				ExpressionMethodReference.TYPE_ARGUMENTS_PROPERTY);
		typeArguments.forEach(typeArgument -> typeArgumentsRewrite.insertLast(typeArgument, null));
	}

	/**
	 * Inserts the existing type arguments to the method reference.
	 * 
	 * @param methodInvocation
	 *            original method invocation with possibly nonempty list of type
	 *            arguments
	 * @param ref
	 *            the new {@link TypeMethodReference}
	 */
	private void saveTypeArguments(MethodInvocation methodInvocation, TypeMethodReference ref) {
		List<Type> typeArguments = ASTNodeUtil.convertToTypedList(methodInvocation.typeArguments(), Type.class);
		ListRewrite typeArgumentsRewrite = astRewrite.getListRewrite(ref,
				TypeMethodReference.TYPE_ARGUMENTS_PROPERTY);
		typeArguments.forEach(typeArgument -> typeArgumentsRewrite.insertLast(typeArgument, null));
	}

	/**
	 * Finds the type of the expression represented by the given node, by
	 * resolving its {@link ITypeBinding} and extracting the simple name out of
	 * it. If the type is a parameterized type, then its erasure is returned. If
	 * the type is a capture, then its upper-bound is returned. Otherwise, the
	 * name of the type binding is returned.
	 * 
	 * @param expression
	 * @return a string representing the simple name of the type of the
	 *         expression or an empty string otherwise.
	 */
	private String findTypeOfSimpleName(SimpleName expression) {
		String typeNameStr;
		ITypeBinding binding = expression.resolveTypeBinding();

		if (binding == null) {
			return ""; //$NON-NLS-1$
		}

		if (binding.isArray()) {
			// see SIM-1453
			return ""; //$NON-NLS-1$
		}

		if (binding.isCapture()) {
			Optional<ITypeBinding> optBinding = Arrays.asList(binding.getTypeBounds())
				.stream()
				.findFirst()
				.map(ITypeBinding::getErasure);
			if (!optBinding.isPresent()) {
				return ""; //$NON-NLS-1$
			}
			binding = optBinding.get();
		}

		if (binding.isMember() && !ASTNodeUtil.enclosedInSameType(expression, binding)) {

			ITypeBinding declaringClass = binding.getDeclaringClass();
			ITypeBinding declaringClassErasure = declaringClass.getErasure();
			String outerTypeName = declaringClassErasure.getName();
			String qualifiedName = binding.getErasure()
				.getQualifiedName();
			int outerTypeStartingIndex = qualifiedName.lastIndexOf(outerTypeName);
			typeNameStr = StringUtils.substring(qualifiedName, outerTypeStartingIndex);
			newImports.add(declaringClassErasure.getQualifiedName());
		} else if (qualifiedNameNeeded(binding)) {
			typeNameStr = binding.getErasure()
				.getQualifiedName();
		} else {
			typeNameStr = binding.getErasure()
				.getName();
			newImports.add(binding.getErasure()
				.getQualifiedName());
		}

		return typeNameStr;
	}

	private boolean qualifiedNameNeeded(ITypeBinding binding) {
		ITypeBinding typeBidning = binding.getErasure() != null ? binding.getErasure() : binding;
		String bindingName = typeBidning.getName();
		String bindingQualifiedName = typeBidning.getQualifiedName();
		CompilationUnit compilationUnit = super.getCompilationUnit();
		return ASTNodeUtil.returnTypedList(compilationUnit.imports(), ImportDeclaration.class)
			.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.anyMatch(name -> {
				QualifiedName qualifiedImportName = (QualifiedName) name;
				return qualifiedImportName.getName()
					.getIdentifier()
					.equals(bindingName) && !bindingQualifiedName.equals(qualifiedImportName.toString());
			});
	}

	/**
	 * Checks whether the body of the lambda expression is a {@link Expression}
	 * or a {@link Block} consisting of a single {@link ExpressionStatement},
	 * and if yes extracts the expression out of the it.
	 * 
	 * @param lambdaExpressionNode
	 *            a node representing a lambda expression.
	 * 
	 * @return an {@link Expression} if the body consists of a single
	 *         expression, or {@code null} if the body is not a single
	 *         expression.
	 */
	private Optional<Expression> extractSingleBodyExpression(LambdaExpression lambdaExpressionNode) {
		ASTNode body = lambdaExpressionNode.getBody();

		if (ASTNode.BLOCK == body.getNodeType()) {
			Block block = (Block) body;
			return ASTNodeUtil.findSingleBlockStatement(block, ExpressionStatement.class)
				.map(ExpressionStatement::getExpression);
		}
		return Optional.of(body)
			.filter(Expression.class::isInstance)
			.map(Expression.class::cast);
	}

	/**
	 * compares the lambda parameter names with the method argument names one by
	 * one
	 * 
	 * @param lambdaParams
	 *            list of lambda parameters with type
	 *            {@link VariableDeclaration}
	 * @param methodArgs
	 *            list of method arguments with type {@link Expression}
	 * @return true, if the parameters have the same name in the same order,
	 *         false otherwise
	 */
	private boolean checkMethodParameters(List<VariableDeclaration> lambdaParams, List<Expression> methodArgs) {

		boolean paramsEqual = true;
		for (int i = 0; i < lambdaParams.size(); i++) {
			Expression methodArgument = methodArgs.get(i);
			VariableDeclaration lambdaParam = lambdaParams.get(i);

			// method argument has to be a SimpleName, not an Expression
			if (ASTNode.SIMPLE_NAME == methodArgument.getNodeType()) {
				String methodArgumentName = ((SimpleName) methodArgument).getIdentifier();
				String lambdaParamName = lambdaParam.getName()
					.getIdentifier();

				if (!methodArgumentName.equals(lambdaParamName)) {
					paramsEqual = false;
					break;
				}
			} else {
				paramsEqual = false;
			}
		}

		return paramsEqual;
	}

	private boolean containsName(List<Expression> list, String name) {
		return list.stream()
			.filter(element -> element instanceof Name)
			.anyMatch(nameIter -> name.equals(((Name) nameIter).getFullyQualifiedName()));
	}

	/**
	 * The solution for case 4 is extracted in this method.
	 * <p>
	 * case 4: reference to instance method of arbitrary type i.e.
	 * 
	 * <pre>
	 * {@code Arrays.sort(stringArray, (a, b) -> a.compareToIgnoreCase(b))} 
	 * 
	 * becomes
	 * 
	 * {@code Arrays.sort(stringArray, String::compareToIgnoreCase)}
	 * </pre>
	 * 
	 * @param lambdaExpressionNode
	 *            the lambda to be replaced by the method reference
	 * @param lambdaParams
	 *            a list of parameters of the lambda expression
	 * @param methodInvocation
	 *            the method invocation occurring as the single expression in
	 *            the lambda body
	 * @param methodArguments
	 *            the arguments of the method invocation in the lambda body.
	 */
	private void replaceWithExpressionMethodReference(LambdaExpression lambdaExpressionNode,
			List<VariableDeclaration> lambdaParams, MethodInvocation methodInvocation,
			List<Expression> methodArguments) {

		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (ASTNode.SIMPLE_NAME != methodInvocationExpression.getNodeType()) {
			return;
		}

		SimpleName methodInvocationExpressionName = (SimpleName) methodInvocationExpression;
		String methodInvocationExpressionNameStr = methodInvocationExpressionName.getIdentifier();
		String lambdaParamNameStr = lambdaParams.get(0)
			.getName()
			.getIdentifier();

		if (!methodInvocationExpressionNameStr.equals(lambdaParamNameStr) || !checkMethodParameters(
				lambdaParams.subList(1, lambdaParams.size()), methodArguments)) {
			return;
		}

		List<ITypeBinding> ambTypes = lambdaParams.stream()
			.map(VariableDeclaration::resolveBinding)
			.map(IVariableBinding::getType)
			.collect(Collectors.toList());
		if (isAmbiguousMethodReference(methodInvocation, ambTypes)) {
			return;
		}

		AST ast = astRewrite.getAST();
		Type type;
		Type explicitParameterType = LambdaNodeUtil.findExplicitLambdaParameterType(
				lambdaParams.get(0))
			.orElse(null);
		if (explicitParameterType != null) {
			type = (Type) astRewrite.createCopyTarget(explicitParameterType);
		} else {
			String typeNameStr = findTypeOfSimpleName(methodInvocationExpressionName);
			if (typeNameStr == null || StringUtils.isEmpty(typeNameStr)) {
				return;
			}
			Name typeName = ast.newName(typeNameStr);
			type = ast.newSimpleType(typeName);

		}

		SimpleName methodName = (SimpleName) astRewrite
			.createCopyTarget(methodInvocation.getName());

		TypeMethodReference ref = ast.newTypeMethodReference();
		saveTypeArguments(methodInvocation, ref);
		ref.setType(type);
		ref.setName(methodName);

		astRewrite.replace(lambdaExpressionNode, ref, null);
		getCommentRewriter().saveCommentsInParentStatement(lambdaExpressionNode);
		onRewrite();
		Type representingType = explicitParameterType != null ? explicitParameterType : type;
		addMarkerEvent(lambdaExpressionNode, representingType, methodInvocation.getName());

	}

	private boolean areIncompatibleFunctionalInterfaces(IMethodBinding contextFunctionalInterface,
			IMethodBinding actualFunctionalInterface) {
		ITypeBinding contextReturnType = contextFunctionalInterface.getReturnType();
		ITypeBinding actualReturnType = actualFunctionalInterface.getReturnType();
		if (!actualReturnType.isAssignmentCompatible(contextReturnType)) {
			return true;
		}
		ITypeBinding[] actualParameters = actualFunctionalInterface.getParameterTypes();
		ITypeBinding[] expectedParameters = contextFunctionalInterface.getParameterTypes();
		if (actualParameters.length != expectedParameters.length) {
			return true;
		}
		for (int i = 0; i < actualParameters.length; i++) {
			ITypeBinding actualParameter = actualParameters[i];
			ITypeBinding expectedParameter = expectedParameters[i];
			if (!expectedParameter.isAssignmentCompatible(actualParameter)) {
				return true;
			}
		}
		return false;
	}

}
