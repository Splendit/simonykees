package eu.jsparrow.core.visitor.impl.map.iterate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * TODO: Discuss the question whether one can reliably assume that this test
 * will always pass for any subclass of Map.
 *
 */
class DemonstrateMapForEachOrderEquivalenceTest {

	@ParameterizedTest
	@ValueSource(classes = {
			HashMap.class,
			TreeMap.class,
			LinkedHashMap.class
	})
	void testMapForEachOrderEquivalence(Class<? extends Map<Integer, String>> mapClass)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {

		Map<Integer, String> map = mapClass.getConstructor()
			.newInstance();
		map.put(1, "1");
		map.put(4, "4");
		map.put(2, "2");
		map.put(7, "7");
		map.put(8, "8");
		List<Integer> keyList = collectKeysForEachKeyInKeySet(map);
		List<Integer> keyList2 = collectKeysForEachEntryInEntrySet(map);
		List<Integer> keyList3 = collectKeysByIterateMapForEach(map);

		for (int i = 0; i < keyList.size(); i++) {
			assertEquals(keyList.get(i), keyList2.get(i));
			assertEquals(keyList.get(i), keyList3.get(i));
		}

	}

	private List<Integer> collectKeysForEachKeyInKeySet(Map<Integer, String> map) {
		List<Integer> keyList = new ArrayList<>();
		for (Integer key : map.keySet()) {
			keyList.add(key);
		}
		return keyList;
	}

	private List<Integer> collectKeysForEachEntryInEntrySet(Map<Integer, String> map) {
		List<Integer> keyList = new ArrayList<>();
		for (Map.Entry<Integer, String> entry : map.entrySet()) {
			keyList.add(entry.getKey());
		}
		return keyList;
	}

	private List<Integer> collectKeysByIterateMapForEach(Map<Integer, String> map) {
		List<Integer> keyList = new ArrayList<>();
		map.forEach((k, v) -> {
			keyList.add(k);
		});
		return keyList;
	}
}
