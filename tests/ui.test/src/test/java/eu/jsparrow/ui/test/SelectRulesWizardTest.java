package eu.jsparrow.ui.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.JdtUnitFixture;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
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

	private JdtUnitFixture fixture = new JdtUnitFixture();
	private IPackageFragment baseFragment = null;

	@BeforeEach
	public void setUp() throws Exception {
		fixture.setUp();

		baseFragment = fixture.addPackageFragment(PACKAGE_PREFIX);
		fixture.addCompilationUnit(baseFragment, "Class1");

		IPackageFragment sub1Fragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".sub1");
		fixture.addCompilationUnit(sub1Fragment, "Class11");

		IPackageFragment subsubFragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".sub1.subsub");
		fixture.addCompilationUnit(subsubFragment, "Class111");

		IPackageFragment sub2Fragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".sub2");
		fixture.addCompilationUnit(sub2Fragment, "Class12");
	}

	@AfterEach
	public void tearDown() throws CoreException {
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
