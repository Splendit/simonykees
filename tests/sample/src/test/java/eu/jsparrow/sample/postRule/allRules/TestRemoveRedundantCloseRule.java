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

	void readFirstLineFromFileWithComments(String path) {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(path), Charset.defaultCharset())) {
			/* comment 1 before output */
			logger.info("First line: {}", br.readLine());
			/* comment 2 after output and before br.close() */
			/* comment 3 */
			/* comment 4 */
			/* comment 5 */
			/*
			 * comment 6
			 */
			/* comment 7 after br.close() */
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	void readFirstLineFromFileWithComments2(String path, String path2) {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(path), Charset.defaultCharset());
				BufferedReader br2 = Files.newBufferedReader(Paths.get(path2), Charset.defaultCharset())) {
			/* comment 1 before first output */
			logger.info("First line: {}", br.readLine());
			/* comment 2 after first output and before br.close() */
			// comment 3
			// comment 4
			// comment 5
			// comment 6
			// comment 7
			// comment 8
			/*
			 * comment 9
			 */
			// comment 10
			/* comment 11 after br.close() */
			// comment 12 before second output
			logger.info("First line: {}", br2.readLine());
			/* comment 13 after second output and before br2.close() */
			/* comment 14 */
			/* comment 15 */
			/* comment 16 */
			// comment 17
			/*
			 * comment 18
			 */
			/* comment 19 after br2.close() */
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	void onlyCloseStatementWithComment(String path, String path2) {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(path), Charset.defaultCharset())) {
			/* comment 1 before br.close() */
			/* comment 2 */
			/* comment 3 */
			/* comment 4 */
			/*
			 * comment 5
			 */
			/* comment 6 after br.close() */
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	void onlyTwoCloseStatememntsWithComment(String path, String path2) {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(path), Charset.defaultCharset());
				BufferedReader br2 = Files.newBufferedReader(Paths.get(path2), Charset.defaultCharset())) {
			/* comment 1 before br.close() */
			/* comment 2 */
			/* comment 3 */
			/**
			 * comment 4
			 */
			/*
			 * comment 5
			 */
			/* comment 6 after br.close() and before br2.close() */
			// comment 7
			/* comment 8 */
			// comment 9
			// comment 10
			/**
			 * comment 11
			 */
			/* comment 12 after br2.close() */
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
