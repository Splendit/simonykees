package eu.jsparrow.core.visitor.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class VisitorTestUtil {

	public static List<ASTNode> collectAllDescendants(ASTNode parent) {
		List<ASTNode> descendants = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public void preVisit(ASTNode node) {
				descendants.add(node);
			}
		};
		parent.accept(visitor);
		return descendants;
	}

	static <T extends ASTNode> T findUniqueNode(ASTNode visitedNode, Class<T> nodetype, Predicate<T> nodePredicate) {
		List<T> nodes = VisitorTestUtil.collectAllDescendants(visitedNode)
			.stream()
			.filter(nodetype::isInstance)
			.map(nodetype::cast)
			.filter(nodePredicate)
			.collect(Collectors.toList());
		assertEquals(1, nodes.size());
		return nodes.get(0);
	}

	static <T extends ASTNode> T findUniqueNode(ASTNode visitedNode, Class<T> nodetype) {
		return findUniqueNode(visitedNode, nodetype, node -> true);
	}

	private VisitorTestUtil() {
		// private default constructor hiding implicit public one
	}

}
