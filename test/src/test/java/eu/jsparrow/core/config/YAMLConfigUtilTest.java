package eu.jsparrow.core.config;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

@SuppressWarnings("nls")
public class YAMLConfigUtilTest {
	
	private static final String RESOURCE_DIRECTORY = "src/test/resources/eu/jsparrow/core/config";
	
	File exportFile;
	
	@Before
	public void setUp() throws Exception {
		exportFile = File.createTempFile("export", "yaml");
	}
	
	@After
	public void tearDown() {
		exportFile.delete();
	}
	
	
	@Test
	public void loadConfiguration_LoadValidYAML_ShouldReturnYAMLConfig() throws Exception {
		YAMLConfig config = YAMLConfigUtil.loadConfiguration(loadResource("valid.yaml"));
		assertNotNull(config);
	}
	
	@Test(expected = YAMLConfigException.class)
	public void loadConfiguration_LoadInvalidYAML_ShouldThrowException() throws Exception {
		YAMLConfig config = YAMLConfigUtil.loadConfiguration(loadResource("invalid.yaml"));
		assertNotNull(config);
	}
	
	@Test
	public void exportConfig_ToFile_ShouldWriteToFile() throws Exception {
		YAMLConfig config = new YAMLConfig();
		YAMLConfigUtil.exportConfig(config, exportFile);
		
		assertNotEquals(0, exportFile.length());
	}
	
	@Test(expected = YAMLConfigException.class)
	public void exportConfig_ToNonWritableFile_ShouldThrowException() throws Exception {
		YAMLConfig config = new YAMLConfig();
		exportFile.setWritable(false);
		YAMLConfigUtil.exportConfig(config, exportFile);
	}
	
	@Test(expected = YAMLConfigException.class)
	public void getSelectedRulesFromConfig_InvalidSelectedProfile_ShouldThrowException() throws Exception {
		YAMLConfig config = new YAMLConfig();
		config.setSelectedProfile("TEST");
		
		YAMLConfigUtil.getSelectedRulesFromConfig(config, new ArrayList<>());
	}
	
	@Test
	public void getSelectedRulesFromConfig_WithoutProfileWithValidRules_ShouldReturnAllRules() throws Exception {
		YAMLConfig config = new YAMLConfig();
		config.getRules().add("TryWithResourceRule");
		
		YAMLConfigUtil.getSelectedRulesFromConfig(config, new ArrayList<>());
	}
	
	
	private File loadResource(String resource) throws Exception {
		return Paths.get(String.join("/", RESOURCE_DIRECTORY, resource)).toFile();
	}
	
}
