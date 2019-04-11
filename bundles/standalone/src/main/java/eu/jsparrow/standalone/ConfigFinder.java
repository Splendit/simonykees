package eu.jsparrow.standalone;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes a folder and searches for a YAML configuration file within.
 * 
 * @since 2.6.0
 */
public class ConfigFinder {

	enum ConfigType {
		CONFIG_FILE,
		JSPARROW_FILE
	}

	private Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	// allows the file name to be config.yml or config.yaml, case insensitive
	private static final Pattern CONFIG_FILE_NAME_PATTERN = Pattern.compile("^config\\.y[a]{0,1}ml$", //$NON-NLS-1$
			Pattern.CASE_INSENSITIVE);
	private static final Pattern JSPARROW_FILE_NAME_PATTERN = Pattern.compile("^jsparrow\\.y[a]{0,1}ml$"); //$NON-NLS-1$

	public Optional<String> getYAMLFilePath(Path filePath, ConfigType type) {
		switch (type) {
		case JSPARROW_FILE:
			return getYAMLFilePath(filePath, JSPARROW_FILE_NAME_PATTERN);
		case CONFIG_FILE:
			return getYAMLFilePath(filePath, CONFIG_FILE_NAME_PATTERN);
		default:
			return Optional.empty();
		}
	}

	public Optional<String> getYAMLFilePath(Path filePath, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		return getYAMLFilePath(filePath, pattern);
	}

	public Optional<String> getYAMLFilePath(Path filePath, Pattern pattern) {

		Optional<String> match = Optional.empty();

		if (filePath.toFile()
			.exists()) {
			try (Stream<Path> fileList = Files.list(filePath)) {
				/*
				 * We always get the first match, sorted so it's always the
				 * same. "CONFIG.YAML" would always be found first.
				 */
				match = fileList.map(file -> file.getFileName()
					.toString())
					.filter(name -> pattern.matcher(name)
						.matches())
					.sorted()
					.findFirst();

				if (match.isPresent()) {
					match = match.map(fileName -> filePath.resolve(fileName).toString());
				} else {
					logger.debug("No matching config file found in directory: '{}'", filePath); //$NON-NLS-1$
				}

			} catch (IOException e) {
				/*
				 * The IOException means that the directory does not exist. This
				 * is not an error case for us. This branch should never be
				 * reached.
				 */
				logger.debug("Unable to list files in directory: '{}'", filePath); //$NON-NLS-1$
			}
		} else {
			logger.debug("Directory does not exist: '{}'", filePath); //$NON-NLS-1$
		}

		return match;
	}

	/**
	 * This should only be used for unit tests.
	 * 
	 * @param logger
	 */
	void setLogger(Logger logger) {
		this.logger = logger;
	}

}
