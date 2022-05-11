package eu.jsparrow.sample.postRule.allRules;

import java.io.BufferedReader;
import java.io.FileReader;
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
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	void readFirstLineFromFile2(String path) {

		try (FileReader fileReader = new FileReader(path); BufferedReader br = new BufferedReader(fileReader)) {
			logger.info("First line: {}", br.readLine());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	void readFirstLineFrom2Files(String path, String path2) {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(path), Charset.defaultCharset());
				BufferedReader br2 = Files.newBufferedReader(Paths.get(path2), Charset.defaultCharset())) {
			logger.info("First line of first file: {}", br.readLine());
			logger.info("First line of second file {}", br2.readLine());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
