package eu.jsparrow.core.visitor.unused;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class ExpressionWithoutSideEffect {
	
	static boolean hasNoSideEffects(VariableDeclarationFragment fragment) {
		Expression initializer = fragment.getInitializer();
		if(initializer == null) {
			return true;
		}
		int initializerNodeType = initializer.getNodeType();
		
		if(initializerNodeType == ASTNode.CLASS_INSTANCE_CREATION) {
			/*
			 * TODO: define a list of types we can tolerate. E.g. new ArrayList(), new LinkedList(), new HashMap, new HashSet, new Object, new String, 
			 */
		} else if (initializerNodeType == ASTNode.METHOD_INVOCATION) {
			/*
			 * TODO: define a list of method invocations we can tolerate. E.g. Collections.emptyList(), etc
			 */
		} else if (initializerNodeType == ASTNode.ARRAY_CREATION) {
			/*
			 * TODO: make sure the literal consists of only literals or instance creations/method invocations that we can tolerate
			 */
		}

		
		return initializerNodeType == ASTNode.NULL_LITERAL
				|| initializerNodeType == ASTNode.NUMBER_LITERAL
				|| initializerNodeType == ASTNode.STRING_LITERAL
				|| initializerNodeType == ASTNode.CHARACTER_LITERAL
				|| initializerNodeType == ASTNode.BOOLEAN_LITERAL
				|| initializerNodeType == ASTNode.TYPE_LITERAL
				|| initializerNodeType == ASTNode.SIMPLE_NAME
				|| initializerNodeType == ASTNode.FIELD_ACCESS;
	}

}
