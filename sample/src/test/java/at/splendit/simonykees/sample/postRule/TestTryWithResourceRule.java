package at.splendit.simonykees.sample.postRule;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;

public class TestTryWithResourceRule {

	static void readFirstLineFromFile(String path) {

		try (BufferedReader br = new BufferedReader(new FileReader(path)); BufferedReader br2 = new BufferedReader(new FileReader(path)); Closeable cl = new BufferedReader(new FileReader(path))) {
			br.close();
			br2.close();
			cl.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
