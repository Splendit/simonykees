package eu.jsparrow.core.visitor.unused;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.AbstractRulesTest;
import eu.jsparrow.core.renaming.RenamingTestHelper;

class UnusedFieldsEngineTest extends AbstractRulesTest {
	
	private static final String ROOT_PACKAGE_NAME = "eu.jsparrow.core";
	private static final String CORE_PACKAGE = "package " + ROOT_PACKAGE_NAME + ";";
	
	private static final String CLASS_WITH_UNUSED_FIELDS = CORE_PACKAGE + "\n"
			+ "public class UnusedFields {\n"
			+ "	\n"
			+ "	public String publicUnusedField = \"\";//1\n"
			+ "	public String publicFieldReassignedInternally = \"\";//2\n"
			+ "	public String publicFieldReassignedExternally = \"\";//3\n"
			+ "	public String publicFieldReassignedInternallyAndExternally = \"\";//4\n"
			+ "	\n"
			+ "	public String publicFieldUsedExternally = \"\";//5\n"
			+ "	public String publicFieldUsedInternally = \"\";//6\n"
			+ "	public String publicFieldUsedInternallyAndExternally = \"\";//7\n"
			+ "	\n"
			+ "	\n"
			+ "	protected String protectedUnusedField = \"\";//8\n"
			+ "	protected String protectedReassignedField = \"\";//9\n"
			+ "	\n"
			+ "	String packageProtectedUnusedField = \"\";//10\n"
			+ "	\n"
			+ "	private String privateUnusedField = \"\";//11\n"
			+ "	private String privateFieldReassignedInternally = \"\";//12\n"
			+ "	private String privateFieldUsedInternally = \"\";\n"
			+ "	\n"
			+ "	void reassignemnts() {\n"
			+ "		publicFieldReassignedInternally = \"value\";//13\n"
			+ "		publicFieldUsedInternallyAndExternally = \"new value\";//14\n"
			+ "		publicFieldReassignedInternallyAndExternally = \"\";//15\n"
			+ "		protectedReassignedField = \"\";//16\n"
			+ "	}\n"
			+ "	\n"
			+ "	void blackHole() {\n"
			+ "		System.out.println(publicFieldUsedInternally);\n"
			+ "		System.out.println(publicFieldUsedInternallyAndExternally);\n"
			+ "		System.out.println(privateFieldUsedInternally);\n"
			+ "	}\n"
			+ "\n"
			+ "}";
	private static final String CLASS_USING_FIELDS = CORE_PACKAGE + "\n"
			+ "public class PublicFieldsConsumer {\n"
			+ "\n"
			+ "	void consumingPublicFields() {\n"
			+ "		UnusedFields unusedFields = new UnusedFields();\n"
			+ "		System.out.println(unusedFields.publicFieldUsedExternally);\n"
			+ "		System.out.println(unusedFields.publicFieldUsedInternallyAndExternally);\n"
			+ "		unusedFields.publicFieldReassignedExternally = \"\";\n"
			+ "	}\n"
			+ "}";
	
	private static final String CLASS_REASSIGNING_FIELDS = CORE_PACKAGE + "\n"
			+ "public class PublicFieldReassignments extends UnusedFields {\n"
			+ "	\n"
			+ "	void reassigingPublicMethods() {\n"
			+ "		UnusedFields unusedFields = new UnusedFields();\n"
			+ "		unusedFields.protectedReassignedField = \"\";\n"
			+ "		unusedFields.publicFieldReassignedExternally = \"\";\n"
			+ "		unusedFields.publicFieldReassignedInternallyAndExternally = \"\";\n"
			+ "	}\n"
			+ "	\n"
			+ "	void reassignParentFields() {\n"
			+ "		publicFieldReassignedExternally = \"new value\";\n"
			+ "		publicFieldReassignedInternallyAndExternally = \"\";\n"
			+ "		protectedReassignedField = \"\";\n"
			+ "	}\n"
			+ "	\n"
			+ "	void reassignParentProtectedFields() {\n"
			+ "		protectedReassignedField = \"\";\n"
			+ "	}\n"
			+ "}";
	
	private UnusedFieldsEngine unusedFieldsEngine;
	private IPackageFragment packageFragment;
	private Map<String, String> compilationUnitNameContents;
	
	
	@BeforeEach
	void setUp() throws Exception {
		unusedFieldsEngine = new UnusedFieldsEngine("Project");
		packageFragment = root.createPackageFragment(ROOT_PACKAGE_NAME, true, null);
		Map<String, String> contents = new HashMap<>();
		contents.put("UnusedFields.java", CLASS_WITH_UNUSED_FIELDS);
		contents.put("PublicFieldReassignments.java", CLASS_REASSIGNING_FIELDS);
		contents.put("PublicFieldsConsumer.java", CLASS_USING_FIELDS);
		compilationUnitNameContents = Collections.unmodifiableMap(contents);
	}
	
	@Test
	void test_allAccessModifiers_shouldFind9Unused() throws Exception {
		Map<String, Boolean> options = new HashMap<String, Boolean>();
		options.put("private-fields", true);
		options.put("protected-fields", true);
		options.put("package-private-fields", true);
		options.put("public-fields", true);
		NullProgressMonitor progressMonitor = new NullProgressMonitor();
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 100);
		List<CompilationUnit> compilationUnits = RenamingTestHelper
				.loadCompilationUnitsFromString(packageFragment, compilationUnitNameContents);
		List<ICompilationUnit> icus = compilationUnits.stream()
				.map(cu -> (ICompilationUnit) cu.getJavaElement())
				.collect(Collectors.toList());
		
		List<UnusedFieldWrapper> unused = unusedFieldsEngine.findUnusedFields(icus , options, subMonitor);
		assertEquals(9, unused.size());
	}
	
	@Test
	void test_publicFields_shouldFindFourUnused() throws Exception {
		Map<String, Boolean> options = new HashMap<String, Boolean>();
		options.put("private-fields", false);
		options.put("protected-fields", false);
		options.put("package-private-fields", false);
		options.put("public-fields", true);
		NullProgressMonitor progressMonitor = new NullProgressMonitor();
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 100);
		List<CompilationUnit> compilationUnits = RenamingTestHelper
				.loadCompilationUnitsFromString(packageFragment, compilationUnitNameContents);
		List<ICompilationUnit> icus = compilationUnits.stream()
				.map(cu -> (ICompilationUnit) cu.getJavaElement())
				.collect(Collectors.toList());
		
		List<UnusedFieldWrapper> unused = unusedFieldsEngine.findUnusedFields(icus , options, subMonitor);
		assertFalse(unused.isEmpty());
	}

}
