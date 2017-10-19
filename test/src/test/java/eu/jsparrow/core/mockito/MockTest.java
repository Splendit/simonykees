package eu.jsparrow.core.mockito;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import eu.jsparrow.core.config.YAMLConfig;

public class MockTest {
	
	@Test
	public void testMockito() throws Exception {
		YAMLConfig yamlConfig = Mockito.mock(YAMLConfig.class);
		
		Mockito.when(yamlConfig.getDefaultProfile()).thenReturn("I'm a profile");
		
		
		assertEquals("I'm a profile", yamlConfig.getDefaultProfile());
		
	}

}
