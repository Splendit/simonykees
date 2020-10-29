package eu.jsparrow.standalone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

	private static final String EU_JSPARROW_PACKAGE = "eu.jsparrow.package";
	private static final String EU_JSPARROW = "eu.jsparrow";

	private CompilationUnitProvider compilationUnitProvider;

	private ICompilationUnit compUnitMock;
	private IPackageDeclaration packageDeclarationMock;

	@Before
	public void setUp() {
		YAMLExcludes excludes = new YAMLExcludes();
		excludes.setExcludePackages(Arrays.asList(EU_JSPARROW, EU_JSPARROW_PACKAGE));

		excludes.setExcludeClasses(Collections.singletonList("eu.jsparrow.test.ExcludedClass.java"));

		compUnitMock = mock(ICompilationUnit.class);
		compilationUnitProvider = new CompilationUnitProvider(Collections.singletonList(compUnitMock), excludes);

		packageDeclarationMock = mock(IPackageDeclaration.class);

	}

	@Test
	public void getFilteredCompilationUnits_excludesIsNull_shouldReturnAllCompilationUnits() {
		YAMLExcludes excludes = null;

		compUnitMock = mock(ICompilationUnit.class);
		compilationUnitProvider = new CompilationUnitProvider(
				Collections.singletonList(compUnitMock), excludes);

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertEquals(1, compilationUnits.size());
	}

	@Test
	public void getFilteredCompilationUnits_classFromExcludedPackage_shouldBeIgnored() throws JavaModelException {
		when(compUnitMock.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] { packageDeclarationMock });
		when(packageDeclarationMock.getElementName()).thenReturn(EU_JSPARROW);

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertTrue(compilationUnits.isEmpty());
	}

	@Test
	public void getFilteredCompilationUnits_classWithoutPackageDeclaration_shouldNotBeIgnored()
			throws JavaModelException {
		when(compUnitMock.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] {});

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertFalse(compilationUnits.isEmpty());
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
	
	/*
	 * Bugfix SIM-1338
	 */
	@Test
	public void getFilteredCompilationUnits_packageInfoFilesExcluded_shouldBeIgnored() throws Exception {
		when(compUnitMock.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] { packageDeclarationMock });
		when(packageDeclarationMock.getElementName()).thenReturn("test.infofiles");
		when(compUnitMock.getElementName()).thenReturn("package-info.java");
		
		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();
		
		assertTrue(compilationUnits.isEmpty());
	}
	
	/*
	 * Bugfix SIM-1338
	 */
	@Test
	public void getFilteredCompilationUnits_moduleInfoFilesExcluded_shouldBeIgnored() throws Exception {
		when(compUnitMock.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] { packageDeclarationMock });
		when(packageDeclarationMock.getElementName()).thenReturn("test.infofiles");
		when(compUnitMock.getElementName()).thenReturn("module-info.java");
		
		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();
		
		assertTrue(compilationUnits.isEmpty());
	}

	@Test
	public void containsExcludedClasses_emptyList_shouldReturnFalse() throws Exception {
		boolean expected = compilationUnitProvider.containsExcludedFiles(Collections.emptyList());
		assertFalse(expected);
	}

	@Test
	public void containsExcludedClasses_classInExcludedFiles_shouldReturnTrue() throws Exception {
		ICompilationUnit compilationUnitInExcludedFiles = mock(ICompilationUnit.class);
		when(compilationUnitInExcludedFiles.getElementName()).thenReturn("ExcludedClass.java");
		when(packageDeclarationMock.getElementName()).thenReturn("eu.jsparrow.test");
		when(compilationUnitInExcludedFiles.getPackageDeclarations())
			.thenReturn(new IPackageDeclaration[] { packageDeclarationMock });

		boolean expected = compilationUnitProvider
			.containsExcludedFiles(Collections.singletonList(compilationUnitInExcludedFiles));

		assertTrue(expected);
	}

	@Test
	public void containsExcludedClasses_classInExcludedPackages_shouldReturnTrue() throws Exception {
		ICompilationUnit compilationUnitInExcludedPackage = mock(ICompilationUnit.class);
		when(packageDeclarationMock.getElementName()).thenReturn(EU_JSPARROW);
		when(compilationUnitInExcludedPackage.getPackageDeclarations())
			.thenReturn(new IPackageDeclaration[] { packageDeclarationMock });

		boolean expected = compilationUnitProvider
			.containsExcludedFiles(Collections.singletonList(compilationUnitInExcludedPackage));

		assertTrue(expected);
	}
	
	@Test
	public void containsExcludedClasses_notExcludedClass_shouldReturnFalse() throws Exception {
		ICompilationUnit compilationUnitInExcludedFiles = mock(ICompilationUnit.class);
		when(compilationUnitInExcludedFiles.getElementName()).thenReturn("NotExcludedClass.java");
		when(packageDeclarationMock.getElementName()).thenReturn("not.excluded.package");
		when(compilationUnitInExcludedFiles.getPackageDeclarations())
			.thenReturn(new IPackageDeclaration[] { packageDeclarationMock });

		boolean expected = compilationUnitProvider
			.containsExcludedFiles(Collections.singletonList(compilationUnitInExcludedFiles));

		assertFalse(expected);
	}

}
