package eu.jsparrow.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * This class provides useful methods to deal with YAML configuration. the used
 * library for YAML parsing is snakeyaml.
 * (https://bitbucket.org/asomov/snakeyaml)
 * 
 * @author Matthias Webhofer
 * @since 2.2.2
 */
public class YAMLConfigUtil {

	private static final Logger logger = LoggerFactory.getLogger(YAMLConfigUtil.class);

	private static final String CONFIG_TAG = "!jsparrow.config"; //$NON-NLS-1$

	private YAMLConfigUtil() {
		// private constructor to hide public default constructor
	}

	/**
	 * loads a jsparrow configuration file and returns a {@link YAMLConfig}
	 * 
	 * @param file
	 *            configuration file (e.g. jsparrow.yml)
	 * @return the configuration stored in the configuration file
	 * @throws YAMLConfigException
	 */
	public static YAMLConfig loadConfiguration(File file) throws YAMLConfigException {
		YAMLConfig config = null;
		try (FileInputStream fis = new FileInputStream(file)) {

			/*
			 * the TypeDescription specifies the type of the configuration class and of the
			 * containing list of profiles because generics are a compile time thing. see
			 * exportConfig method.
			 */
			TypeDescription rootTypeDescription = new TypeDescription(YAMLConfig.class, CONFIG_TAG);
			rootTypeDescription.putListPropertyType("profiles", YAMLProfile.class); //$NON-NLS-1$

			/*
			 * the constructor is used for the configuration of snakeyaml
			 */
			Constructor constructor = new Constructor(YAMLConfig.class);
			constructor.addTypeDescription(rootTypeDescription);

			Yaml yaml = new Yaml(constructor);

			config = yaml.loadAs(fis, YAMLConfig.class);
		} catch (YAMLException | IOException e) {
			throw new YAMLConfigException(e.getMessage(), e);
		}
		return config;
	}

	/**
	 * loads the given configuration file and imports the declared profiles to
	 * jsparrow eclipse version
	 * 
	 * @param file
	 *            configuration file
	 * @throws YAMLConfigException
	 */
	public static void importProfiles(File file) throws YAMLConfigException {
		YAMLConfig config = YAMLConfigUtil.loadConfiguration(file);
		if (config != null) {
			List<YAMLProfile> profiles = config.getProfiles();
			profiles.forEach(profile -> {
				// TODO actual import
				logger.info(profile.toString());
			});
		}
	}

	/**
	 * exports configuration in form of {@link YAMLConfig} to the given file. this
	 * is also used for exporting profiles from jsparrow eclipse version.
	 * 
	 * @param config
	 *            configuration for export
	 * @param file
	 *            configuratin file
	 * @throws YAMLConfigException
	 */
	public static void exportConfig(YAMLConfig config, File file) throws YAMLConfigException {
		try (FileWriter fw = new FileWriter(file)) {
			/*
			 * the Representer is used to put an alias type into the YAML file. Otherwise
			 * the fully qualified class name would be used and we could run into troubles
			 * with obfuscation.
			 */
			Representer representer = new Representer();
			representer.addClassTag(YAMLConfig.class, new Tag(CONFIG_TAG));

			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

			Yaml yaml = new Yaml(representer, options);
			yaml.dump(config, fw);

			logger.info("config file exported to " + file.getAbsolutePath()); //$NON-NLS-1$
		} catch (IOException e) {
			throw new YAMLConfigException(e.getMessage(), e);
		}
	}
}
