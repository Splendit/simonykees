package resource.tryWithResource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TryWithResource {

	static String readFirstLineFromFile(String path) {

		try (BufferedReader br = new BufferedReader(new FileReader(path));
				) {
			BufferedReader br2 = new BufferedReader(new FileReader(path));
			String result = br.readLine();
			br.close();
			return result;
			// TODO Auto-generated catch block
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

}
