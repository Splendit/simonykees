package at.splendit.simonykees.core.rule;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import at.splendit.simonykees.core.rule.impl.ArithmethicAssignmentRule;
import at.splendit.simonykees.core.rule.impl.BracketsToControlRule;
import at.splendit.simonykees.core.rule.impl.CodeFormatterRule;
import at.splendit.simonykees.core.rule.impl.CollectionRemoveAllRule;
import at.splendit.simonykees.core.rule.impl.DiamondOperatorRule;
import at.splendit.simonykees.core.rule.impl.ForToForEachRule;
import at.splendit.simonykees.core.rule.impl.FunctionalInterfaceRule;
import at.splendit.simonykees.core.rule.impl.InefficientConstructorRule;
import at.splendit.simonykees.core.rule.impl.MultiCatchRule;
import at.splendit.simonykees.core.rule.impl.OrganiseImportsRule;
import at.splendit.simonykees.core.rule.impl.OverrideAnnotationRule;
import at.splendit.simonykees.core.rule.impl.PrimitiveBoxedForStringRule;
import at.splendit.simonykees.core.rule.impl.RearrangeClassMembersRule;
import at.splendit.simonykees.core.rule.impl.RemoveNewStringConstructorRule;
import at.splendit.simonykees.core.rule.impl.RemoveToStringOnStringRule;
import at.splendit.simonykees.core.rule.impl.SerialVersionUidRule;
import at.splendit.simonykees.core.rule.impl.StringConcatToPlusRule;
import at.splendit.simonykees.core.rule.impl.StringFormatLineSeparatorRule;
import at.splendit.simonykees.core.rule.impl.StringUtilsRule;
import at.splendit.simonykees.core.rule.impl.TryWithResourceRule;
import at.splendit.simonykees.core.rule.impl.WhileToForEachRule;

@SuppressWarnings("nls")
public enum Tag {

	LOOP("LOOP"), JAVA_1_1("1.1", "1"), JAVA_1_2("1.2", "2"), JAVA_1_3("1.3", "3"), JAVA_1_4("1.4",
			"4"), JAVA_1_5("1.5", "5"), JAVA_1_6("1.6", "6"), JAVA_1_7("1.7", "7"), JAVA_1_8("1.8", "8"), EMPTY();
	// not yet arrived! JAVA_1_9("1.5","5");

	private List<String> tagName;

	private Tag(String... tagName) {
		this.tagName = Arrays.asList(tagName);
	}

	public List<String> getTagNames() {
		return tagName;
	}
	

	@SuppressWarnings("rawtypes")
	public static List<Tag> getTagsForRule(Class<? extends RefactoringRule> clazz) {

		if (ArithmethicAssignmentRule.class == clazz) {
			return Arrays.asList(JAVA_1_4);
		} else if (BracketsToControlRule.class == clazz) {
			return Arrays.asList(JAVA_1_1);
		} else if (CodeFormatterRule.class == clazz) {
			return Arrays.asList(JAVA_1_1);
		} else if (CollectionRemoveAllRule.class == clazz) {
			return Arrays.asList(JAVA_1_2);
		} else if (DiamondOperatorRule.class == clazz) {
			return Arrays.asList(JAVA_1_7);
		} else if (ForToForEachRule.class == clazz) {
			return Arrays.asList(JAVA_1_5,LOOP);
		} else if (FunctionalInterfaceRule.class == clazz) {
			return Arrays.asList(JAVA_1_8);
		} else if (InefficientConstructorRule.class == clazz) {
			return Arrays.asList(JAVA_1_5);
		} else if (MultiCatchRule.class == clazz) {
			return Arrays.asList(JAVA_1_7);
		} else if (OrganiseImportsRule.class == clazz) {
			return Arrays.asList(JAVA_1_1);
		} else if (OverrideAnnotationRule.class == clazz) {
			return Arrays.asList(JAVA_1_6);
		} else if (PrimitiveBoxedForStringRule.class == clazz) {
			return Arrays.asList(JAVA_1_1);
		} else if (RearrangeClassMembersRule.class == clazz) {
			return Arrays.asList(JAVA_1_1);
		} else if (RemoveNewStringConstructorRule.class == clazz) {
			return Arrays.asList(JAVA_1_1);
		} else if (RemoveToStringOnStringRule.class == clazz) {
			return Arrays.asList(JAVA_1_1);
		} else if (SerialVersionUidRule.class == clazz) {
			return Arrays.asList(JAVA_1_1);
		} else if (StringConcatToPlusRule.class == clazz) {
			return Arrays.asList(JAVA_1_1);
		} else if (StringFormatLineSeparatorRule.class == clazz) {
			return Arrays.asList(JAVA_1_5);
		} else if (StringUtilsRule.class == clazz) {
			return Arrays.asList(JAVA_1_1);
		} else if (TryWithResourceRule.class == clazz) {
			return Arrays.asList(JAVA_1_7);
		} else if (WhileToForEachRule.class == clazz) {
			return Arrays.asList(JAVA_1_5,LOOP);
		}

		throw new NoSuchElementException("Class:[" + clazz.getName() + "] has no tags defined. Fix this in:["
				+ Tag.class.getCanonicalName() + "]");
	}
	
	public static Tag getTageForName(String name) {
		return Arrays.stream(Tag.class.getEnumConstants()).filter(tag -> tag.name().equals(name)).findFirst().orElse(null);
	}

	public String[] getAllTags() {
		List<String> allTagsList = Arrays.stream(Tag.class.getEnumConstants()).map(t -> t.getTagNames())
				.flatMap(List::stream).collect(Collectors.toList());
		return allTagsList.stream().toArray(String[]::new);
	}

}
