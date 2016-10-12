package at.splendit.simonykees.core.builder;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

/** Helper Class to generate new ASTNodes
 * 
 * @author Martin Huter
 *
 */
public class NodeBuilder {

	@Deprecated
	@SuppressWarnings("unchecked")
	public static MethodInvocation newMethodInvocation(AST ast, Expression optinoalExpression, SimpleName name,
			Expression argument) {
		MethodInvocation resultMI = ast.newMethodInvocation();
		resultMI.setExpression(optinoalExpression);
		resultMI.setName(name);
		resultMI.arguments().add(argument);
		return resultMI;
	}
	
	@Deprecated
	@SuppressWarnings("unchecked")
	public static MethodInvocation newMethodInvocation(AST ast, Expression optinoalExpression, SimpleName name,
			List<Expression> arguments) {
		MethodInvocation resultMI = ast.newMethodInvocation();
		resultMI.setExpression(optinoalExpression);
		resultMI.setName(name);
		resultMI.arguments().addAll(arguments);
		return resultMI;
	}

}
