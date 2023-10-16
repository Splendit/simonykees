package eu.jsparrow.core.visitor.impl.map;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class ItemAtIndex {
	/**
	 * TODO: move this method to {@link ASTNodeUtil} and delete
	 * {@link ItemAtIndex}
	 *
	 * This method could be used for example for
	 * {@link ASTNodeUtil#findSubsequentStatementInBlock(org.eclipse.jdt.core.dom.Statement, Class)}
	 */
	public static <T extends ASTNode> Optional<T> findItemAtIndex(@SuppressWarnings("rawtypes") List list, int index,
			Class<T> itemType) {
		if (index < 0) {
			return Optional.empty();
		}

		if (index >= list.size()) {
			return Optional.empty();
		}

		Object item = list.get(index);
		return ASTNodeUtil.castToOptional(item, itemType);
	}

}
