package at.splendit.simonykees.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.Tag;
import at.splendit.simonykees.core.rule.impl.ArithmethicAssignmentRule;
import at.splendit.simonykees.core.rule.impl.BracketsToControlRule;
import at.splendit.simonykees.core.rule.impl.CodeFormatterRule;
import at.splendit.simonykees.core.rule.impl.CollectionRemoveAllRule;
import at.splendit.simonykees.core.rule.impl.DiamondOperatorRule;
import at.splendit.simonykees.core.rule.impl.FieldNameConventionRule;
import at.splendit.simonykees.core.rule.impl.ForToForEachRule;
import at.splendit.simonykees.core.rule.impl.FunctionalInterfaceRule;
import at.splendit.simonykees.core.rule.impl.InefficientConstructorRule;
import at.splendit.simonykees.core.rule.impl.LambdaForEachIfWrapperToFilterRule;
import at.splendit.simonykees.core.rule.impl.LambdaToMethodReferenceRule;
import at.splendit.simonykees.core.rule.impl.MultiCatchRule;
import at.splendit.simonykees.core.rule.impl.MultiVariableDeclarationLineRule;
import at.splendit.simonykees.core.rule.impl.OrganiseImportsRule;
import at.splendit.simonykees.core.rule.impl.OverrideAnnotationRule;
import at.splendit.simonykees.core.rule.impl.PrimitiveBoxedForStringRule;
import at.splendit.simonykees.core.rule.impl.RearrangeClassMembersRule;
import at.splendit.simonykees.core.rule.impl.RemoveNewStringConstructorRule;
import at.splendit.simonykees.core.rule.impl.RemoveToStringOnStringRule;
import at.splendit.simonykees.core.rule.impl.SerialVersionUidRule;
import at.splendit.simonykees.core.rule.impl.StatementLambdaToExpressionRule;
import at.splendit.simonykees.core.rule.impl.StringConcatToPlusRule;
import at.splendit.simonykees.core.rule.impl.StringFormatLineSeparatorRule;
import at.splendit.simonykees.core.rule.impl.StringLiteralEqualityCheckRule;
import at.splendit.simonykees.core.rule.impl.StringUtilsRule;
import at.splendit.simonykees.core.rule.impl.TryWithResourceRule;
import at.splendit.simonykees.core.rule.impl.WhileToForEachRule;
import at.splendit.simonykees.core.rule.impl.standardLogger.StandardLoggerRule;

public class TagUtil {

	private TagUtil() {

	}

	@SuppressWarnings({ "rawtypes", "nls" })
	public static List<Tag> getTagsForRule(Class<? extends RefactoringRule> clazz) {

		if (ArithmethicAssignmentRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_4);
		} else if (BracketsToControlRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1, Tag.FORMATTING, Tag.READABILITY);
		} else if (CodeFormatterRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1, Tag.FORMATTING, Tag.READABILITY);
		} else if (CollectionRemoveAllRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_2, Tag.OLD_LANGUAGE_CONSTRUCTS);
		} else if (DiamondOperatorRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_7, Tag.OLD_LANGUAGE_CONSTRUCTS);
		} else if (FieldNameConventionRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_0_9, Tag.READABILITY);
		} else if (ForToForEachRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_5, Tag.LOOP, Tag.OLD_LANGUAGE_CONSTRUCTS);
		} else if (FunctionalInterfaceRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_8, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.LAMBDA);
		} else if (InefficientConstructorRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_5, Tag.PERFORMANCE);
		} else if (LambdaForEachIfWrapperToFilterRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA);
		} else if (LambdaToMethodReferenceRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA);
		} else if (MultiCatchRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_7, Tag.OLD_LANGUAGE_CONSTRUCTS);
		} else if (MultiVariableDeclarationLineRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS);
		} else if (OrganiseImportsRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1, Tag.FORMATTING, Tag.READABILITY);
		} else if (OverrideAnnotationRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_6, Tag.READABILITY);
		} else if (PrimitiveBoxedForStringRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION, Tag.PERFORMANCE);
		} else if (RearrangeClassMembersRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY);
		} else if (RemoveNewStringConstructorRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION, Tag.PERFORMANCE);
		} else if (RemoveToStringOnStringRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION, Tag.PERFORMANCE);
		} else if (SerialVersionUidRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1);
		} else if (StatementLambdaToExpressionRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA);
		} else if (StringConcatToPlusRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION);
		} else if (StringFormatLineSeparatorRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_5, Tag.STRING_MANIPULATION);
		} else if (StringLiteralEqualityCheckRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1);
		} else if (StringUtilsRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION);
		} else if (TryWithResourceRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_7, Tag.OLD_LANGUAGE_CONSTRUCTS);
		} else if (WhileToForEachRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_5, Tag.LOOP, Tag.OLD_LANGUAGE_CONSTRUCTS);
		} else if (StandardLoggerRule.class == clazz) {
			return Arrays.asList(Tag.JAVA_1_1);
		}

		throw new NoSuchElementException("Class:[" + clazz.getName() + "] has no tags defined. Fix this in:["
				+ Tag.class.getCanonicalName() + "]");
	}

}
