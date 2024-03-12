package eu.jsparrow.independent.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("nls")
class SnakeYamlTest {

	public void writeMapToYaml(Map<String, Object> dataMap, File file) throws IOException {

		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		PrintWriter writer = new PrintWriter(file);
		// StringWriter writer = new StringWriter();
		yaml.dump(dataMap, writer);
	}

	private Map<String, Object> createPersonDataMap() {
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("id", 19);
		dataMap.put("name", "John");
		dataMap.put("address", "Star City");
		dataMap.put("department", "Medical");
		dataMap.put("hobbies", Arrays.asList("Football", "Hiking", "Dicso"));
		Map<String, String> languages = new HashMap<>();
		languages.put("English", "fluent in spoken and written");
		languages.put("German", "advanced");
		languages.put("French", "advanced");
		languages.put("Spanish", "basics");
		dataMap.put("languages", languages);
		return dataMap;
	}

	private Map<String, Object> readMapFromYaml(String fileName) {
		InputStream inputStream = this.getClass()
			.getClassLoader()
			.getResourceAsStream(fileName);
		Yaml yaml = new Yaml();
		Map<String, Object> data = yaml.load(inputStream);
		System.out.println(data);
		return data;
	}

	@SuppressWarnings("unchecked")
	@Test
	void test() throws IOException {

		File file = new File("./src/main/resources/map_to_yaml.yml").getCanonicalFile();
		assertTrue(file.getAbsolutePath()
			.endsWith("/simonykees/jsparrow-independent-main/src/main/resources/map_to_yaml.yml"));
		Files.deleteIfExists(file.toPath());
		Map<String, Object> dataMap = createPersonDataMap();
		writeMapToYaml(dataMap, file);
		Map<String, Object> mapFromYaml = readMapFromYaml("map_to_yaml.yml");
		assertNotNull(mapFromYaml);
		assertEquals(6, mapFromYaml.size());
		assertEquals(4, ((Map<String, String>) mapFromYaml.get("languages")).size());
	}

}
