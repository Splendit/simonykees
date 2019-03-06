package org.eu.jsparrow.rules.common.util;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.junit.Test;
import org.osgi.framework.Version;

import eu.jsparrow.rules.common.util.JdtVersionBindingUtil;

@SuppressWarnings("nls")
public class JdtVersionBindingUtilTest {

	private static final String JDT_VERSION_NEON = "3.12.0";
	private static final String JDT_VERSION_OXYGEN = "3.13.2";
	private static final String JDT_VERSION_PHOTON = "3.14.0";

	@Test
	public void test_findJLSLevel_shouldReturn10() {
		Version jdtVersion = createJDTVersion(JDT_VERSION_PHOTON);
		int jlsLevel = JdtVersionBindingUtil.findJLSLevel(jdtVersion);
		assertEquals(10, jlsLevel);
	}

	@Test
	public void test_findJLSLevel_shouldReturn9() {
		Version jdtVersion = createJDTVersion(JDT_VERSION_OXYGEN);
		int jlsLevel = JdtVersionBindingUtil.findJLSLevel(jdtVersion);
		assertEquals(9, jlsLevel);
	}

	@Test
	public void test_findJLSLevel_shouldReturn8() {
		Version jdtVersion = createJDTVersion(JDT_VERSION_NEON);
		int jlsLevel = JdtVersionBindingUtil.findJLSLevel(jdtVersion);
		assertEquals(8, jlsLevel);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void findTryWithResourcesProperty_JLS8_shouldReturnResources() {
		Version jdtVersion = createJDTVersion(JDT_VERSION_NEON);
		ChildListPropertyDescriptor structuralPropertyDescriptor = JdtVersionBindingUtil
			.findTryWithResourcesProperty(jdtVersion);
		assertEquals(TryStatement.RESOURCES_PROPERTY, structuralPropertyDescriptor);
	}

	@Test
	public void findTryWithResourcesProperty_JLS9_shouldReturnResources2() {
		Version jdtVersion = createJDTVersion(JDT_VERSION_OXYGEN);
		ChildListPropertyDescriptor structuralPropertyDescriptor = JdtVersionBindingUtil
			.findTryWithResourcesProperty(jdtVersion);
		assertEquals(TryStatement.RESOURCES2_PROPERTY, structuralPropertyDescriptor);
	}

	@Test
	public void findTryWithResourcesProperty_JLS10_shouldReturnResources2() {
		Version jdtVersion = createJDTVersion(JDT_VERSION_PHOTON);
		ChildListPropertyDescriptor structuralPropertyDescriptor = JdtVersionBindingUtil
			.findTryWithResourcesProperty(jdtVersion);
		assertEquals(TryStatement.RESOURCES2_PROPERTY, structuralPropertyDescriptor);
	}

	private Version createJDTVersion(String version) {
		return Version.parseVersion(version);
	}

}
