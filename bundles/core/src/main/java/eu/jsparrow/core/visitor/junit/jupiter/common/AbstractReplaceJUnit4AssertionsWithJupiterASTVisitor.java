package eu.jsparrow.core.visitor.junit.jupiter.common;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public abstract class AbstractReplaceJUnit4AssertionsWithJupiterASTVisitor extends AbstractAddImportASTVisitor {

	protected static final String ORG_JUNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	protected static final String ORG_JUNIT_JUPITER_API_ASSERTIONS_PREFIX = ORG_JUNIT_JUPITER_API_ASSERTIONS + "."; //$NON-NLS-1$

	@SuppressWarnings({ "unchecked" })
	protected MethodInvocation createNewInvocationWithoutQualifier(String newMethodName,
			List<Expression> arguments) {
		AST ast = astRewrite.getAST();
		MethodInvocation newInvocation = ast.newMethodInvocation();
		newInvocation.setName(ast.newSimpleName(newMethodName));
		List<Expression> newInvocationArguments = newInvocation.arguments();
		arguments.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
			.forEach(newInvocationArguments::add);
		return newInvocation;
	}

	protected MethodInvocation createNewInvocationWithAssertionsQualifier(MethodInvocation contextForImport,
			String newMethodName, List<Expression> arguments) {
		MethodInvocation newInvocation = createNewInvocationWithoutQualifier(newMethodName, arguments);
		Name newQualifier = addImport(ORG_JUNIT_JUPITER_API_ASSERTIONS, contextForImport);
		newInvocation.setExpression(newQualifier);
		return newInvocation;
	}
}
