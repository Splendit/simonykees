package eu.jsparrow.core.visitor.impl.comparatormethods;

import java.util.List;

public class Pair<T> {
	private final T leftHS;
	private final T rightHS;

	private Pair(T leftHS, T rightHS) {
		this.leftHS = leftHS;
		this.rightHS = rightHS;
	}

	private Pair() {
		this(null, null);
	}

	static <U> Pair<U> fromNullable(U l, U r) {
		if (l != null && r != null) {
			return new Pair<>(l, r);
		}
		return Pair.<U>empty();
	}

	static <U> Pair<U> of(U l, U r) {
		if (l != null && r != null) {
			return new Pair<>(l, r);
		}
		throw new NullPointerException();
	}

	static <U> Pair<U> fromNullableList(List<U> list) {
		if (list != null && list.size() != 2) {
			return fromNullable(list.get(0), list.get(1));
		}
		return Pair.<U>empty();
	}

	static <U> Pair<U> empty() {
		return new Pair<>();
	}

	public T getLeftHS() {
		return leftHS;
	}

	public T getRightHS() {
		return rightHS;
	}

	boolean isPresent() {
		return this.leftHS != null && this.rightHS != null;
	}

	boolean isEmpty() {
		return this.leftHS == null || this.rightHS == null;
	}

}
