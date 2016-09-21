package at.splendit.simonykees.sample.preRule;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TryWithResourceTest {

	static String readFirstLineFromFile(String path) {

		try (BufferedReader br = new BufferedReader(new FileReader(path))
				) {
			BufferedReader br2 = new BufferedReader(new FileReader(path));
			Closeable cl = new BufferedReader(new FileReader(path));
			br.close();
			return "";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();			
		}
		
		
		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path));
				Closeable cl = new BufferedReader(new FileReader(path))) {
			return "";
		}
		 catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}

}
