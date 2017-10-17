package eu.jsparrow.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.ui.test.jdtunit.JdtUnitFixture;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;

/**
 * Test for {@link SelectRulesWizard}
 * 
 * @author Matthias Webhofer
 * @since 2.2.2
 */
@SuppressWarnings("nls")
public class SelectRuleswizardTest {

	private static final String PACKAGE_PREFIX = "eu.japsarrow.test";

	private static JdtUnitFixture fixture = new JdtUnitFixture();
	
	private static List<ICompilationUnit> baseFragmentCusRecursively = new LinkedList<>();
	private static List<ICompilationUnit> baseFragmentCusNonRecursively = new LinkedList<>();

	@BeforeClass
	public static void setUp() throws Exception {
		fixture.setUp();

		IPackageFragment baseFragment = fixture.addPackageFragment(PACKAGE_PREFIX);
		ICompilationUnit class01 = fixture.addCompilationUnit(baseFragment, "Class01.java");
		ICompilationUnit class02 = fixture.addCompilationUnit(baseFragment, "Class02.java");
		baseFragmentCusRecursively.add(class01);
		baseFragmentCusRecursively.add(class02);
		baseFragmentCusNonRecursively.add(class01);
		baseFragmentCusNonRecursively.add(class02);

		IPackageFragment test1Fragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".test1");
		ICompilationUnit class11 = fixture.addCompilationUnit(test1Fragment, "Class11.java");
		ICompilationUnit class12 = fixture.addCompilationUnit(test1Fragment, "Class12.java");
		ICompilationUnit class13 = fixture.addCompilationUnit(test1Fragment, "Class13.java");
		baseFragmentCusRecursively.add(class11);
		baseFragmentCusRecursively.add(class12);
		baseFragmentCusRecursively.add(class13);

		IPackageFragment test1Sub1Fragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".test1.sub1");
		ICompilationUnit class111 = fixture.addCompilationUnit(test1Sub1Fragment, "Class111.java");
		baseFragmentCusRecursively.add(class111);

		IPackageFragment test1Sub1InternalFragment = fixture
				.addPackageFragment(PACKAGE_PREFIX + ".test1.sub1.internal");
		ICompilationUnit class1111 = fixture.addCompilationUnit(test1Sub1InternalFragment, "Class1111.java");
		ICompilationUnit class1112 = fixture.addCompilationUnit(test1Sub1InternalFragment, "Class1112.java");
		baseFragmentCusRecursively.add(class1111);
		baseFragmentCusRecursively.add(class1112);
		
		IPackageFragment test2Fragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".test2");
		ICompilationUnit class21 = fixture.addCompilationUnit(test2Fragment, "Class21.java");
		ICompilationUnit class22 = fixture.addCompilationUnit(test2Fragment, "Class22.java");
		baseFragmentCusRecursively.add(class21);
		baseFragmentCusRecursively.add(class22);
		
		IPackageFragment test2Sub1Fragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".test2.sub1");
		ICompilationUnit class211 = fixture.addCompilationUnit(test2Sub1Fragment, "Class211.java");
		ICompilationUnit class212 = fixture.addCompilationUnit(test2Sub1Fragment, "Class212.java");
		ICompilationUnit class213 = fixture.addCompilationUnit(test2Sub1Fragment, "Class213.java");
		baseFragmentCusRecursively.add(class211);
		baseFragmentCusRecursively.add(class212);
		baseFragmentCusRecursively.add(class213);
		
		IPackageFragment test3Fragment = fixture.addPackageFragment(PACKAGE_PREFIX + ".test3");
		ICompilationUnit class31 = fixture.addCompilationUnit(test3Fragment, "Class31.java");
		baseFragmentCusRecursively.add(class31);
	}

	@Test
	public void collectCURecursively_allCUShouldReturn() throws JavaModelException {
		IPackageFragment fragment = getBaseFragment();
		List<ICompilationUnit> result = new LinkedList<>();
		
		SimonykeesPreferenceManager.setResolvePackagesRecursively(true);
		
		if(fragment != null) {
			SelectRulesWizard.collectICompilationUnits(result, Arrays.asList(fragment), null);
			assertEquals(baseFragmentCusRecursively.size(), result.size());
		} else {
			fail();
		}
	}
	
	@Test
	public void collectCURecursively_flatPackageLevelCUShouldReturn() throws JavaModelException {
		IPackageFragment fragment = getBaseFragment();
		List<ICompilationUnit> result = new LinkedList<>();
		
		SimonykeesPreferenceManager.setResolvePackagesRecursively(false);
		
		if(fragment != null) {
			SelectRulesWizard.collectICompilationUnits(result, Arrays.asList(fragment), null);
			assertEquals(baseFragmentCusNonRecursively.size(), result.size());
		} else {
			fail();
		}
	}

	@AfterClass
	public static void tearDown() throws CoreException {
		fixture.tearDown();
	}
	
	private IPackageFragment getBaseFragment() throws JavaModelException {
		IPackageFragment[] fragments = fixture.getJavaProject().getPackageFragments();
		Optional<IPackageFragment> fragment = Arrays.stream(fragments).filter(f -> f.getElementName().equals(PACKAGE_PREFIX)).findFirst();
		return fragment.isPresent() ? fragment.get() : null;
	}
}
