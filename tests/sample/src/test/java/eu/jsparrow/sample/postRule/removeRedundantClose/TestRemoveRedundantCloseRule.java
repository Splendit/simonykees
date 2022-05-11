package eu.jsparrow.sample.postRule.removeRedundantClose;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@SuppressWarnings({ "nls" })
public class TestRemoveRedundantCloseRule {

	void readFirstLineFromFile(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			System.out.println("First line: " + br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readFirstLineFromFile2(String path) {

		try (FileReader fileReader = new FileReader(path); BufferedReader br = new BufferedReader(fileReader)) {
			System.out.println("First line: " + br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void readFirstLineFrom2Files(String path, String path2) {
		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path2))) {
			System.out.println("First line of first file: " + br.readLine());
			System.out.println("First line of second file " + br2.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
