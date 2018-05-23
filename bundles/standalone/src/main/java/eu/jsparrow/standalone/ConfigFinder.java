package eu.jsparrow.standalone;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes a folder and searches for a YAML configuration file within. 
 * 
 * @since 2.6.0
 */
public class ConfigFinder {

	private Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	// allows the file name to be config.yml or config.yaml, case insensitive
	private static final Pattern CONFIG_FILE_NAME_PATTERN = Pattern
		.compile("^[Cc][Oo][Nn][Ff][Ii][Gg]\\.[Yy][Aa]{0,1}[Mm][Ll]$"); //$NON-NLS-1$

	public Optional<String> getYAMLFilePath(Path filePath) {

		Optional<String> match = Optional.empty();

		if (Files.exists(filePath)) {
			try {
				// we always get the first match
				match = Files.list(filePath)
					.map(file -> file.getFileName()
						.toString())
					.filter(name -> CONFIG_FILE_NAME_PATTERN.matcher(name)
						.matches())
					.findFirst();

				if (match.isPresent()) {
					match = match.map(fileName -> String.format("%s/%s", filePath, fileName)); //$NON-NLS-1$
				} else {
					logger.debug("No matching config file found in directory: '{}'", filePath.toString()); //$NON-NLS-1$
				}

			} catch (IOException e) {
				/*
				 * The IOException means that the directory does not exist. This
				 * is not an error case for us. This branch should never be
				 * reached.
				 */
				logger.debug("Unable to list files in directory: '{}'", filePath.toString()); //$NON-NLS-1$
			}
		} else {
			logger.debug("Directory does not exist: '{}'", filePath.toString()); //$NON-NLS-1$
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
