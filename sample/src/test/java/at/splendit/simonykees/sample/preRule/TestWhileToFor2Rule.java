package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestWhileToFor2Rule {
	
	public void testWhileToFor5() {
		List<String> l = new ArrayList<>();

		Iterator<String> iterator = l.iterator();
		String s;
		while (iterator.hasNext()) {
			Object k;
			s = iterator.next();
			System.out.println(s);
		}
	}
	/*
	public void testNextOnlyIterator(){
		List<String> stringList = new ArrayList<>();
		
		Iterator<String> stringIterator = stringList.iterator();
		String s = null;
		while((s = stringIterator.next()) != null){
			System.out.println(s);
		}
	}*/
}
