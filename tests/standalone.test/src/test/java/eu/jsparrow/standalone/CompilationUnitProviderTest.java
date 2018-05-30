package eu.jsparrow.standalone;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.config.YAMLExcludes;

@SuppressWarnings("nls")
public class CompilationUnitProviderTest {

	private StandaloneConfig standaloneConfig;
	private YAMLExcludes excludes;
	private CompilationUnitProvider compilationUnitProvider;

	private ICompilationUnit compUnit;
	private List<ICompilationUnit> compilationUnits;
	private IPackageDeclaration packageDeclaration;

	@Before
	public void setUp() {
		excludes = new YAMLExcludes();
		excludes.setExcludePackages(new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
			{
				add("eu.jsparrow");
				add("eu.jsparrow.package");
			}
		});

		excludes.setExcludeClasses(new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
			{
				add("eu.jsparrow.test.ExcludedClass.java");
			}
		});

		standaloneConfig = mock(StandaloneConfig.class);

		compilationUnitProvider = new CompilationUnitProvider(standaloneConfig, excludes);

		compUnit = mock(ICompilationUnit.class);
		compilationUnits = new ArrayList<ICompilationUnit>() {
			private static final long serialVersionUID = 1L;
			{
				add(compUnit);
			}
		};
		packageDeclaration = mock(IPackageDeclaration.class);

	}

	@Test
	public void getFilteredCompilationUnits_classFromExcludedPackage_shouldBeIgnored() throws JavaModelException {
		when(standaloneConfig.getICompilationUnits()).thenReturn(compilationUnits);
		when(compUnit.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] { packageDeclaration });
		when(packageDeclaration.getElementName()).thenReturn("eu.jsparrow");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertTrue(compilationUnits.isEmpty());
	}

	@Test
	public void getFilteredCompilationUnits_excludedClass_shouldBeIgnored() throws JavaModelException {
		when(standaloneConfig.getICompilationUnits()).thenReturn(compilationUnits);
		when(compUnit.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] { packageDeclaration });
		when(packageDeclaration.getElementName()).thenReturn("eu.jsparrow.test");
		when(compUnit.getElementName()).thenReturn("ExcludedClass.java");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertTrue(compilationUnits.isEmpty());
	}

	@Test
	public void getFilteredCompilationUnits_nothingEcluded_returnAll() throws JavaModelException {
		when(standaloneConfig.getICompilationUnits()).thenReturn(compilationUnits);
		when(compUnit.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] { packageDeclaration });
		when(packageDeclaration.getElementName()).thenReturn("eu.jsparrow.test");
		when(compUnit.getElementName()).thenReturn("NotExcludedChass.java");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertTrue(compilationUnits.size() == 1);
		assertTrue(compilationUnits.contains(compUnit));
	}
}
