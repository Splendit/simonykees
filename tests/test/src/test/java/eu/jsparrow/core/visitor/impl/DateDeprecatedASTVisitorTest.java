package eu.jsparrow.core.visitor.impl;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({ "nls" })
public class DateDeprecatedASTVisitorTest extends UsesSimpleJDTUnitFixture {

	public static Stream<Arguments> createDates() {
		return Stream.of(Arguments.of("99.00, 1, 1", "1900 + 99.00, 1, 1" ),
				Arguments.of("100, 1, 1", "2000, 1, 1"),
				Arguments.of("99, 1, 1, 1, 1", "1999, 1, 1, 1, 1" ),
				Arguments.of("99, 1, 1, 1, 1, 1", "1999, 1, 1, 1, 1, 1" ),
				Arguments.of("99, 1, 1", "1999, 1, 1" )
				);
	}

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new DateDeprecatedASTVisitor());
		fixture.addImport("java.util.Date");
	}

	@ParameterizedTest
	@MethodSource("createDates")
	public void visit_newDate_YMD(String dateConfigPre, String dateConfigPost) throws Exception {
		String original = "Date d = new Date(" + dateConfigPre + ");";
		String expected = "Calendar calendar = Calendar.getInstance(); calendar.set(" + dateConfigPost + "); Date d = calendar.getTime();";
		assertChange(original, expected);
		
		ImportDeclaration expectedAddedImport = fixture.getAstRewrite()
			.getAST()
			.newImportDeclaration();
		expectedAddedImport.setName(fixture.getAstRewrite()
			.getAST()
			.newName("java.util.Calendar"));
		ASTMatcher astMatcher = new ASTMatcher();
		assertTrue(fixture.getImports()
			.stream()
			.anyMatch(i -> astMatcher.match(i, expectedAddedImport)));
	}

	@ParameterizedTest
	@MethodSource("createDates")
	public void visit_newDateExp_YMD(String dateConfigPre, String dateConfigPost) throws Exception {
		String original = "new Date(" + dateConfigPre + ");";
		String expected = "Calendar calendar = Calendar.getInstance(); calendar.set(" + dateConfigPost + "); calendar.getTime();";
		assertChange(original, expected);
		
		ImportDeclaration expectedAddedImport = fixture.getAstRewrite()
			.getAST()
			.newImportDeclaration();
		expectedAddedImport.setName(fixture.getAstRewrite()
			.getAST()
			.newName("java.util.Calendar"));
		ASTMatcher astMatcher = new ASTMatcher();
		assertTrue(fixture.getImports()
			.stream()
			.anyMatch(i -> astMatcher.match(i, expectedAddedImport)));
	}

	@ParameterizedTest
	@MethodSource("createDates")
	public void visit_newDate_YMD_extra_statement(String dateConfigPre, String dateConfigPost) throws Exception {
		String original = "new String(\"Hellow\"); Date d = new Date(" + dateConfigPre + ");";
		String expected = "new String(\"Hellow\"); Calendar calendar = Calendar.getInstance(); calendar.set("
				+ dateConfigPost + "); Date d = calendar.getTime();";
		assertChange(original, expected);
				
		ImportDeclaration expectedAddedImport = fixture.getAstRewrite()
			.getAST()
			.newImportDeclaration();
		expectedAddedImport.setName(fixture.getAstRewrite()
			.getAST()
			.newName("java.util.Calendar"));
		ASTMatcher astMatcher = new ASTMatcher();
		assertTrue(fixture.getImports()
			.stream()
			.anyMatch(i -> astMatcher.match(i, expectedAddedImport)));
	}

	@ParameterizedTest
	@MethodSource("createDates")
	public void visit_newDate_YMD_single_statement(String dateConfigPre, String dateConfigPost) throws Exception {
		String original = "if(true) new Date(" + dateConfigPre + ");";
		String expected = "if(true){ Calendar calendar = Calendar.getInstance(); calendar.set(" + dateConfigPost + "); calendar.getTime();}";
		assertChange(original, expected);
		
		ImportDeclaration expectedAddedImport = fixture.getAstRewrite()
			.getAST()
			.newImportDeclaration();
		expectedAddedImport.setName(fixture.getAstRewrite()
			.getAST()
			.newName("java.util.Calendar"));
		ASTMatcher astMatcher = new ASTMatcher();
		assertTrue(fixture.getImports()
			.stream()
			.anyMatch(i -> astMatcher.match(i, expectedAddedImport)));
	}
	
	@ParameterizedTest
	@MethodSource("createDates")
	public void visit_newDate_YMD_avoidNameConflict(String dateConfigPre, String dateConfigPost) throws Exception {
		String original = "int calendar; double calendar2; Date calendar1 = new Date(" + dateConfigPre + ");";
		String expected = "int calendar; double calendar2; Calendar calendar3 = Calendar.getInstance(); calendar3.set(" + dateConfigPost + "); Date calendar1 = calendar3.getTime();";

		assertChange(original, expected);
	}
	
	@ParameterizedTest
	@MethodSource("createDates")
	public void visit_newDate_YMD_multiIntroducedNames(String dateConfigPre, String dateConfigPost) throws Exception {
		String original = "Date d = new Date(" + dateConfigPre + "); int calendar1; Date d2 = new Date(" + dateConfigPre + ");";
		String expected =
				"Calendar calendar = Calendar.getInstance(); "
				+ "calendar.set(" + dateConfigPost + "); "
				+ "Date d = calendar.getTime();"
				+ "int calendar1;"
				+ "Calendar calendar2 = Calendar.getInstance();"
				+ "calendar2.set(" + dateConfigPost +");"
				+ "Date d2 = calendar2.getTime();";

		assertChange(original, expected);
	}
	
	@Test
	public void visit_newDate_noParameters() throws Exception {
		assertNoChange("Date date = new Date();");
	}
}
