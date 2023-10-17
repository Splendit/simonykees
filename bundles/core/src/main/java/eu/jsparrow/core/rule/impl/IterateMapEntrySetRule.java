package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.map.iterate.IterateMapEntrySetASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see IterateMapEntrySetASTVisitor
 * 
 * @since 4.20.0
 */
public class IterateMapEntrySetRule extends RefactoringRuleImpl<IterateMapEntrySetASTVisitor> {

	public static final String RULE_ID = "IterateMapEntrySet"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	public IterateMapEntrySetRule() {
		super();
		this.visitorClass = IterateMapEntrySetASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription("Iterate Map::entrySet", ""
				+ "According to the rule S2864 on the web site 'sonarcloud.io',"
				+ " the performance is can be improved by iterating the entry set of a map returned by the method 'entrySet()'"
				+ " in cases where both key and value are needed."
				+ " This rule inspects iterations on key sets of maps."
				+ " As soon as an invocation of the 'get(Object key)' - method of the map is found,"
				+ " the iteration is transformed to one which uses the entry set instead of the key set.",
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_5, Tag.PERFORMANCE));

	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

}
