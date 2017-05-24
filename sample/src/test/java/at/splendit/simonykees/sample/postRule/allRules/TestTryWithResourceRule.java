package at.splendit.simonykees.sample.postRule.allRules;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "resource", "unused" })
public class TestTryWithResourceRule {

	private static final Logger logger = LoggerFactory.getLogger(TestTryWithResourceRule.class);

	static void readFirstLineFromFile(String path) {

		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path));
				Closeable cl = new BufferedReader(new FileReader(path))) {
			br.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void assignedResource(String path) {
		try (BufferedReader br2 = new BufferedReader(new FileReader(path))) {
			BufferedReader br = new BufferedReader(new FileReader(path));
			br.readLine();
			br = new BufferedReader(new FileReader(path));
			br2.readLine();
			br.close();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void assignedMultipleResources(String path) {
		try (BufferedReader br4 = new BufferedReader(new FileReader(path));
				BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path));
				BufferedReader br003 = new BufferedReader(new FileReader(path));
				BufferedReader br004 = new BufferedReader(new FileReader(path))) {
			br.readLine();
			br2.readLine();
			br003.readLine();
			br004.readLine();
			br4.read();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void assignedMultipleResources1(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path));
				BufferedReader br003 = new BufferedReader(new FileReader(path));
				BufferedReader br004 = new BufferedReader(new FileReader(path));
				BufferedReader br4 = new BufferedReader(new FileReader(path))) {
			br.readLine();
			br2.readLine();
			br003.readLine();
			br004.readLine();
			br4.read();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void assignedMultipleResources2(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br4 = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path));
				BufferedReader br3 = new BufferedReader(new FileReader(path))) {
			br.readLine();
			br2.readLine();
			br3.readLine();
			br4.read();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void assignedMultipleResources3(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br4 = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path))) {
			br.readLine();
			br2.readLine();
			br4.read();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void assignedResource4(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			BufferedReader br2 = new BufferedReader(new FileReader(path));
			br.readLine();
			br2 = new BufferedReader(new FileReader(path));
			br.readLine();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
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
			br.close();
			br2.close();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
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
			br.close();
			br2.close();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void assignedResource7(String path) {

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			BufferedReader br2;
			br2 = new BufferedReader(new FileReader(path));
			br2.readLine();
			br.readLine();
			br2.close();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void nestedTryWithResource(String path) {

		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path))) {
			try (BufferedReader br3 = new BufferedReader(new FileReader(path))) {
				br3.read();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			br2.readLine();
			br.readLine();

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
