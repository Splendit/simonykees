package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Container for all structural data belonging to a method invocation:
 * <ul>
 * <li>method name</li>
 * <li>optional type arguments</li>
 * <li>optional arguments</li>
 * <li>an optional method invocation expression</li>
 * </ul>
 * 
 * @since 4.7.0
 */
public class MethodInvocationData {

	private final String methodName;
	private List<Type> typeArguments;
	private List<Expression> arguments;
	private Expression expression;

	/**
	 * Creates an instance of {@link MethodInvocationData} storing all necessary
	 * structural data needed for an AssertJ assertThat invocation:
	 * <ul>
	 * <li>method name</li>
	 * <li>one argument</li>
	 * <li>an optional method invocation expression</li>
	 * </ul>
	 */
	static MethodInvocationData createNewAssertThatData(MethodInvocation oldAssertThat,
			Expression newAssertThatArgument) {
		String newAssertThaIdentifier = oldAssertThat.getName()
			.getIdentifier();
		MethodInvocationData newAssertThatData = new MethodInvocationData(newAssertThaIdentifier);
		newAssertThatData.setExpression(oldAssertThat.getExpression());
		List<Expression> newAssertThatArguments = Arrays.asList(newAssertThatArgument);
		newAssertThatData.setArguments(newAssertThatArguments);
		return newAssertThatData;
	}

	public MethodInvocationData(String methodName) {
		this.methodName = methodName;
	}

	public void setTypeArguments(List<Type> typeArguments) {
		this.typeArguments = typeArguments;
	}

	public void setArguments(List<Expression> arguments) {
		this.arguments = arguments;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public String getMethodName() {
		return methodName;
	}

	public List<Type> getTypeArguments() {
		if (typeArguments == null) {
			return Collections.emptyList();
		}
		return typeArguments;
	}

	public List<Expression> getArguments() {
		if (arguments == null) {
			return Collections.emptyList();
		}
		return arguments;
	}

	public Optional<Expression> getExpression() {
		return Optional.ofNullable(expression);
	}

	@SuppressWarnings("unchecked")
	public MethodInvocation createNewMethodInvocation(ASTRewrite astRewrite) {
		AST ast = astRewrite.getAST();
		MethodInvocation newMethodInvocation = ast.newMethodInvocation();
		newMethodInvocation.setName(ast.newSimpleName(getMethodName()));

		List<Type> newTpeArguments = getTypeArguments()
			.stream()
			.map(typeArgument -> (Type) astRewrite.createCopyTarget(typeArgument))
			.collect(Collectors.toList());
		newMethodInvocation.typeArguments()
			.addAll(newTpeArguments);

		List<Expression> newArguments = getArguments()
			.stream()
			.map(argument -> (Expression) astRewrite.createCopyTarget(argument))
			.collect(Collectors.toList());
		newMethodInvocation.arguments()
			.addAll(newArguments);

		getExpression()
			.ifPresent(expression -> newMethodInvocation
				.setExpression((Expression) astRewrite.createCopyTarget(expression)));
		return newMethodInvocation;
	}

}
