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
}
