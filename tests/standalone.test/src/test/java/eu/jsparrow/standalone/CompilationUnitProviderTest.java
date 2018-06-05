package eu.jsparrow.standalone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.config.YAMLExcludes;

@SuppressWarnings("nls")
public class CompilationUnitProviderTest {

	private CompilationUnitProvider compilationUnitProvider;

	private ICompilationUnit compUnitMock;
	private IPackageDeclaration packageDeclarationMock;

	@Before
	public void setUp() {
		YAMLExcludes excludes = new YAMLExcludes();
		excludes.setExcludePackages(Arrays.asList("eu.jsparrow", "eu.jsparrow.package"));

		excludes.setExcludeClasses(Collections.singletonList("eu.jsparrow.test.ExcludedClass.java"));

		compUnitMock = mock(ICompilationUnit.class);
		compilationUnitProvider = new CompilationUnitProvider(Collections.singletonList(compUnitMock), excludes);

		packageDeclarationMock = mock(IPackageDeclaration.class);

	}

	@Test
	public void getFilteredCompilationUnits_classFromExcludedPackage_shouldBeIgnored() throws JavaModelException {
		when(compUnitMock.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] { packageDeclarationMock });
		when(packageDeclarationMock.getElementName()).thenReturn("eu.jsparrow");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertTrue(compilationUnits.isEmpty());
	}

	@Test
	public void getFilteredCompilationUnits_excludedClass_shouldBeIgnored() throws JavaModelException {
		when(compUnitMock.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] { packageDeclarationMock });
		when(packageDeclarationMock.getElementName()).thenReturn("eu.jsparrow.test");
		when(compUnitMock.getElementName()).thenReturn("ExcludedClass.java");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertTrue(compilationUnits.isEmpty());
	}

	@Test
	public void getFilteredCompilationUnits_nothingEcluded_returnAll() throws JavaModelException {
		when(compUnitMock.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] { packageDeclarationMock });
		when(packageDeclarationMock.getElementName()).thenReturn("eu.jsparrow.test");
		when(compUnitMock.getElementName()).thenReturn("NotExcludedChass.java");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertEquals(1, compilationUnits.size());
		assertTrue(compilationUnits.contains(compUnitMock));
	}
}
