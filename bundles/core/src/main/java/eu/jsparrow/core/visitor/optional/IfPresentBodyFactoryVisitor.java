package eu.jsparrow.core.visitor.optional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * A visitor for replacing the {@link Optional#get()} method invocation with the parameter
 * of the lambda expression of {@link Optional#ifPresent(Consumer)}.
 * 
 * @since 2.6
 *
 */
public class IfPresentBodyFactoryVisitor extends ASTVisitor {

	private List<MethodInvocation> getInvocations;
	private String parameterName;
	private ASTRewrite astRewrite;

	public IfPresentBodyFactoryVisitor(List<MethodInvocation> getInvocations, String parameterName,
			ASTRewrite astRewrite) {
		this.getInvocations = getInvocations;
		this.parameterName = parameterName;
		this.astRewrite = astRewrite;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (!getInvocations.contains(methodInvocation)) {
			return true;
		}

		/*
		 * replace the method invocation with the parameter of the lambda
		 * expression of Optional.ifPresent
		 */
		AST ast = astRewrite.getAST();
		SimpleName simpleName = ast.newSimpleName(parameterName);
		astRewrite.replace(methodInvocation, simpleName, null);

		return true;
	}
}
