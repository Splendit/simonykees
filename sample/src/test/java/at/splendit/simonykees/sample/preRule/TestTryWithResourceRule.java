package at.splendit.simonykees.sample.preRule;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;

@SuppressWarnings({"resource", "unused"})
public class TestTryWithResourceRule {

	static void readFirstLineFromFile(String path) {

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			BufferedReader br2 = new BufferedReader(new FileReader(path));
			Closeable cl = new BufferedReader(new FileReader(path));
			br.close();
			br2.close();
			cl.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void assignedResource(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path)), br2 = new BufferedReader(new FileReader(path));
			br.readLine();
			br = new BufferedReader(new FileReader(path));
			br2.readLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void assignedMultipleResources(String path) {
		try (BufferedReader br4 = new BufferedReader(new FileReader(path))) {
			BufferedReader br = new BufferedReader(new FileReader(path)), br2 = new BufferedReader(new FileReader(path)), br3 = new BufferedReader(new FileReader(path));
			br.readLine();
			br2.readLine();
			br3.readLine();
			br4.read();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void assignedMultipleResources2(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path)), br4 = new BufferedReader(new FileReader(path)), br2 = new BufferedReader(new FileReader(path));
			BufferedReader br3 = new BufferedReader(new FileReader(path));
			br.readLine();
			br2.readLine();
			br3.readLine();
			br4.read();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	
	public void assignedMultipleResources3(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path)), br4 = new BufferedReader(new FileReader(path)), br2 = new BufferedReader(new FileReader(path));
			br.readLine();
			br2.readLine();
			br4.read();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void assignedResource4(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path)), br2 = new BufferedReader(new FileReader(path));
			br.readLine();
			br2 = new BufferedReader(new FileReader(path));
			br.readLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void assignedResource5(String path) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			BufferedReader br2 = new BufferedReader(new FileReader(path));
			br.readLine();
			br2 = new BufferedReader(new FileReader(path));
			br.readLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void assignedResource6(String path) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			BufferedReader br2;
			br2 = new BufferedReader(new FileReader(path));
			br2.readLine();
			br.readLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void assignedResource7(String path) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			BufferedReader br2;
			br2 = new BufferedReader(new FileReader(path));
			br2.readLine();
			br.readLine();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void nestedTryWithResource(String path) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			BufferedReader br2 = new BufferedReader(new FileReader(path));
			try {
				BufferedReader br3 = new BufferedReader(new FileReader(path));
				br3.read();
				br3.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
			br2.readLine();
			br.readLine();
			br.close();
			br2.close();
			br2.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
