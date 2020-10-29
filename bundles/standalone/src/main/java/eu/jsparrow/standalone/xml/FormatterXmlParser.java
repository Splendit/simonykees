package eu.jsparrow.standalone.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.standalone.xml.model.Profile;
import eu.jsparrow.standalone.xml.model.Profiles;

public class FormatterXmlParser {

	private static final Logger logger = LoggerFactory.getLogger(FormatterXmlParser.class);

	private static final String CODE_FORMATTER_PROFILE_KEY = "CodeFormatterProfile"; //$NON-NLS-1$

	private FormatterXmlParser() {
	}

	/**
	 * Returns a {@link Map} of formatter settings entries, with id as key and
	 * value as value.
	 * </p>
	 * Note: The XML is structured to allow more than one profile. In case there
	 * is more than one profile, this will method always return the first one!
	 * </p>
	 * Shortened example Eclipse formatter file:
	 *
	 * <pre>
	 * <code>
	 * {@code
	 * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
	 * <profiles version="17">
	 *     <profile kind="CodeFormatterProfile" name=
	"Eclipse Splendit default" version="17">
	 *         <setting id=
	"org.eclipse.jdt.core.formatter.insert_space_after_ellipsis" value=
	"insert"/>
	 *         <setting id=
	"org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations" value
	="insert"/>
	 *     </profile>
	 * </profiles>}
	 * </code>
	 * </pre>
	 *
	 * @param file
	 *            Eclipse formatter file
	 * @return a {@link Map} of settings
	 * @throws FormatterXmlParserException
	 *             when parsing of the provided file is impossible (e.g.,
	 *             invalid XML, non-existing file, etc.)
	 */
	public static Map<String, String> getFormatterSettings(File file) throws FormatterXmlParserException {
		Profiles profiles;

		/*
		 * we let null values pass because the unmarshaller throws an exception
		 * if the file is null and we need to handle that exception anyway.
		 */
		if (file != null && !file.exists()) {
			throw new FormatterXmlParserException(String.format("Path unavailable: %s", file.getAbsolutePath())); //$NON-NLS-1$
		}

		try {
			JAXBContext context = JAXBContext.newInstance(Profiles.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			profiles = (Profiles) unmarshaller.unmarshal(file);
		} catch (JAXBException e) {
			throw new FormatterXmlParserException(
					String.format("Unable to parse the given formatting file: %s", file.getAbsolutePath()), e); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
			// if the file parameter is null
			throw new FormatterXmlParserException("File path is null", e); //$NON-NLS-1$
		} catch (ClassCastException e) {
			// when the root element cannot be cast to Profiles
			throw new FormatterXmlParserException(
					String.format("Unexpected XML structure: %s", file.getAbsolutePath()), e); //$NON-NLS-1$
		}

		Profile relevantProfile = profiles.getProfiles()
			.stream()
			.filter(p -> CODE_FORMATTER_PROFILE_KEY.equals(p.getKind()))
			.findFirst()
			.orElse(null);

		if (relevantProfile == null) {
			throw new FormatterXmlParserException(
					String.format("No CodeFormatterProfile found in: %s", file.getAbsolutePath())); //$NON-NLS-1$
		}

		Map<String, String> settings = new HashMap<String, String>();

		// add settings to the map
		relevantProfile.getSettings()
			.forEach(s -> settings.put(s.getId(), s.getValue()));

		if (settings.isEmpty()) {
			throw new FormatterXmlParserException(
					String.format("No formatter settings found in: %s", file.getAbsolutePath())); //$NON-NLS-1$
		}

		logger.debug("'{}' settings loaded for formatting profile '{}' in '{}'", settings.size(), //$NON-NLS-1$
				relevantProfile != null ? relevantProfile.getName() : "<no profile found>", file.getAbsolutePath()); //$NON-NLS-1$

		return settings;
	}
}