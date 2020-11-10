package eu.jsparrow.standalone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.jsparrow.core.config.YAMLExcludes;

@SuppressWarnings("nls")
public class CompilationUnitProviderTest {

	private static final String EU_JSPARROW_PACKAGE = "eu.jsparrow.package";
	private static final String EU_JSPARROW = "eu.jsparrow";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private CompilationUnitProvider compilationUnitProvider;

	private ICompilationUnit compUnitMock;
	private IPackageDeclaration packageDeclarationMock;

	@Before
	public void setUp() throws Exception {
		YAMLExcludes excludes = new YAMLExcludes();
		excludes.setExcludePackages(Arrays.asList(EU_JSPARROW, EU_JSPARROW_PACKAGE));
		excludes.setExcludeClasses(Collections.singletonList("eu.jsparrow.test.ExcludedClass.java"));
		packageDeclarationMock = mock(IPackageDeclaration.class);
		when(packageDeclarationMock.getElementName()).thenReturn("eu.jsparrow.test");
		compUnitMock = createICompilationUnitMock("CompUnit.java", "/eu/jsparrow/test/CompUnit.java",
				packageDeclarationMock);
		compilationUnitProvider = new CompilationUnitProvider(Collections.singletonList(compUnitMock), excludes, "");
	}

	@Test
	public void getFilteredCompilationUnits_noGlobPattern_shouldReturnAllCompilationUnits() throws Exception {
		ICompilationUnit compUnit2 = createICompilationUnitMock("CompUnit2.java", "/some/CompUnit2.java",
				packageDeclarationMock);
		ICompilationUnit compUnit3 = createICompilationUnitMock("CompUnit3.java", "/CompUnit3.java",
				packageDeclarationMock);
		CompilationUnitProvider compilationUnitProvider = new CompilationUnitProvider(
				Arrays.asList(compUnitMock, compUnit2, compUnit3), null, "");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertEquals(3, compilationUnits.size());
	}

	@Test
	public void getFilteredCompilationUnits_multipleGlobPatterns_shouldReturnTwoCompilationUnits() throws Exception {
		ICompilationUnit compUnit2 = createICompilationUnitMock("CompUnit2.java", "eu/jsparrow/test/CompUnit2.java",
				packageDeclarationMock);
		ICompilationUnit compUnit3 = createICompilationUnitMock("CompUnit3.java", "eu/jsparrow/test/CompUnit3.java",
				packageDeclarationMock);
		CompilationUnitProvider compilationUnitProvider = new CompilationUnitProvider(
				Arrays.asList(compUnitMock, compUnit2, compUnit3), null, "test/CompUnit2.java \n test/CompUnit3.java");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertEquals(2, compilationUnits.size());

	}

	@Test
	public void getFilteredCompilationUnits_multipleGlobPatternsWithEmptyLines_shouldReturnTwoCompilationUnits()
			throws Exception {
		ICompilationUnit compUnit2 = createICompilationUnitMock("CompUnit2.java", "eu/jsparrow/test/CompUnit2.java",
				packageDeclarationMock);
		ICompilationUnit compUnit3 = createICompilationUnitMock("CompUnit3.java", "eu/jsparrow/test/CompUnit3.java",
				packageDeclarationMock);
		CompilationUnitProvider compilationUnitProvider = new CompilationUnitProvider(
				Arrays.asList(compUnitMock, compUnit2, compUnit3), null,
				"test/CompUnit2.java \n\n test/CompUnit3.java");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertEquals(2, compilationUnits.size());

	}

	@Test
	public void getFilteredCompilationUnits_excludingSelectedSources_shouldReturnOneCompilationUnit() throws Exception {
		ICompilationUnit compUnit2 = createICompilationUnitMock("CompUnit2.java", "eu/jsparrow/test/CompUnit2.java",
				packageDeclarationMock);
		ICompilationUnit compUnit3 = createICompilationUnitMock("CompUnit3.java", "eu/jsparrow/test/CompUnit3.java",
				packageDeclarationMock);
		YAMLExcludes excludes = new YAMLExcludes();
		excludes.setExcludeClasses(Collections.singletonList("eu.jsparrow.test.CompUnit3.java"));
		CompilationUnitProvider compilationUnitProvider = new CompilationUnitProvider(
				Arrays.asList(compUnitMock, compUnit2, compUnit3), excludes,
				"jsparrow/test/CompUnit2.java\njsparrow/test/CompUnit3.java");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertEquals(1, compilationUnits.size());
	}

	@Test
	public void getFilteredCompilationUnits_selectSingleFile_shouldReturnOneCompilationUnits() throws Exception {

		ICompilationUnit compUnit2 = createICompilationUnitMock("CompUnit2.java",
				"/eu/jsparrow/test/CompUnit2.java", packageDeclarationMock);
		CompilationUnitProvider compilationUnitProvider = new CompilationUnitProvider(
				Arrays.asList(compUnitMock, compUnit2), null, "test/CompUnit.java");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertEquals(1, compilationUnits.size());
	}

	@Test
	public void getFilteredCompilationUnits_globSelectAllInFolder_shouldReturnMatchingCompilationUnits()
			throws Exception {
		ICompilationUnit compUnit2 = createICompilationUnitMock("CompUnit2.java", "/eu/jsparrow/test/CompUnit2.java",
				packageDeclarationMock);
		CompilationUnitProvider compilationUnitProvider = new CompilationUnitProvider(
				Arrays.asList(compUnitMock, compUnit2), null, "test/*");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertEquals(2, compilationUnits.size());
	}

	@Test
	public void getFilteredCompilationUnits_selectOneInFolder_shouldReturnAllCompilationUnits() throws Exception {
		ICompilationUnit compUnit2 = createICompilationUnitMock("CompUnit2.java", "/eu/jsparrow/CompUnit2.java",
				packageDeclarationMock);
		ICompilationUnit compUnit3 = createICompilationUnitMock("CompUnit3.java", "/eu/CompUnit3.java",
				packageDeclarationMock);
		CompilationUnitProvider compilationUnitProvider = new CompilationUnitProvider(
				Arrays.asList(compUnitMock, compUnit2, compUnit3), null, "jsparrow/CompUnit2.java");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertEquals(1, compilationUnits.size());
	}

	@Test
	public void getFilteredCompilationUnits_nonMatchingSelection_shouldReturnNoCompilationUnits() throws Exception {
		CompilationUnitProvider compilationUnitProvider = new CompilationUnitProvider(
				Collections.singletonList(compUnitMock), null, "test/CompUnit2.java");

		List<ICompilationUnit> compilationUnits = compilationUnitProvider.getFilteredCompilationUnits();

		assertTrue(compilationUnits.isEmpty());
	}

	@Test
	public void getFilteredCompilationUnits_excludesIsNull_shouldReturnAllCompilationUnits() {
		YAMLExcludes excludes = null;

		compilationUnitProvider = new CompilationUnitProvider(
				Collections.singletonList(compUnitMock), excludes, "");

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
	public void getFilteredCompilationUnits_nothingExcluded_returnAll() throws JavaModelException {
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

	public static ICompilationUnit createICompilationUnitMock(String name, String path,
			IPackageDeclaration packageDeclarationMock) throws Exception {
		ICompilationUnit compUnit = mock(ICompilationUnit.class);
		IPath path2 = new org.eclipse.core.runtime.Path(path);
		when(compUnit.getPackageDeclarations()).thenReturn(new IPackageDeclaration[] { packageDeclarationMock });
		when(compUnit.getElementName()).thenReturn(name);
		when(compUnit.getPath()).thenReturn(path2);
		return compUnit;
	}
}
