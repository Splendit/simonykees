package eu.jsparrow.ui.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.jdtunit.JdtUnitFixture;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;

/**
 * Test for {@link SelectRulesWizard}
 * 
 * @author Matthias Webhofer
 * @since 2.2.2
 */
@SuppressWarnings("nls")
public class SelectRulesWizardTest {

	private static final String PACKAGE_PREFIX = "eu.japsarrow.test";

	private static JdtUnitFixture fixture = new JdtUnitFixture();
	private static IPackageFragment baseFragment = null;

	@BeforeClass
	public static void setUp() throws Exception {
		fixture.setUp();

		baseFragment = fixture.addPackageFragment(PACKAGE_PREFIX);
		fixture.addCompilationUnit(baseFragment, "Class1.java");

		IPackageFragment sub1Fragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".sub1");
		fixture.addCompilationUnit(sub1Fragment, "Class11.java");

		IPackageFragment subsubFragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".sub1.subsub");
		fixture.addCompilationUnit(subsubFragment, "Class111.java");

		IPackageFragment sub2Fragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".sub2");
		fixture.addCompilationUnit(sub2Fragment, "Class12.java");
	}

	@AfterClass
	public static void tearDown() throws CoreException {
		fixture.tearDown();
	}

	@Test
	public void collectCURecursively_allCUShouldReturn() throws JavaModelException {
		List<ICompilationUnit> result = new LinkedList<>();
		int allCompilationUnitCount = 4;
		SimonykeesPreferenceManager.setResolvePackagesRecursively(true);

		SelectRulesWizard.collectICompilationUnits(result, Arrays.asList(baseFragment), null);
		assertEquals(allCompilationUnitCount, result.size());
	}

	@Test
	public void collectCURecursively_flatPackageLevelCUShouldReturn() throws JavaModelException {
		List<ICompilationUnit> result = new LinkedList<>();
		int baseFragmentCompilationUnitCount = 1;
		SimonykeesPreferenceManager.setResolvePackagesRecursively(false);

		SelectRulesWizard.collectICompilationUnits(result, Arrays.asList(baseFragment), null);
		assertEquals(baseFragmentCompilationUnitCount, result.size());
	}
}
