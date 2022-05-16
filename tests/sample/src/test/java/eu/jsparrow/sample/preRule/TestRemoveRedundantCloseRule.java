package eu.jsparrow.sample.preRule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@SuppressWarnings({ "nls" })
public class TestRemoveRedundantCloseRule {

	void readFirstLineFromFile(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			System.out.println("First line: " + br.readLine());
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readFirstLineFromFile2(String path) {

		try (FileReader fileReader = new FileReader(path); BufferedReader br = new BufferedReader(fileReader)) {
			System.out.println("First line: " + br.readLine());
			fileReader.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readFirstLineFrom2Files(String path, String path2) {
		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path2))) {
			System.out.println("First line of first file: " + br.readLine());
			br.close();
			System.out.println("First line of second file " + br2.readLine());
			br2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readFirstLineFromFileWithComments(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			/* comment 1 before output */
			System.out.println("First line: " + br.readLine());
			/* comment 2 after output and before br.close() */
			br/* comment 3 */./* comment 4 */close(/* comment 5 */)
			/*
			 * comment 6
			 */;
			/* comment 7 after br.close() */
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readFirstLineFromFileWithComments2(String path, String path2) {
		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path2))) {
			/* comment 1 before first output */
			System.out.println("First line: " + br.readLine());
			/* comment 2 after first output and before br.close() */
			br // comment 3
				. // comment 4
				close // comment 5
				( // comment 6
				) // comment 7
					// comment 8
			/*
			 * comment 9
			 */; // comment 10
			/* comment 11 after br.close() */
			// comment 12 before second output
			System.out.println("First line: " + br2.readLine());
			/* comment 13 after second output and before br2.close() */
			br2/* comment 14 */./* comment 15 */close(/* comment 16 */)
			// comment 17
			/*
			 * comment 18
			 */;
			/* comment 19 after br2.close() */
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void onlyCloseStatementWithComment(String path, String path2) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			/* comment 1 before br.close() */
			br/* comment 2 */./* comment 3 */close(/* comment 4 */)
			/*
			 * comment 5
			 */;
			/* comment 6 after br.close() */
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void onlyTwoCloseStatememntsWithComment(String path, String path2) {
		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path2))) {
			/* comment 1 before br.close() */
			br/* comment 2 */./* comment 3 */close(
			/**
			 * comment 4
			 */
			)
			/*
			 * comment 5
			 */;
			/* comment 6 after br.close() and before br2.close() */
			br2// comment 7
				.close(/* comment 8 */) // comment 9
			// comment 10
			/**
			 * comment 11
			 */
			;
			/* comment 12 after br2.close() */
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
