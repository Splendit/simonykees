package eu.jsparrow.core.visitor;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.util.ClassRelationUtil;

/**
 * converts lambda expressions to method references of the form
 * {@code <Expression>::<MethodName>}. statement lambdas have to be converted to
 * expression lambdas first, using {@link StatementLambdaToExpressionASTVisitor}
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class LambdaToMethodReferenceASTVisitor extends AbstractAddImportASTVisitor {

	private Set<String> newImports = new HashSet<>();

	@Override
	public void endVisit(CompilationUnit cu) {
		this.addImports.addAll(filterNewImportsByExcludingCurrentPackage(cu, newImports));
		super.endVisit(cu);
	}

	@Override
	public boolean visit(LambdaExpression lambdaExpressionNode) {

		Expression body = extractSingleBodyExpression(lambdaExpressionNode);

		// work only with expression lambdas
		if (body != null) {

			List<VariableDeclaration> lambdaParams = ASTNodeUtil.convertToTypedList(lambdaExpressionNode.parameters(),
					VariableDeclaration.class);

			// only single method invocations are relevant for cases 1, 2 and 3
			if (ASTNode.METHOD_INVOCATION == body.getNodeType()) {
				MethodInvocation methodInvocation = (MethodInvocation) body;
				List<Expression> methodArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
						Expression.class);
				Expression methodInvocationExpression = methodInvocation.getExpression();

				if (methodArguments.size() == lambdaParams.size()
						&& checkMethodParameters(lambdaParams, methodArguments)) {
					this.standardReference(lambdaExpressionNode, methodInvocation, methodInvocationExpression,
							methodArguments);
				}

				else if ((lambdaParams.size() - 1) == methodArguments.size() && methodInvocationExpression != null
						&& ASTNode.SIMPLE_NAME == methodInvocationExpression.getNodeType()) {
					this.referenceToInstanceMethodOfArbitraryType(lambdaExpressionNode,
							(SimpleName) methodInvocationExpression, methodArguments, methodInvocation, lambdaParams);
				}
			} else if (ASTNode.CLASS_INSTANCE_CREATION == body.getNodeType()) {
				this.referenceToClassInstanceCreation(lambdaExpressionNode, (ClassInstanceCreation) body, lambdaParams);
			}
		}

		return true;
	}

	/**
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
	 * 
	 * @param lambdaExpressionNode
	 * @param methodInvocation
	 * @param methodInvocationExpression
	 * @param methodArguments
	 */
	private void standardReference(LambdaExpression lambdaExpressionNode, MethodInvocation methodInvocation,
			Expression methodInvocationExpression, List<Expression> methodArguments) {
		ExpressionMethodReference ref = astRewrite.getAST().newExpressionMethodReference();

		// save type arguments
		saveTypeArguments(methodInvocation, ref);

		boolean isReferenceExpressionSet = false;

		// no expression present -> assume 'this'
		if (methodInvocationExpression == null) {

			/*
			 * Ensure that the lambda expression is enclosed in the same class as the method
			 * which is being referenced. We have to check this, because the method could be
			 * declared in the outer class.
			 */
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			ITypeBinding methodsDeclaringClass = methodBinding.getDeclaringClass();
			AbstractTypeDeclaration lambdaEnclosing = ASTNodeUtil.getSpecificAncestor(lambdaExpressionNode,
					AbstractTypeDeclaration.class);
			ITypeBinding lambdaEnclosingType = lambdaEnclosing.resolveBinding();

			if (Modifier.isStatic(methodBinding.getModifiers())) {
				SimpleName staticClassName = astRewrite.getAST()
						.newSimpleName(methodsDeclaringClass.getErasure().getName());
				ref.setExpression(staticClassName);
				isReferenceExpressionSet = true;
			} else if (ClassRelationUtil.compareITypeBinding(methodsDeclaringClass, lambdaEnclosingType)) {
				ClassInstanceCreation enclosingAnonymousInnerClass = ASTNodeUtil
						.getSpecificAncestor(lambdaExpressionNode, ClassInstanceCreation.class);
				if (enclosingAnonymousInnerClass == null) {
					ThisExpression thisExpression = astRewrite.getAST().newThisExpression();
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
				isReferenceExpressionSet = true;
			}
		}

		if (isReferenceExpressionSet) {
			SimpleName methodName = (SimpleName) astRewrite.createCopyTarget(methodInvocation.getName());
			ref.setName(methodName);
			astRewrite.replace(lambdaExpressionNode, ref, null);
		}
	}

	/**
	 * case 4: reference to instance method of arbitrary type i.e.
	 * 
	 * Arrays.sort(stringArray, (a, b) -> a.compareToIgnoreCase(b))
	 * 
	 * becomes
	 * 
	 * Arrays.sort(stringArray, String::compareToIgnoreCase)
	 * 
	 * @param lambdaExpressionNode
	 * @param methodInvocationExpressionName
	 * @param methodArguments
	 * @param methodInvocation
	 * @param lambdaParams
	 */
	private void referenceToInstanceMethodOfArbitraryType(LambdaExpression lambdaExpressionNode,
			SimpleName methodInvocationExpressionName, List<Expression> methodArguments,
			MethodInvocation methodInvocation, List<VariableDeclaration> lambdaParams) {
		String methodInvocationExpressionNameStr = methodInvocationExpressionName.getIdentifier();
		String lambdaParamNameStr = lambdaParams.get(0).getName().getIdentifier();

		if (methodInvocationExpressionNameStr.equals(lambdaParamNameStr)
				&& checkMethodParameters(lambdaParams.subList(1, lambdaParams.size()), methodArguments)) {

			String typeNameStr = findTypeOfSimpleName(methodInvocationExpressionName);

			if (typeNameStr != null && !typeNameStr.isEmpty()) {

				SimpleName typeName = astRewrite.getAST().newSimpleName(typeNameStr);
				SimpleName methodName = (SimpleName) astRewrite.createCopyTarget(methodInvocation.getName());

				ExpressionMethodReference ref = astRewrite.getAST().newExpressionMethodReference();
				saveTypeArguments(methodInvocation, ref);
				ref.setExpression(typeName);
				ref.setName(methodName);

				astRewrite.replace(lambdaExpressionNode, ref, null);

				/*
				 * SIM-514 bugfix missing import
				 */
				ITypeBinding typeBinding = methodInvocationExpressionName.resolveTypeBinding();
				if (typeBinding != null) {
					String qualifiedName = typeBinding.getErasure().getQualifiedName();
					if (qualifiedName != null && !qualifiedName.equals("")) { //$NON-NLS-1$
						newImports.add(qualifiedName);
					}
				}
			}
		}
	}

	/**
	 * case 5: reference to class instance creation (new) i.e.
	 * 
	 * Set<Person> persSet2 = transferElements(personList, () -> new HashSet<>())
	 * 
	 * becomes
	 * 
	 * Set<Person> persSet3 = transferElements(personList, HashSet<Person>::new)
	 * 
	 * @param lambdaExpressionNode
	 * @param classInstanceCreation
	 * @param lambdaParams
	 */
	private void referenceToClassInstanceCreation(LambdaExpression lambdaExpressionNode,
			ClassInstanceCreation classInstanceCreation, List<VariableDeclaration> lambdaParams) {
		List<Expression> classInstanceCreationArguments = ASTNodeUtil
				.convertToTypedList(classInstanceCreation.arguments(), Expression.class);

		AnonymousClassDeclaration annonymousClass = classInstanceCreation.getAnonymousClassDeclaration();
		if (annonymousClass == null && lambdaParams.size() == classInstanceCreation.arguments().size()
				&& checkMethodParameters(lambdaParams, classInstanceCreationArguments)) {
			Type classInstanceCreationType = classInstanceCreation.getType();

			CreationReference ref = astRewrite.getAST().newCreationReference();
			if (ASTNode.PARAMETERIZED_TYPE == classInstanceCreationType.getNodeType()
					&& ((ParameterizedType) classInstanceCreationType).typeArguments().isEmpty()) {
				ref.setType(
						(Type) astRewrite.createMoveTarget(((ParameterizedType) classInstanceCreationType).getType()));
			} else {
				ref.setType((Type) astRewrite.createCopyTarget(classInstanceCreationType));
			}

			astRewrite.replace(lambdaExpressionNode, ref, null);
		}
	}

	/**
	 * Inserts the existing type arguments to the method reference.
	 * 
	 * @param methodInvocation
	 *            original method invocation with possibly nonempty list of type
	 *            arguments
	 * @param ref
	 *            the new method reference
	 */
	private void saveTypeArguments(MethodInvocation methodInvocation, ExpressionMethodReference ref) {
		List<Type> typeArguments = ASTNodeUtil.convertToTypedList(methodInvocation.typeArguments(), Type.class);
		ListRewrite typeArgumentsRewrite = astRewrite.getListRewrite(ref,
				ExpressionMethodReference.TYPE_ARGUMENTS_PROPERTY);
		typeArguments.forEach(typeArgument -> typeArgumentsRewrite.insertLast(typeArgument, null));
	}

	/**
	 * Finds the type of the expression represented by the given node, by resolving
	 * its {@link ITypeBinding} and extracting the simple name out of it. If the
	 * type is a parameterized type, then its erasure is returned. If the type is a
	 * capture, then its upper-bound is returned. Otherwise, the name of the type
	 * binding is returned.
	 * 
	 * @param expression
	 * @return a string representing the simple name of the type of the expression
	 *         or an empty string otherwise.
	 */
	private String findTypeOfSimpleName(SimpleName expression) {
		String typeNameStr;
		ITypeBinding binding = expression.resolveTypeBinding();
		if (binding.isParameterizedType()) {
			ITypeBinding erasure = binding.getErasure();
			typeNameStr = erasure.getName();
		} else if (binding.isCapture()) {
			typeNameStr = Arrays.asList(binding.getTypeBounds()).stream().findFirst().map(ITypeBinding::getErasure)
					.map(ITypeBinding::getName).orElse(""); //$NON-NLS-1$
		} else {
			typeNameStr = binding.getName();
		}
		return typeNameStr;
	}

	/**
	 * Checks whether the body of the lambda expression is a {@link Expression} or a
	 * {@link Block} consisting of a single {@link ExpressionStatement}, and if yes
	 * extracts the expression out of the it.
	 * 
	 * @param lambdaExpressionNode
	 *            a node representing a lambda expression.
	 * 
	 * @return an {@link Expression} if the body consists of a single expression, or
	 *         {@code null} if the body is not a single expression.
	 */
	private Expression extractSingleBodyExpression(LambdaExpression lambdaExpressionNode) {
		ASTNode body = lambdaExpressionNode.getBody();

		if (ASTNode.BLOCK == body.getNodeType()) {
			Block block = (Block) body;
			List<Statement> statements = ASTNodeUtil.returnTypedList(block.statements(), Statement.class);
			if (statements.size() == 1) {
				Statement singleStatement = statements.get(0);
				if (ASTNode.EXPRESSION_STATEMENT == singleStatement.getNodeType()) {
					return ((ExpressionStatement) singleStatement).getExpression();
				}
			}
		} else if (body instanceof Expression) {
			return (Expression) body;
		}

		return null;
	}

	/**
	 * compares the lambda parameter names with the method argument names one by one
	 * 
	 * @param lambdaParams
	 *            list of lambda parameters with type {@link VariableDeclaration}
	 * @param methodArgs
	 *            list of method arguments with type {@link Expression}
	 * @return true, if the parameters have the same name in the same order, false
	 *         otherwise
	 */
	private boolean checkMethodParameters(List<VariableDeclaration> lambdaParams, List<Expression> methodArgs) {

		boolean paramsEqual = true;
		for (int i = 0; i < lambdaParams.size(); i++) {
			Expression methodArgument = methodArgs.get(i);
			VariableDeclaration lambdaParam = lambdaParams.get(i);

			// method argument has to be a SimpleName, not an Expression
			if (ASTNode.SIMPLE_NAME == methodArgument.getNodeType()) {
				String methodArgumentName = ((SimpleName) methodArgument).getIdentifier();
				String lambdaParamName = lambdaParam.getName().getIdentifier();

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
		return list.stream().filter(element -> element instanceof Name)
				.anyMatch(nameIter -> name.equals(((Name) nameIter).getFullyQualifiedName()));
	}
}
