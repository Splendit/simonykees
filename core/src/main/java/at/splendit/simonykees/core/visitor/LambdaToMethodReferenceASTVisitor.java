package at.splendit.simonykees.core.visitor;

import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import at.splendit.simonykees.core.util.ASTNodeUtil;

/**
 * converts lambda expressions to method references of the form
 * {@code <Expression>::<MethodName>}. statement lambdas have to be converted to
 * expression lambdas first, using {@link StatementLambdaToExpressionASTVisitor}
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class LambdaToMethodReferenceASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(LambdaExpression lambdaExpressionNode) {

		// work only with expression lambdas
		if (lambdaExpressionNode.getBody() instanceof Expression) {
			Expression expression = (Expression) lambdaExpressionNode.getBody();
			List<VariableDeclaration> lambdaParams = ASTNodeUtil.convertToTypedList(lambdaExpressionNode.parameters(),
					VariableDeclaration.class);

			// only single method invocations are relevant for cases 1, 2 and 3
			if (expression instanceof MethodInvocation) {
				MethodInvocation methodInvocation = (MethodInvocation) expression;
				List<Expression> methodArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
						Expression.class);
				Expression methodInvocationExpression = methodInvocation.getExpression();

				/*
				 * case 1: reference to static method
				 * 
				 * case 2: reference to instance method i.e.
				 * 
				 * personList.forEach(element -> System.out.println(element));
				 * 
				 * becomes
				 * 
				 * personList.forEach(System.out::println);
				 * 
				 * case 3: reference to 'this' i.e.
				 * 
				 * personList.forEach(person -> doSomething(person));
				 * 
				 * becomes
				 * 
				 * personList.forEach(this::doSomething);
				 */
				if (methodArguments.size() == lambdaParams.size()
						&& checkMethodParameters(lambdaParams, methodArguments)) {

					SimpleName methodName = (SimpleName) astRewrite.createCopyTarget(methodInvocation.getName());

					ExpressionMethodReference ref = astRewrite.getAST().newExpressionMethodReference();
					ref.setName(methodName);

					boolean isReferenceExpressionSet = false;

					// simple name, qualified name or 'this'
					if (methodInvocationExpression instanceof Name
							|| methodInvocationExpression instanceof ThisExpression) {

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
					// no expression present -> assume 'this'
					else if (methodInvocationExpression == null) {

						IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
						if (!Modifier.isStatic(methodBinding.getModifiers())) {
							ThisExpression thisExpression = astRewrite.getAST().newThisExpression();
							ref.setExpression(thisExpression);
							isReferenceExpressionSet = true;
						} else {
							SimpleName staticClassName = astRewrite.getAST()
									.newSimpleName(methodBinding.getDeclaringClass().getErasure().getName());
							ref.setExpression(staticClassName);
							isReferenceExpressionSet = true;
						}

					}

					if (isReferenceExpressionSet) {
						astRewrite.replace(lambdaExpressionNode, ref, null);
					}
				}

				/*
				 * case 4: reference to instance method of arbitrary type i.e.
				 * 
				 * Arrays.sort(stringArray, (a, b) -> a.compareToIgnoreCase(b));
				 * 
				 * becomes
				 * 
				 * Arrays.sort(stringArray, String::compareToIgnoreCase);
				 */
				else if ((lambdaParams.size() - 1) == methodArguments.size() && methodInvocationExpression != null) {

					if (methodInvocationExpression instanceof SimpleName) {
						SimpleName methodInvocationExpressionName = (SimpleName) methodInvocationExpression;
						String methodInvocationExpressionNameStr = methodInvocationExpressionName.getIdentifier();
						String lambdaParamNameStr = lambdaParams.get(0).getName().getIdentifier();

						if (methodInvocationExpressionNameStr.equals(lambdaParamNameStr) && checkMethodParameters(
								lambdaParams.subList(1, lambdaParams.size()), methodArguments)) {

							ITypeBinding binding = methodInvocationExpressionName.resolveTypeBinding();
							String typeNameStr;
							if (binding.isParameterizedType()) {
								ITypeBinding erasure = binding.getErasure();
								typeNameStr = erasure.getName();
							} else {
								typeNameStr = binding.getName();
							}

							SimpleName typeName = astRewrite.getAST().newSimpleName(typeNameStr);
							SimpleName methodName = (SimpleName) astRewrite
									.createCopyTarget(methodInvocation.getName());

							ExpressionMethodReference ref = astRewrite.getAST().newExpressionMethodReference();
							ref.setExpression(typeName);
							ref.setName(methodName);

							astRewrite.replace(lambdaExpressionNode, ref, null);
						}
					}
				}
			}

			/*
			 * case 5: reference to class instance creation (new) i.e.
			 * 
			 * Set<Person> persSet2 = transferElements(personList, () -> new
			 * HashSet<>());
			 * 
			 * becomes
			 * 
			 * Set<Person> persSet3 = transferElements(personList,
			 * HashSet<Person>::new);
			 */
			else if (expression instanceof ClassInstanceCreation) {
				ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;

				if (lambdaParams.size() == classInstanceCreation.arguments().size()) {
					Type classInstanceCreationType = classInstanceCreation.getType();

					CreationReference ref = astRewrite.getAST().newCreationReference();

					if (classInstanceCreationType instanceof ParameterizedType
							&& ((ParameterizedType) classInstanceCreationType).typeArguments().size() == 0) {
						ref.setType((Type) astRewrite
								.createMoveTarget(((ParameterizedType) classInstanceCreationType).getType()));
					} else {
						ref.setType((Type) astRewrite.createCopyTarget(classInstanceCreationType));
					}

					astRewrite.replace(lambdaExpressionNode, ref, null);
				}
			}
		}

		return true;
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
			if (methodArgument instanceof SimpleName) {
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
		boolean retVal = false;

		for (Expression element : list) {
			if (element instanceof Name) {
				String elementName = ((Name) element).getFullyQualifiedName();
				if (name.equals(elementName)) {
					retVal = true;
					break;
				}
			}
		}

		return retVal;
	}
}
