package at.splendit.simonykees.core.rule;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import at.splendit.simonykees.core.rule.impl.WhileToForEachRule;

@SuppressWarnings("nls")
public enum Tag {
	
	LOOP("LOOP"),
	JAVA_1_5("1.5","5");
	
	private List<String> tagName;
	
	private Tag(String... tagName){
		this.tagName=Arrays.asList(tagName);
	}
	
	public String getTagName(){
		return tagName.stream().collect(Collectors.joining(","));
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Tag> getTagsForRule(Class<? extends RefactoringRule> clazz) {
		if(WhileToForEachRule.class == clazz){
			return Arrays.asList(LOOP,LOOP);
		}
		if(WhileToForEachRule.class == clazz){
			return Arrays.asList();
		}
		
		
		return Arrays.asList(LOOP);
		//throw new RuntimeException();
	}
	
	
	
	
	public String getAllTags(){
		return Arrays.stream(Tag.class.getEnumConstants()).map(Tag::getTagName).collect(Collectors.joining(";"));
	}

}
