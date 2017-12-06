package eu.jsparrow.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.visitor.renaming.FieldDeclarationASTVisitor;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;

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
		packageFragment = root.createPackageFragment(ROOT_PACKAGE_NAME, true, null);
		compilationUnits = loadCompilationUnits(packageFragment, compilationUnitNameContents);
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
