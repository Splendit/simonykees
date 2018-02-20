package eu.jsparrow.core.util;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Tags for our rules.
 * <p>
 * Customers can filter rules by tags.
 * 
 * @author Martin Huter
 * @since 1.2
 */
public class TagUtil {

	private TagUtil() {

	}

	/*
	 * IMPORTANT: Tags have to match the labels on the individual rule sites in
	 * Confluence: https://confluence.splendit.loc/display/SIM/Implemented+Rules
	 */
	@SuppressWarnings({ "rawtypes", "nls" })
	public static List<Tag> getTagsForRule(Class<? extends RefactoringRule> clazz) {

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = RulesContainer.getAllRules(false);

		List<Tag> tags = rules.stream()
			.filter(rule -> rule.getClass() == clazz)
			.map(rule -> rule.getRuleDescription()
				.getTags())
			.flatMap(List<Tag>::stream)
			.collect(Collectors.toList());

		if (tags != null && !tags.isEmpty()) {
			return tags;
		}

		throw new NoSuchElementException("Class:[" + clazz.getName() + "] has no tags defined. Fix this in:["
				+ TagUtil.class.getCanonicalName() + "]");
	}

}
