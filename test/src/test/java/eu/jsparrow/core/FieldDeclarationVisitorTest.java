package eu.jsparrow.core;

import static eu.jsparrow.core.util.ASTNodeUtil.convertToTypedList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.util.RefactoringUtil;
import eu.jsparrow.core.visitor.renaming.FieldDeclarationASTVisitor;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.core.visitor.renaming.FieldReferencesSearchEngine;
import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;

@SuppressWarnings("nls")
public class FieldDeclarationVisitorTest extends AbstractRulesTest {
	
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
		compilationUnits = loadCompilationUnits();
	}
	
	@Test
	public void computeMetaData() throws JavaModelException, IOException {

		/*
		 * Having a FieldDeclarationASTVisitor
		 */
		FieldDeclarationASTVisitor referencesVisitor = new FieldDeclarationASTVisitor(
				new IJavaElement[] { packageFragment });
		
		/*
		 * When visiting the loaded compilation units
		 */
		for (CompilationUnit compilationUnit : compilationUnits) {
			compilationUnit.accept(referencesVisitor);
		}
		
		/*
		 * expecting two re-nameable fields to be found...
		 */
		List<FieldMetaData> metaData = referencesVisitor.getFieldMetaData();
		assertEquals(2, metaData.size());

		/*
		 * ... with the following properties...
		 */
		FieldMetaData fieldNameMetaData = findByOldIdentifier(metaData, "field_name");
		assertNotNull(fieldNameMetaData);
		assertEquals(1, fieldNameMetaData.getReferences().size());
		assertEquals("fieldName", fieldNameMetaData.getNewIdentifier());
		
		fieldNameMetaData = findByOldIdentifier(metaData, "notAnInstance_ofBad_class");
		assertNotNull(fieldNameMetaData);
		assertEquals("notAnInstanceOfBadClass", fieldNameMetaData.getNewIdentifier());
		assertEquals(1, fieldNameMetaData.getReferences().size());
		
		fieldNameMetaData = findByOldIdentifier(metaData, "bad_class_name");
		assertNull(fieldNameMetaData);
	}

	@Test
	public void findReferences() throws JavaModelException, IOException {

		/*
		 * Having a FieldDeclarationASTVisitor and a field with 
		 * unsafe type name (i.e. having a $ in its name).
		 */
		
		FieldReferencesSearchEngine searchEngine = new FieldReferencesSearchEngine(new IJavaElement[] { packageFragment });
		List<VariableDeclarationFragment> fragments = findDeclarationsWithUnsafeTypeName(compilationUnits);
		assertFalse(fragments.isEmpty());
		VariableDeclarationFragment fragment = fragments.get(0);
		assertEquals("bad_class_name", fragment.getName().getIdentifier());
		/*
		 * When searching for the references of the field with unsafe type name 
		 */
		Optional<List<ReferenceSearchMatch>> optReferences = searchEngine.findFieldReferences(fragment);
		assertTrue(optReferences.isPresent());

		/*
		 * expecting no references to be found, event though the field is
		 * referenced two times.
		 * FIXME this looks like a bug in org.eclipse.jdt.core.search.SearchEngine. 
		 * If the bug will eventually be fixed, this test case will fail. 
		 */
		List<ReferenceSearchMatch> references = optReferences.get();
		assertTrue("No references can be found if the type of the field has a $", references.isEmpty());
	}
	
	private List<CompilationUnit> loadCompilationUnits() throws JavaModelException, IOException {

		packageFragment = root.createPackageFragment(ROOT_PACKAGE_NAME, true, null);
		List<ICompilationUnit> iCompilationUnits = new ArrayList<>();
		for (Map.Entry<String, String> entry : compilationUnitNameContents.entrySet()) {
			iCompilationUnits.add(packageFragment.createCompilationUnit(entry.getKey(), entry.getValue(), true, null));
		}
		return iCompilationUnits.stream()
			.map(RefactoringUtil::parse)
			.collect(Collectors.toList());
	}

	private List<VariableDeclarationFragment> findDeclarationsWithUnsafeTypeName(
			List<CompilationUnit> compilationUnits) {
		List<VariableDeclarationFragment> fragments = new ArrayList<>();
		compilationUnits.stream()
			.flatMap(cu -> convertToTypedList(cu.types(), TypeDeclaration.class).stream())
			.flatMap(type -> convertToTypedList(type.bodyDeclarations(), FieldDeclaration.class).stream())
			.forEach(field -> hasDollarSignInTypeName(fragments, field));

		return fragments;
	}

	private void hasDollarSignInTypeName(List<VariableDeclarationFragment> fragments, FieldDeclaration field) {
		Type fieldType = field.getType();
		ITypeBinding fieldTypeBinding = fieldType.resolveBinding();
		if (fieldTypeBinding != null && fieldTypeBinding.getName()
			.contains("$")) {
			fragments
				.addAll(convertToTypedList(field.fragments(), VariableDeclarationFragment.class));
		}
	}
	
	private FieldMetaData findByOldIdentifier(List<FieldMetaData> metaData, String string) {
		return metaData.stream()
			.filter(mData -> mData.getFieldDeclaration()
				.getName()
				.getIdentifier()
				.equals(string))
			.findFirst()
			.orElse(null);
	}
}
