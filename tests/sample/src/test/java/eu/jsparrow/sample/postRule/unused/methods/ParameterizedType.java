package eu.jsparrow.sample.postRule.unused.methods;

import java.util.ArrayList;
import java.util.List;

public class ParameterizedType<T> {

	private List<T> values = new ArrayList<>();

	public void add(T value) {
		values.add(value);
	}

	public T foo() {
		return null;
	}
}
