package eu.jsparrow.core.config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.osgi.util.NLS;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;

public class YAMLConfigUtilTest {

	
	
	@Test
	public void loadConfiguration_LoadValidYAML_ShouldReturnYamlConfig() throws Exception {
		YAMLConfig config = YAMLConfigUtil.loadConfiguration(loadResource("valid.yaml"));
		assertNotNull(config);
	}
	
	@Test(expected = YAMLConfigException.class)
	public void loadConfiguration_LoadInvalidYAML_ShouldThrowYAMLConfigException() throws Exception {
		YAMLConfig config = YAMLConfigUtil.loadConfiguration(loadResource("invalid.yaml"));
		assertNotNull(config);
	}
	
	
	private File loadResource(String resource) throws URISyntaxException {
		URL url = this.getClass().getResource(resource);
        return new File(url.toURI());
	}
}
