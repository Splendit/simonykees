package at.splendit.simonykees.sample.postRule.whileToFor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestWhileToFor2Rule {

	public void testWhileToFor5() {
		List<String> l = new ArrayList<>();

		for (String s:l){
			System.out.println(s);
		}
	}
	/*
	public void testNextOnlyIterator(){
		List<String> stringList = new ArrayList<>();
		
		for(String s : stringList){
			System.out.println(s);
		}
	}*/
}
