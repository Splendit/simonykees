package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings({ "nls" })
@RunWith(Parameterized.class)
public class DateDeprecatedASTVisitorTest extends UsesJDTUnitFixture {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "99, 1, 1", "1999, 1, 1" }, { "100, 1, 1", "2000, 1, 1" },
				{ "99, 1, 1, 1, 1", "1999, 1, 1, 1, 1" }, { "99, 1, 1, 1, 1, 1", "1999, 1, 1, 1, 1, 1" } });
	}

	private String dateConfigPre;
	private String dateConfigPost;

	public DateDeprecatedASTVisitorTest(String dateConfigPre, String dateConfigPost) {
		this.dateConfigPre = dateConfigPre;
		this.dateConfigPost = dateConfigPost;
	}

	private DateDeprecatedASTVisitor visitor;

	@Before
	public void setUp() throws Exception {
		visitor = new DateDeprecatedASTVisitor();
		fixture.addImport("java.util.Date");
	}

	@Test
	public void visit_newDate_YMD() throws Exception {
		fixture.addMethodBlock("Date d = new Date(" + dateConfigPre + ");");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock(
				"Calendar calendar = Calendar.getInstance(); calendar.set(" + dateConfigPost + "); Date d = calendar.getTime();");
		assertMatch(expected, fixture.getMethodBlock());
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

	@Test
	public void visit_newDateExp_YMD() throws Exception {
		fixture.addMethodBlock("new Date(" + dateConfigPre + ");");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock(
				"Calendar calendar = Calendar.getInstance(); calendar.set(" + dateConfigPost + "); calendar.getTime();");
		assertMatch(expected, fixture.getMethodBlock());
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

	@Test
	public void visit_newDate_YMD_extra_statement() throws Exception {
		fixture.addMethodBlock("new String(\"Hellow\"); Date d = new Date(" + dateConfigPre + ");");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock("new String(\"Hellow\"); Calendar calendar = Calendar.getInstance(); calendar.set("
				+ dateConfigPost + "); Date d = calendar.getTime();");
		assertMatch(expected, fixture.getMethodBlock());
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

	@Test
	public void visit_newDate_YMD_single_statement() throws Exception {
		fixture.addMethodBlock("if(true) new Date(" + dateConfigPre + ");");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block expected = createBlock(
				"if(true){ Calendar calendar = Calendar.getInstance(); calendar.set(" + dateConfigPost + "); calendar.getTime();}");
		assertMatch(expected, fixture.getMethodBlock());
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
	
	@Test
	public void visit_newDate_YMD_avoidNameConflict() throws Exception {
		fixture.addMethodBlock("int calendar; double calendar2; Date calendar1 = new Date(" + dateConfigPre + ");");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock(
				"int calendar; double calendar2; Calendar calendar3 = Calendar.getInstance(); calendar3.set(" + dateConfigPost + "); Date calendar1 = calendar3.getTime();");
		assertMatch(expected, fixture.getMethodBlock());
	}
	
	@Test
	public void visit_newDate_YMD_multiIntroducedNames() throws Exception {
		fixture.addMethodBlock("Date d = new Date(" + dateConfigPre + "); int calendar1; Date d2 = new Date(" + dateConfigPre + ");");
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expected = createBlock(
				"Calendar calendar = Calendar.getInstance(); "
				+ "calendar.set(" + dateConfigPost + "); "
				+ "Date d = calendar.getTime();"
				+ "int calendar1;"
				+ "Calendar calendar2 = Calendar.getInstance();"
				+ "calendar2.set(" + dateConfigPost +");"
				+ "Date d2 = calendar2.getTime();");
		assertMatch(expected, fixture.getMethodBlock());
	}
}
