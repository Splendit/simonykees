package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
@SuppressWarnings({ "serial", "nls", "unchecked", "rawtypes", "unused" })
public class ImmutableStaticFinalCollectionsRule {
	
	private static final List<String> CONSTANT_LIST = new ArrayList<String>() {
		{
			add("foo");
			add("bar");
		}
	};

	private static final Collection<String> CONSTANT_COLLECTION = new ArrayList<String>() {
		{
			add("foo");
			add("bar");
		}
	};

	private static final Map<String, String> CONSTANT_MAP = new HashMap() {
		{
			put("foo", "bar");
		}
	};

	private static final Set<String> CONSTANT_SET = new HashSet() {
		{
			add("foo");
			add("bar");
		}
	};

	private static final NavigableMap<String, String> CONSTANT_NAV_MAP = new TreeMap<String, String>() {
		{
			put("foo", "bar");
		}
	};

	private static final NavigableSet<String> CONSTANT_NAV_SET = new TreeSet<String>() {
		{
			add("foo");
			add("bar");
		}
	};

	private static final SortedMap<String, String> CONSTANT_SORT_MAP = new TreeMap<String, String>() {
		{
			put("foo", "bar");
		}
	};

	private static final SortedSet<String> CONSTANT_SORT_SET = new TreeSet<String>() {
		{
			add("foo");
			add("bar");
		}
	};

	private static final List<String> CONSTANT_LIST_2 = new LinkedList<String>();

	static {
		CONSTANT_LIST_2.add("foo");
		CONSTANT_LIST_2.add("bar");
		CONSTANT_LIST_2.add(CONSTANT_LIST.get(0));
	}

	private static final List<String> CONSTANT_LIST_3 = new LinkedList<>();

	public void test() {
		CONSTANT_LIST_3.add("foo");
		CONSTANT_LIST_3.add("bar");
	}
}
