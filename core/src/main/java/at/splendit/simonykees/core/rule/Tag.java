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
	
	LOOP("LOOP"),
	JAVA_1_1("1.1","1"),
	JAVA_1_2("1.2","2"),
	JAVA_1_3("1.3","3"),
	JAVA_1_4("1.4","4"),
	JAVA_1_5("1.5","5"),
	JAVA_1_6("1.6","6"),
	JAVA_1_7("1.7","7"),
	JAVA_1_8("1.8","8");
	//not yet arrived! JAVA_1_9("1.5","5");
	
	private List<String> tagName;
	
	private Tag(String... tagName){
		this.tagName=Arrays.asList(tagName);
	}
	
	public String getTagName(){
		return tagName.stream().collect(Collectors.joining(","));
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Tag> getTagsForRule(Class<? extends RefactoringRule> clazz) {
		
		if(ArithmethicAssignmentRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(BracketsToControlRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(CodeFormatterRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(CollectionRemoveAllRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(DiamondOperatorRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(ForToForEachRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(FunctionalInterfaceRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(InefficientConstructorRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(MultiCatchRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(OrganiseImportsRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(OverrideAnnotationRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(PrimitiveBoxedForStringRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(RearrangeClassMembersRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(RemoveNewStringConstructorRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(RemoveToStringOnStringRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(SerialVersionUidRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(StringConcatToPlusRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(StringFormatLineSeparatorRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(StringUtilsRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(TryWithResourceRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		else if(WhileToForEachRule.class == clazz){
			return Arrays.asList(LOOP);
		}
		
		throw new NoSuchElementException("Class:["+clazz.getName()+"] has no tags defined. Fix this in:["+Tag.class.getCanonicalName()+"]");
	}
	
	
	
	
	public String getAllTags(){
		return Arrays.stream(Tag.class.getEnumConstants()).map(Tag::getTagName).collect(Collectors.joining(";"));
	}

}
