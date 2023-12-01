package eu.jsparrow.independent.main;

import java.io.File;
import java.io.IOException;
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
 * // TODO: implement method to copy dependencies to main resources<br>
 * // TODO: implement method to create manifest.independent file
 */
@SuppressWarnings("nls")
public class ProductPlugInHelper {

	// static Optional<Path> findSourceMainResourcesDirectory() throws
	// URISyntaxException {
	// File srcMainResources = new File("src/main/resources");
	// String absolutePathAsString = srcMainResources.getAbsolutePath();
	// URI uri;
	// uri = new URI("file://" + absolutePathAsString);
	// Path path = Paths.get(uri);
	// if (Files.isDirectory(path)) {
	// return Optional.of(path);
	// }
	// return Optional.empty();
	// }

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
		File simonykees = new File("..").getCanonicalFile();

		Path plugInsPath = Paths.get(simonykees.getAbsolutePath(), //
				"releng", //
				"eu.jsparrow.independent.product", //
				"target", //
				"repository", //
				"plugins");

		List<String> pluginNames = Arrays.asList(plugInsPath.toFile()
			.list());

		Collections.sort(pluginNames);

		return pluginNames;
	}

	static void copyProductPlugIns() throws URISyntaxException, IOException {
		Path sourceMainResourcesAbsolutePath = Paths.get("src", "main", "resources")
			.toAbsolutePath();
		clearSourceMainResources(sourceMainResourcesAbsolutePath);

		// File jSparrowIndependentMain = new File(".").getCanonicalFile();
		// Path srcMainResources = Paths.get("src", "main",
		// "resources").toAbsolutePath();
		File simonykees = new File("..").getCanonicalFile();

		Path plugInsPath = Paths.get(simonykees.getAbsolutePath(), //
				"releng", //
				"eu.jsparrow.independent.product", //
				"target", //
				"repository", //
				"plugins");

		File sourceMainResourcesAbsolutePath2File = sourceMainResourcesAbsolutePath.toFile();

		File[] pluginFiles = plugInsPath.toFile()
			.listFiles();
		for (File file : pluginFiles) {
			Path source = file.toPath();
			Path target = (new File(sourceMainResourcesAbsolutePath2File, file.getName())).toPath();
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static void main(String[] args) {
		try {
			copyProductPlugIns();
		} catch (URISyntaxException | IOException e) {

			e.printStackTrace();
		}

	}
}