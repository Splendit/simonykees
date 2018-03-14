package eu.jsparrow.standalone;

import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

public class YAMLStandaloneConfigTest {
	
	private YAMLStandaloneConfig config; 
	
	@Test 
	public void load_whenFileNotExists_shouldReturnDefaultConfig() throws Exception {
		config = YAMLStandaloneConfig.load(new File("src/test/resources/doesnotexist"));
		
		assertThat(config.getKey(), isEmptyString());
	}
	
	@Test
	public void load_withValidConfigFile_shouldLoadConfig() throws Exception{
		config = YAMLStandaloneConfig.load(new File("src/test/resources/standalone-config.yaml")); //$NON-NLS-1$
		
		assertEquals("Test123", config.getKey());
	}

}
