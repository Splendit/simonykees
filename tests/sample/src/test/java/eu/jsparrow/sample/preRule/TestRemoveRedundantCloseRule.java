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
}
