package eu.jsparrow.core.markers;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class CoreRefactoringEventManagerTest extends UsesJDTUnitFixture {
	
	private CoreRefactoringEventManager eventManager;
	
	@BeforeEach
	void setUp() throws Exception {
		eventManager = new CoreRefactoringEventManager();
		defaultFixture.addImport(java.util.Comparator.class.getName());
	}
	
	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_eventGenerator_shouldCreateEvents() throws Exception {
		String method = ""
				+ "void testMethod() {\n"
				+ "	Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);\n"
				+ "}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, method);
		ICompilationUnit icu = defaultFixture.getICompilationUnit();
		eventManager.discoverRefactoringEvents(icu,  Arrays.asList("LambdaToMethodReferenceResolver", "UseComparatorMethodsResolver"));
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(2, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				()-> assertEquals("Use predefined comparator", event.getName()),
				()-> assertEquals(136, event.getOffset()),
				()-> assertEquals(32, event.getLength()),
				()-> assertEquals("UseComparatorMethodsResolver", event.getResolver()),
				()-> assertEquals("Comparator.naturalOrder()", event.getCodePreview()),
				()-> assertEquals("Lambda expression can be replaced with predefined comparator", event.getMessage()),
				()-> assertEquals(5, event.getWeightValue()));
		
		RefactoringMarkerEvent event2 = events.get(1);
		assertAll(
				()-> assertEquals("Replace lambda expression with method reference", event2.getName()),
				()-> assertEquals("LambdaToMethodReferenceResolver", event2.getResolver()),
				()-> assertEquals("Simplify the lambda expression by using a method reference.", event2.getMessage()));
	}

	@Test
	void test_resolveEvents_shouldResolveEvents() throws Exception {
		String method = ""
				+ "void testMethod() {\n"
				+ "	Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);\n"
				+ "}";
		String expected = ""
				+ "package fixturepackage;\n"
				+ "import java.util.Comparator;\n"
				+ "class TestCU {\n"
				+ "  void testMethod(){\n"
				+ "    Comparator<Integer> comparator=Comparator.naturalOrder();\n"
				+ "  }\n"
				+ "}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, method);
		ICompilationUnit icu = defaultFixture.getICompilationUnit();
		/*
		 * Just count the offset. Or run discoverEvents to figure it out. 
		 */
		eventManager.resolve(icu, "UseComparatorMethodsResolver", 136);
		
		String newSource = icu.getSource();
		assertMatch(
				ASTNodeBuilder.createCompilationUnitFromString(expected),
				ASTNodeBuilder.createCompilationUnitFromString(newSource));
	}
	
	@Test
	void test_resolveSingleMarker_shouldResolveMarker() throws Exception {
		String method = ""
				+ "void testMethod() {\n"
				+ "	Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);\n"
				+ "	Comparator<Integer> comparator2 = (lhs, rhs) -> lhs.compareTo(rhs);\n"
				+ "}";
		String expected = ""
				+ "package fixturepackage;\n"
				+ "import java.util.Comparator;\n"
				+ "class TestCU {\n"
				+ "  void testMethod(){\n"
				+ "    Comparator<Integer> comparator=Comparator.naturalOrder();\n"
				+ "    Comparator<Integer> comparator2 = (lhs, rhs) -> lhs.compareTo(rhs);\n"
				+ "  }\n"
				+ "}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, method);
		ICompilationUnit icu = defaultFixture.getICompilationUnit();
		/*
		 * Just count the offset. Or run discoverEvents to figure it out. 
		 */
		eventManager.resolve(icu, "UseComparatorMethodsResolver", 136);
		
		String newSource = icu.getSource();

		assertMatch(
				ASTNodeBuilder.createCompilationUnitFromString(expected),
				ASTNodeBuilder.createCompilationUnitFromString(newSource));
	}

}
