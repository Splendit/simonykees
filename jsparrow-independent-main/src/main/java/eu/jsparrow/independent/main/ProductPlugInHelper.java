package eu.jsparrow.independent.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Copies Dependencies to src/main/resources and <br>
 * creates a manifest.independent file
 * https://docs.oracle.com/javase/tutorial/essential/io/copy.html#:~:text=You%20can%20copy%20a%20file,Directories%20can%20be%20copied.
 */
@SuppressWarnings("nls")
public class ProductPlugInHelper {

	private static File productRepositoryPlugInsDirectory;

	private static void clearSourceMainResources(Path surceMainResourcesPath) throws IOException {
		try (Stream<Path> childStream = Files.list(surceMainResourcesPath);) {
			childStream.filter(child -> child.toFile()
				.isFile())
				.forEach(child -> {
					try {
						Files.delete(child);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
		}
	}

	/**
	 * 
	 * @return Sorted List of the names of all plug-in files from the
	 *         directory<br>
	 *         {@code "/simonykees/releng/eu.jsparrow.independent.product/target/repository/plugins"}.
	 * @throws IOException
	 */
	static List<String> getProductPlugInNames() throws IOException {
		List<String> pluginNames = Arrays.asList(getRepositoryPlugInsDirectory().list());
		Collections.sort(pluginNames);
		return pluginNames;
	}

	static void copyProductPlugIns() throws URISyntaxException, IOException {

		Path sourceMainResourcesAbsolutePath = Paths.get("src", "main", "resources")
			.toAbsolutePath();
		clearSourceMainResources(sourceMainResourcesAbsolutePath);
		File sourceMainResourcesAbsolutePath2File = sourceMainResourcesAbsolutePath.toFile();

		File[] pluginFiles = getRepositoryPlugInsDirectory().listFiles();
		for (File file : pluginFiles) {
			Path source = file.toPath();
			Path target = (new File(sourceMainResourcesAbsolutePath2File, file.getName())).toPath();
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	static File getRepositoryPlugInsDirectory() throws IOException {
		if (productRepositoryPlugInsDirectory == null) {
			File simonykees = new File("..").getCanonicalFile();
			Path pathToRepositoryPlugins = Paths.get(simonykees.getAbsolutePath(), //
					"releng", //
					"eu.jsparrow.independent.product", //
					"target", //
					"repository", //
					"plugins");
			productRepositoryPlugInsDirectory = pathToRepositoryPlugins.toFile();
		}
		return productRepositoryPlugInsDirectory;
	}

	/**
	 * If it is an acceptable approach to get the plug-in - input stream
	 * directly from the plug-in directory of the product, then this method will
	 * be used, and the method where the plug-ins are copied to the resources
	 * folder to be installed from there can be abolished.
	 */
	static InputStream getRepositoryPlugInInputStream(String plugInName) throws IOException {
		File plugInFile = new File(getRepositoryPlugInsDirectory(), plugInName);
		return new FileInputStream(plugInFile);
	}

	public static void main(String[] args) {
		try {
			copyProductPlugIns();
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}
}