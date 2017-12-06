package eu.jsparrow.core;

import static eu.jsparrow.core.util.ASTNodeUtil.convertToTypedList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.visitor.renaming.FieldReferencesSearch;
import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;

@SuppressWarnings("nls")
public class FieldReferencesSearchTest extends AbstractRulesTest {
	
	private static final String ROOT_PACKAGE_NAME = "eu.jsparrow.core";
	private static final String CORE_PACKAGE = "package " + ROOT_PACKAGE_NAME + ";";
	private static final String CLASS_WITH_DOLLAR_SIGN_NAME = CORE_PACKAGE + "\n" + 
			"\n" + 
			""
			+ "public class BadClassName$ {\n" + 
			"	public String field_name = \"\";\n" + 
			"	\n" + 
			"	public String getFieldName() {\n" + 
			"		return this.field_name;\n" + 
			"	}\n" + 
			"}";
	
	private static final String USING_CLASS_WITH_DOLLAR_SIGN_NAME = CORE_PACKAGE + "\n" + 
			"\n" + 
			""+
			"public class UsingBadClass {\n" + 
			"	\n" + 
			"	public BadClassName$ bad_class_name = new BadClassName$();\n" + 
			"	public String notAnInstance_ofBad_class;\n" + 
			"	\n" + 
			"	public UsingBadClass() {\n" + 
			"		notAnInstance_ofBad_class = \"\";\n" + 
			"		bad_class_name = new BadClassName$();\n" + 
			"		bad_class_name.getFieldName();\n" + 
			"	}\n" + 
			"}";
	
	private static final Map<String, String> compilationUnitNameContents;
	static {
		Map<String, String> nameContents = new HashMap<>();
		nameContents.put("UsingBadClass.java", USING_CLASS_WITH_DOLLAR_SIGN_NAME);
		nameContents.put("BadClassName$.java", CLASS_WITH_DOLLAR_SIGN_NAME);
		compilationUnitNameContents = Collections.unmodifiableMap(nameContents);
	}
	
	private IPackageFragment packageFragment;
	private List<CompilationUnit> compilationUnits;
	
	@Before
	public void setUpCompilationUnits() throws JavaModelException, IOException {
		packageFragment = root.createPackageFragment(ROOT_PACKAGE_NAME, true, null);
		compilationUnits = loadCompilationUnits(packageFragment, compilationUnitNameContents);
	}
	
	@Test
	public void findReferences() throws JavaModelException, IOException {

		/*
		 * Having a FieldDeclarationASTVisitor and a field with unsafe type name
		 * (i.e. having a $ in its name).
		 */

		FieldReferencesSearch searchEngine = new FieldReferencesSearch(new IJavaElement[] { packageFragment });
		VariableDeclarationFragment fragment = findFieldDeclarations(compilationUnits).stream()
			.filter(f -> "notAnInstance_ofBad_class".equals(f.getName().getIdentifier()))
			.findFirst().orElse(null);
		assertNotNull(fragment);

		/*
		 * When searching for the references of the field with unsafe type name
		 */
		Optional<List<ReferenceSearchMatch>> optReferences = searchEngine.findFieldReferences(fragment);
		assertTrue(optReferences.isPresent());

		/*
		 * expecting one reference to be found
		 */
		List<ReferenceSearchMatch> references = optReferences.get();
		assertEquals(1, references.size());
	}
	
	@Test
	public void findReferences_typeHavingDollarSign() throws JavaModelException, IOException {

		/*
		 * Having a FieldDeclarationASTVisitor and a field with 
		 * unsafe type name (i.e. having a $ in its name).
		 */
		
		FieldReferencesSearch searchEngine = new FieldReferencesSearch(new IJavaElement[] { packageFragment });
		VariableDeclarationFragment fragment = findFieldDeclarations(compilationUnits).stream()
				.filter(f -> "bad_class_name".equals(f.getName().getIdentifier()))
				.findFirst()
				.orElse(null);
		assertNotNull(fragment);

		/*
		 * When searching for the references of the field with unsafe type name 
		 */
		Optional<List<ReferenceSearchMatch>> optReferences = searchEngine.findFieldReferences(fragment);
		assertTrue(optReferences.isPresent());

		/*
		 * expecting no references to be found, event though the field is
		 * referenced two times. it looks like a bug in org.eclipse.jdt.core.search.SearchEngine. 
		 * If the bug will eventually be fixed, this test case will fail. 
		 */
		List<ReferenceSearchMatch> references = optReferences.get();
		assertTrue("No references can be found if the type of the field has a $", references.isEmpty());
	}
	
	private List<VariableDeclarationFragment> findFieldDeclarations(
			List<CompilationUnit> compilationUnits) {
		return compilationUnits.stream()
			.flatMap(cu -> convertToTypedList(cu.types(), TypeDeclaration.class).stream())
			.flatMap(type -> convertToTypedList(type.bodyDeclarations(), FieldDeclaration.class).stream())
			.flatMap(field -> ASTNodeUtil.convertToTypedList(field.fragments(), VariableDeclarationFragment.class).stream())
			.collect(Collectors.toList());

	}
}
