package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.map.iterate.IterateMapForEachASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

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
 * @see IterateMapEntrySetASTVisitor
 * 
 * @since 4.20.0
 */
public class IterateMapForEachRule extends RefactoringRuleImpl<IterateMapForEachASTVisitor> {

	public static final String RULE_ID = "IterateMapEntrySet"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	public IterateMapForEachRule() {
		super();
		this.visitorClass = IterateMapForEachASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription("Iterate Map::forEach", ""
				+ "In Java 8 the method 'map.forEach(...)' has been introduced in order to simplify iteration on the key-value entries of a map."
				+ " This rule looks for various iterations on maps, for example for iterations by for-loops, and replaces the given iteration by a "
				+ " 'map.forEach((k,v)->{...})' construct if this is reasonable and possible.",
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.READABILITY));

	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
