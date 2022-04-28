package eu.jsparrow.sample.postRule.allRules;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "nls" })
public class TestRemoveRedundantCloseRule {

	private static final Logger logger = LoggerFactory.getLogger(TestRemoveRedundantCloseRule.class);

	void readFirstLineFromFile(String path) {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(path), Charset.defaultCharset())) {
			logger.info("First line: {}", br.readLine());
			br.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
