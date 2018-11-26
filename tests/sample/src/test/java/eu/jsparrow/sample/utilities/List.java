package eu.jsparrow.sample.utilities;

import java.util.ArrayList;

public class List<T> {
	
	private java.util.List<T> list = new ArrayList<>();
	
	public List() {
		
	}
	
	public List(java.util.List<T> list) {
		this.list = list;
	}
	
	public void add(T t) {
		list.add(t);
	}
	
	public T get(int index) {
		return list.get(index);
	}
	
	public int size() {
		return list.size();
	}
}
