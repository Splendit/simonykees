package eu.jsparrow.core.visitor.impl.map.iterate;

import org.eclipse.jdt.core.dom.EnhancedForStatement;

import eu.jsparrow.core.markers.common.IterateMapEntrySetEvent;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * TODO: discuss whether this rule is reasonable and reliable. This rule is only
 * possible if one can rely on a contract that the order of the keys for a given
 * instance of Map remain always the same, as shown in the following Example:
 * 
 * <pre>
 * // 1
 * List<Integer> keyList = new ArrayList<>();
 * for (Integer key : map.keySet()) {
 * 	keyList.add(key);
 * }
 * 
 * // 2
 * List<Integer> keyList = new ArrayList<>();
 * for (Map.Entry<Integer, String> entry : map.entrySet()) {
 * 	keyList.add(entry.getKey());
 * }
 * 
 * // 3
 * List<Integer> keyList = new ArrayList<>();
 * map.forEach((k, v) -> {
 * 	keyList.add(k);
 * });
 * </pre>
 * 
 * In this example, the order of the elements in keyList should always be
 * guaranteed to be the same.
 * 
 * @since 4.20.0
 */
public class IterateMapForEachASTVisitor extends AbstractASTRewriteASTVisitor
		implements IterateMapEntrySetEvent {

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {

		return true;
	}

}
