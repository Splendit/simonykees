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
				"Calendar cal = Calendar.getInstance(); cal.set(" + dateConfigPost + "); Date d = cal.getTime();");
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
				"Calendar cal = Calendar.getInstance(); cal.set(" + dateConfigPost + "); cal.getTime();");
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

		Block expected = createBlock("new String(\"Hellow\"); Calendar cal = Calendar.getInstance(); cal.set("
				+ dateConfigPost + "); Date d = cal.getTime();");
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
				"if(true){ Calendar cal = Calendar.getInstance(); cal.set(" + dateConfigPost + "); cal.getTime();}");
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
}
