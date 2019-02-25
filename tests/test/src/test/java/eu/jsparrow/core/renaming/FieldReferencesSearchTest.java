package eu.jsparrow.core.renaming;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.AbstractRulesTest;
import eu.jsparrow.core.visitor.renaming.FieldReferencesSearch;
import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;

@SuppressWarnings("nls")
public class FieldReferencesSearchTest extends AbstractRulesTest  {
	
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
	
	private static final String FIELDS_IN_ANONYMOUS_CLASSES = CORE_PACKAGE + "\n"
			+ "public class FieldsInAnonymousClasses {\n" 
			+ "	public void avoidAnonymousClasses() {\n"
			+ "		Foo foo = new Foo() {\n" 
			+ "			\n" 
			+ "			public String foo_field;\n" 
			+ "			\n"
			+ "			@Override\n" 
			+ "			public void foo() {\n" 
			+ "				this.foo_field = \"\";\n"
			+ "			}\n" 
			+ "		};\n" 
			+ "		Foo foo2 = new Foo() {\n"
			+ "			public String foo_field;\n" 
			+ "			\n" 
			+ "			@Override\n"
			+ "			public void foo() {\n" 
			+ "				this.foo_field = \"\";\n" 
			+ "			}\n"
			+ "		};\n" 
			+ "	}\n" 
			+ "	abstract class Foo {\n" 
			+ "		public abstract void foo();\n" 
			+ "	}\n"
			+ "}";

	private static final Map<String, String> compilationUnitHavingAnonymousClasses;
	private static final Map<String, String> compilationUnitNameContents;
	static {
		Map<String, String> nameContents = new HashMap<>();
		nameContents.put("UsingBadClass.java", USING_CLASS_WITH_DOLLAR_SIGN_NAME);
		nameContents.put("BadClassName$.java", CLASS_WITH_DOLLAR_SIGN_NAME);
		compilationUnitNameContents = Collections.unmodifiableMap(nameContents);
		
		nameContents = new HashMap<>();
		nameContents.put("FieldsInAnonymousClasses.java", FIELDS_IN_ANONYMOUS_CLASSES);
		compilationUnitHavingAnonymousClasses = Collections.unmodifiableMap(nameContents);
	}
	
	private IPackageFragment packageFragment;
	
	@Before
	public void setUpCompilationUnits() throws Exception {
		IPackageFragmentRoot root = AbstractRulesTest.createRootPackageFragment();
		packageFragment = root.createPackageFragment(ROOT_PACKAGE_NAME, true, null);
	}
	
	@Test
	public void findReferences() throws Exception {

		List<CompilationUnit> compilationUnits = RenamingTestHelper
				.loadCompilationUnitsFromString(packageFragment, compilationUnitNameContents);
		/*
		 * Having a FieldDeclarationASTVisitor and a field with unsafe type name
		 * (i.e. having a $ in its name).
		 */

		FieldReferencesSearch searchEngine = new FieldReferencesSearch(new IJavaElement[] { packageFragment });
		VariableDeclarationFragment fragment = RenamingTestHelper.findFieldDeclarations(compilationUnits).stream()
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
	public void findReferences_typeHavingDollarSign() throws Exception {

		List<CompilationUnit> compilationUnits = RenamingTestHelper
				.loadCompilationUnitsFromString(packageFragment, compilationUnitNameContents); 
		/*
		 * Having a FieldDeclarationASTVisitor and a field with 
		 * unsafe type name (i.e. having a $ in its name).
		 */
		
		FieldReferencesSearch searchEngine = new FieldReferencesSearch(new IJavaElement[] { packageFragment });
		VariableDeclarationFragment fragment = RenamingTestHelper.findFieldDeclarations(compilationUnits).stream()
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
	
	@Test
	public void referencesOfFields_anonymousClasses() throws Exception {
		
		List<CompilationUnit> compilationUntis = RenamingTestHelper
				.loadCompilationUnitsFromString(packageFragment, compilationUnitHavingAnonymousClasses);
		/*
		 * Having loaded two anonymous classes of the same type, both declaring
		 * a field with the same name
		 */
		List<VariableDeclarationFragment> declInAnonymousClasses = findDeclarationsInAnonymousClass(compilationUntis);
		assertEquals(2, declInAnonymousClasses.size());

		/*
		 * When searching for the references of the first field
		 */
		VariableDeclarationFragment fragment = declInAnonymousClasses.get(0);
		FieldReferencesSearch searchEngine = new FieldReferencesSearch(new IJavaElement[] { packageFragment });

		List<ReferenceSearchMatch> references = searchEngine.findFieldReferences(fragment)
			.orElse(Collections.emptyList());

		/*
		 * Expecting references to be found...
		 * 
		 * SIM-934 - indeed there is only one reference. But the search
		 * engine is confusing the references of the field in the second
		 * anonymous class with the ones in the first.
		 */
		assertEquals(2, references.size());
	}
}
