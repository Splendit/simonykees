package eu.jsparrow.standalone.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.stream.XMLInputFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.jsparrow.standalone.xml.model.Profile;
import eu.jsparrow.standalone.xml.model.Profiles;
import eu.jsparrow.standalone.xml.model.Setting;

/**
 * This class is used to parse Eclipse formatting XML files, using the JAXB. See
 * {@link FormatterXmlParser#getFormatterSettings(File)} for more information.
 * 
 * @since 3.23.0
 */
public class FormatterXmlParser {

    private static final Logger logger = LoggerFactory.getLogger(FormatterXmlParser.class);

    private static final String CODE_FORMATTER_PROFILE_KEY = "CodeFormatterProfile"; //$NON-NLS-1$

    private FormatterXmlParser() {
        // methods should be called statically, no instance needed
    }

    /**
     * Returns a {@link Map} of formatter settings entries, with id as key and
     * value as value.
     * </p>
     * Note: The XML is structured to allow more than one profile. In case there
     * is more than one profile, this method will always return the first one!
     * </p>
     * Shortened example Eclipse formatter file:
     *
     * <pre>
     * <code>
     * {@code
     * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
     * <profiles version="17">
     *     <profile kind="CodeFormatterProfile" name=
     * "Eclipse Splendit default" version="17">
     *         <setting id=
     * "org.eclipse.jdt.core.formatter.insert_space_after_ellipsis" value=
     * "insert"/>
     *         <setting id=
     * "org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations" value
     * ="insert"/>
     *     </profile>
     * </profiles>}
     * </code>
     * </pre>
     *
     * @param file Eclipse formatter file
     * @return a {@link Map} of settings
     * @throws FormatterXmlParserException when parsing of the provided file is impossible (e.g.,
     *                                     invalid XML, non-existing file, etc.)
     */
    public static Map<String, String> getFormatterSettings(File file) throws FormatterXmlParserException {
        Profiles profiles;

        if (file == null) {
            throw new FormatterXmlParserException("File path is null"); //$NON-NLS-1$
        }

        String absolutePath = file.getAbsolutePath();

        if (!file.exists()) {
            throw new FormatterXmlParserException(String.format("Path unavailable: %s", absolutePath)); //$NON-NLS-1$
        }

        String fileContent = null;
        try {
            fileContent = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new FormatterXmlParserException(
                    String.format("Unable to parse the given formatting file: %s", absolutePath), e); //$NON-NLS-1$
        }

        try {
            XMLInputFactory ifactory = new WstxInputFactory(); // Woodstox XMLInputFactory impl
            ifactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, 32000);
            XmlMapper xmlMapper = new XmlMapper(ifactory);

            profiles = xmlMapper.readValue(fileContent, Profiles.class);
        } catch (JsonProcessingException e) {
            throw new FormatterXmlParserException(
                    String.format("Unexpected XML structure in: %s", absolutePath), e); //$NON-NLS-1$
        }

        Profile relevantProfile = profiles.getProfileList()
                .stream()
                .filter(p -> CODE_FORMATTER_PROFILE_KEY.equals(p.getKind()))
                .findFirst()
                .orElse(null);

        if (relevantProfile == null) {
            throw new FormatterXmlParserException(
                    String.format("No CodeFormatterProfile found in: %s", absolutePath)); //$NON-NLS-1$
        }

        Map<String, String> settings = relevantProfile.getSettings()
                .stream()
                .collect(Collectors.toMap(Setting::getId, Setting::getValue));

        if (settings.isEmpty()) {
            throw new FormatterXmlParserException(
                    String.format("No formatter settings found in: %s", absolutePath)); //$NON-NLS-1$
        }

        logger.debug("'{}' settings loaded for formatting profile '{}' in '{}'", settings.size(), //$NON-NLS-1$
                relevantProfile.getName(), absolutePath);

        return settings;
    }

}