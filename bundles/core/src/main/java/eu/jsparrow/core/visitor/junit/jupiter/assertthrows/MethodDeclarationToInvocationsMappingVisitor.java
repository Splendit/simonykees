package eu.jsparrow.core.visitor.junit.jupiter.assertthrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Creates a mapping of {@link MethodDeclaration} as key and a list of all
 * {@link MethodInvocation}- nodes as value which are carried out in the given
 * {@link MethodDeclaration}.
 * 
 * 
 * @since 3.29.0
 *
 */
public class MethodDeclarationToInvocationsMappingVisitor extends ASTVisitor {

	private final Map<MethodDeclaration, List<MethodInvocation>> methodDeclarationToInvocationsMap = new HashMap<>();

	@Override
	public boolean visit(MethodInvocation node) {
		MethodDeclaration surroundingMethod = findSurroundingMethod(node);
		methodDeclarationToInvocationsMap.computeIfAbsent(surroundingMethod, key -> new ArrayList<>());
		methodDeclarationToInvocationsMap.get(surroundingMethod)
			.add(node);
		return true;
	}

	private MethodDeclaration findSurroundingMethod(MethodInvocation methodInvocation) {

		BodyDeclaration bodyDeclarationAncestor = ASTNodeUtil.getSpecificAncestor(methodInvocation,
				BodyDeclaration.class);
		ASTNode parent = methodInvocation.getParent();
		while (parent != null) {
			if (parent == bodyDeclarationAncestor) {
				if (parent.getNodeType() == ASTNode.METHOD_DECLARATION) {
					return (MethodDeclaration) parent;
				}
				return null;
			}
			if (parent.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
				return null;
			}
			parent = parent.getParent();
		}
		return null;
	}
}