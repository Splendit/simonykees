package eu.jsparrow.core.visitor.impl.map.iterate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class ASTNodesCollectorVisitor<T extends ASTNode> extends ASTVisitor {

	private final Class<T> nodeType;
	private final List<T> nodeList = new ArrayList<>();
	private final Predicate<T> predicate;

	public static <T extends ASTNode> T findUniqueNode(ASTNode astNode, Class<T> nodeType,
			Predicate<T> predicate) {

		ASTNodesCollectorVisitor<T> simpleNamesCollectorVisitor = new ASTNodesCollectorVisitor<>(
				nodeType, predicate);

		astNode.accept(simpleNamesCollectorVisitor);
		List<T> nodeList = simpleNamesCollectorVisitor.getNodeList();
		assertEquals(1, nodeList.size());
		return nodeList.get(0);
	}

	public ASTNodesCollectorVisitor(Class<T> nodeType, Predicate<T> predicate) {
		this.nodeType = nodeType;
		this.predicate = predicate;
	}

	@Override
	public void preVisit(ASTNode node) {
		findNodeToAdd(node).ifPresent(nodeList::add);
	}

	private Optional<T> findNodeToAdd(ASTNode node) {
		return Optional.of(node)
			.filter(nodeType::isInstance)
			.map(nodeType::cast)
			.filter(predicate);
	}

	public List<T> getNodeList() {
		return nodeList;
	}
}
