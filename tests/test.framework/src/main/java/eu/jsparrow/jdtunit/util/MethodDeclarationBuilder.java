package eu.jsparrow.jdtunit.util;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jface.text.BadLocationException;

import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.jdtunit.JdtUnitFixtureClass;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class for creating method declarations for testing purposes in order
 * to avoid logics inside the unit test cases.
 * <p>
 * The following operations can be carried out for the given method:
 * <ul>
 * <li>Declaration of parameters.</li>
 * <li>Declare the last method parameter as a vararg parameter.</li>
 * <li>...</li>
 * </ul>
 *
 * @since 3.15.0
 */
@SuppressWarnings("nls")
public class MethodDeclarationBuilder {

	private MethodDeclaration methodDeclaration;

	private MethodDeclarationBuilder(JdtUnitFixtureClass fixture, String methodName)
			throws JavaModelException, BadLocationException, JdtUnitException {
		this.methodDeclaration = fixture.addMethod(methodName);
	}

	/**
	 * Add a method declaration to the class represented by the first parameter.
	 * 
	 * @param fixture
	 *            a {@link JdtUnitFixtureClass} representing the class in which
	 *            the method is declared.
	 * @param methodName
	 *            a {@link String} representing the name of the method
	 * @return a {@link MethodDeclarationBuilder}
	 * @throws Exception
	 *             if the method could not be created.
	 */
	public static MethodDeclarationBuilder factory(JdtUnitFixtureClass fixture, String methodName) throws Exception {
		return new MethodDeclarationBuilder(fixture, methodName);
	}

	/**
	 * Add a parameter to the method declaration.
	 * 
	 * 
	 * @param typeName
	 *            represents the type of the parameter which has no generic
	 *            arguments and can also be a primitive type like for example
	 *            {@code int}.
	 * @return the {@code this}-reference to the given
	 *         {@link MethodDeclarationBuilder}
	 */
	public MethodDeclarationBuilder withSimpleTypeParameter(String typeName) {
		AST ast = methodDeclaration.getAST();
		Code primitiveTypeCode = PrimitiveType.toCode(typeName);
		if (primitiveTypeCode != null) {
			return addParameter(ast.newPrimitiveType(primitiveTypeCode));
		}
		SimpleType paramType = ast.newSimpleType(ast.newSimpleName(typeName));
		return addParameter(paramType);
	}

	@SuppressWarnings("unchecked")
	private MethodDeclarationBuilder addParameter(Type paramType) {
		AST ast = methodDeclaration.getAST();
		int index = this.methodDeclaration.parameters()
			.size();
		SimpleName parameName = ast.newSimpleName("param_" + index);
		SingleVariableDeclaration paramDeclaration = NodeBuilder.newSingleVariableDeclaration(ast, parameName,
				paramType);
		methodDeclaration.parameters()
			.add(paramDeclaration);
		return this;
	}

	/**
	 * Add a parameter to the method declaration.
	 * 
	 * @param typeName
	 *            represents the type of the parameter which can have generic
	 *            arguments and is expected to be the simple name of a Java
	 *            type.
	 * @param typeArgumentIdentifiers
	 *            represents the list of type arguments of the parameter type
	 *            which are all expected to represent simple names of Java
	 *            classes.
	 * @return the {@code this}-reference to the given
	 *         {@link MethodDeclarationBuilder}
	 */
	@SuppressWarnings("unchecked")
	public MethodDeclarationBuilder withParameterizedTypeParameter(String typeName, String... typeArgumentIdentifiers) {
		if (typeArgumentIdentifiers.length == 0) {
			return withSimpleTypeParameter(typeName);
		}
		AST ast = methodDeclaration.getAST();
		ParameterizedType parameterizedType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName(typeName)));
		List<Type> typeArguments = parameterizedType.typeArguments();
		Arrays.stream(typeArgumentIdentifiers)
			.map(ast::newSimpleName)
			.map(ast::newSimpleType)
			.forEach(typeArguments::add);
		addParameter(parameterizedType);
		return this;
	}

	/**
	 * Specifies that the last parameter will be a vararg parameter.
	 * 
	 * @return the {@code this}-reference to the given
	 *         {@link MethodDeclarationBuilder}
	 */
	public MethodDeclarationBuilder withVarArgs() {
		List<SingleVariableDeclaration> parameters = ASTNodeUtil.convertToTypedList(methodDeclaration.parameters(),
				SingleVariableDeclaration.class);
		if (parameters.isEmpty()) {
			return this;
		}
		parameters.forEach(param -> param.setVarargs(false));
		SingleVariableDeclaration last = parameters.get(parameters.size() - 1);
		last.setVarargs(true);
		return this;
	}
}
