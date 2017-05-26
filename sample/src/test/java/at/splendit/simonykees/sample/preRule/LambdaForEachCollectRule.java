package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.List;

public class LambdaForEachCollectRule {
	
	public void convertForEachToCollect(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();
		
		
		objectList.stream().map(o -> o.toString())
		.forEach( oString -> {
			oStrings.add(oString);
		});
	}
	
	public void convertForEachExpressionToCollect(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();
		
		objectList.stream().map(o -> o.toString())
		.forEach((String oString) -> 
			oStrings.add(oString)
		);
	}
}
