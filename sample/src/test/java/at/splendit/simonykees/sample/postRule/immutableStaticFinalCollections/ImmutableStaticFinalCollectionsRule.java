package at.splendit.simonykees.sample.postRule.immutableStaticFinalCollections;

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
import java.util.Collections;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
@SuppressWarnings({ "serial", "nls", "unchecked", "rawtypes" })
public class ImmutableStaticFinalCollectionsRule {
	
	public static final List<String> CONSTANT_LIST = Collections.unmodifiableList(new ArrayList<String>() {
		{
			add("foo");
			add("bar");
		}
	});

	public static final Collection<String> CONSTANT_COLLECTION = Collections.unmodifiableCollection(new ArrayList<String>() {
		{
			add("foo");
			add("bar");
		}
	});

	public static final Map<String, String> CONSTANT_MAP = Collections.unmodifiableMap(new HashMap() {
		{
			put("foo", "bar");
		}
	});

	public static final Set<String> CONSTANT_SET = Collections.unmodifiableSet(new HashSet() {
		{
			add("foo");
			add("bar");
		}
	});

	public static final NavigableMap<String, String> CONSTANT_NAV_MAP = Collections.unmodifiableNavigableMap(new TreeMap<String, String>() {
		{
			put("foo", "bar");
		}
	});

	public static final NavigableSet<String> CONSTANT_NAV_SET = Collections.unmodifiableNavigableSet(new TreeSet<String>() {
		{
			add("foo");
			add("bar");
		}
	});

	public static final SortedMap<String, String> CONSTANT_SORT_MAP = Collections.unmodifiableSortedMap(new TreeMap<String, String>() {
		{
			put("foo", "bar");
		}
	});

	public static final SortedSet<String> CONSTANT_SORT_SET = Collections.unmodifiableSortedSet(new TreeSet<>() {
		{
			add("foo");
			add("bar");
		}
	});

	public static final List<String> CONSTANT_LIST_2 = new LinkedList<String>();

	static {
		CONSTANT_LIST_2.add("foo");
		CONSTANT_LIST_2.add("bar");
		CONSTANT_LIST_2.add(CONSTANT_LIST.get(0));
	}
}
