package at.splendit.simonykees.core.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.Before;
import org.junit.Test;

public class EnumsWithoutEqualsASTVisitorTest {

	private EnumsWithoutEqualsASTVisitor visitor;

	@Test
	public void testASTRewriteExample() throws Exception {
		// create a new project
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		project.create(null);
		project.open(null);
		try {
			// set the Java nature and Java build path
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);

			IJavaProject javaProject = JavaCore.create(project);

			// build path is: project as source folder and JRE container
			IClasspathEntry[] cpentry = new IClasspathEntry[] { JavaCore.newSourceEntry(javaProject.getPath()),
					JavaRuntime.getDefaultJREContainerEntry() };
			javaProject.setRawClasspath(cpentry, javaProject.getPath(), null);
			Map options = new HashMap();
			options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
			options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
			javaProject.setOptions(options);

			// create a test file
			IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(project);
			IPackageFragment pack1 = root.createPackageFragment("test1", false, null);

			StringBuffer buf = new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.math.RoundingMode;\n");
			buf.append("public class E {\n");
			buf.append("    public void foo(int i) {\n");
			buf.append("        while (--i > 0) {\n");
			buf.append("            System.beep();\n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			ICompilationUnit cu = pack1.createCompilationUnit("E.java", buf.toString(), true, null);

			// create an AST
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(cu);
			parser.setResolveBindings(true);
			parser.setEnvironment(null, null, null, true);
			parser.setBindingsRecovery(true);
			CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
			AST ast = astRoot.getAST();

			// create the descriptive ast rewriter
			final ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());

			// get the block node that contains the statements in the method
			// body
			TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
			MethodDeclaration methodDecl = typeDecl.getMethods()[0];
			Block block = methodDecl.getBody();

			// create new statements to insert
			MethodInvocation newInv1 = ast.newMethodInvocation();
			newInv1.setName(ast.newSimpleName("bar1"));
			Statement newStatement1 = ast.newExpressionStatement(newInv1);

			MethodInvocation newInv2 = ast.newMethodInvocation();
			newInv2.setName(ast.newSimpleName("bar2"));
			Statement newStatement2 = ast.newExpressionStatement(newInv2);

			// describe that the first node is inserted as first statement in
			// block, the other one as last statement
			// note: AST is not modified by this
			ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			listRewrite.insertFirst(newStatement1, null);
			listRewrite.insertLast(newStatement2, null);

			// evaluate the text edits corresponding to the described changes.
			// AST and CU still unmodified.
			TextEdit res = rewrite.rewriteAST();

			// apply the text edits to the compilation unit
			Document document = new Document(cu.getSource());
			res.apply(document);
			cu.getBuffer().setContents(document.get());

			// test result
			String preview = cu.getSource();

			buf = new StringBuffer();
			buf.append("package test1;\n");
			buf.append("import java.math.RoundingMode;\n");
			buf.append("public class E {\n");
			buf.append("    public void foo(int i) {\n");
			buf.append("        bar1();\n");
			buf.append("        while (--i > 0) {\n");
			buf.append("            System.beep();\n");
			buf.append("        }\n");
			buf.append("        bar2();\n");
			buf.append("    }\n");
			buf.append("}\n");
			assertEquals(preview, buf.toString());
		} finally {
			project.delete(true, null);
		}
	}

	@Test
	public void testASTRewriteExampleWithVisitor() throws Exception {
		// create a new project
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		project.create(null);
		project.open(null);
		try {
			// set the Java nature and Java build path
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);

			IJavaProject javaProject = JavaCore.create(project);

			// build path is: project as source folder and JRE container
			IClasspathEntry[] cpentry = new IClasspathEntry[] { JavaCore.newSourceEntry(javaProject.getPath()),
					JavaRuntime.getDefaultJREContainerEntry() };
			javaProject.setRawClasspath(cpentry, javaProject.getPath(), null);
			Map options = new HashMap();
			options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
			options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
			javaProject.setOptions(options);

			// create a test file
			IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(project);
			IPackageFragment pack1 = root.createPackageFragment("test1", false, null);

			StringBuffer buf = new StringBuffer();

			ICompilationUnit cu = pack1.createCompilationUnit("TestClass.java", buf.toString(), false, null);

			// create an AST
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setSource(cu);
			parser.setResolveBindings(false);
			CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
			astRoot.recordModifications();

			AST ast = astRoot.getAST();

			PackageDeclaration pd = ast.newPackageDeclaration();
			Name astName = ast.newName("test1");
			pd.setName(astName);
			astRoot.setPackage(pd);

			ImportDeclaration im = ast.newImportDeclaration();
			im.setName(ast.newName("java.math.RoundingMode"));
			astRoot.imports().add(im);

			TypeDeclaration td = ast.newTypeDeclaration();
			td.setInterface(false);
			td.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
			td.setName(ast.newSimpleName("TestClass"));
			astRoot.types().add(td);

			MethodDeclaration md = ast.newMethodDeclaration();
			md.setName(ast.newSimpleName("TestMethod"));
			td.bodyDeclarations().add(md);

			String old = "RoundingMode roundingMode;\n " + "if(roundingMode.equals(RoundingMode.UP)){}";

			ASTNode convertedAstNodeWithMethodBody = ASTNode.copySubtree(ast, createBlockFromString(old));
			Block block = (Block) convertedAstNodeWithMethodBody;
			md.setBody(block);

			Document document = new Document(cu.getSource());
			TextEdit res = astRoot.rewrite(document, options);
			res.apply(document);
			cu.getBuffer().setContents(document.get());

			ASTParser parser2 = ASTParser.newParser(AST.JLS8);
			parser2.setSource(cu);
			parser2.setResolveBindings(true);

			astRoot = (CompilationUnit) parser2.createAST(null);

			final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

			visitor.setAstRewrite(astRewrite);

			astRoot.accept(visitor);

			TextEdit edit = astRewrite.rewriteAST();
			boolean hasEdits = !Arrays.asList(edit.getChildren()).isEmpty();
			assertTrue(hasEdits);

			apply(edit, cu);

			String src = cu.getSource();
			ASTParser parser3 = ASTParser.newParser(AST.JLS8);
			parser3.setSource(cu);
			parser3.setResolveBindings(true);

			CompilationUnit newAstRoot = (CompilationUnit) parser3.createAST(null);

			SearchInfixVisitor visi = new SearchInfixVisitor();
			newAstRoot.accept(visi);
			InfixExpression ifi = visi.getFound();
			assertNotNull(ifi);
			String testing = ifi.toString();
			assertEquals(InfixExpression.Operator.EQUALS, ifi.getOperator());

		} finally {
			project.delete(true, null);
		}
	}

	@Before
	public void setUp() {
		visitor = new EnumsWithoutEqualsASTVisitor();
	}

	private void apply(final ASTRewrite astRewrite, ICompilationUnit cu)
			throws BadLocationException, JavaModelException {
		Document document = new Document(cu.getSource());
		TextEdit edit = astRewrite.rewriteAST(document, null);
		edit.apply(document);
		cu.getBuffer().setContents(document.get());
	}

	private void apply(final TextEdit edit, ICompilationUnit cu) throws BadLocationException, JavaModelException {
		Document document = new Document(cu.getSource());
		edit.apply(document);
		cu.getBuffer().setContents(document.get());
	}

	private Block createBlockFromString(String string) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setSource(string.toCharArray());
		astParser.setKind(ASTParser.K_STATEMENTS);
		return (Block) astParser.createAST(null);
	}

	private class SearchVisitor extends ASTVisitor {

		private MethodInvocation found;

		@Override
		public boolean visit(MethodInvocation mi) {
			found = mi;
			return true;
		}

		public MethodInvocation getFound() {
			return found;
		}
	}

	private class SearchInfixVisitor extends ASTVisitor {

		private InfixExpression found;

		@Override
		public boolean visit(InfixExpression mi) {
			found = mi;
			return true;
		}

		public InfixExpression getFound() {
			return found;
		}
	}

	@Test
	public void demoCodeCreateAST() {
		final AST target = AST.newAST(AST.JLS8);

		final ASTRewrite astRewrite = ASTRewrite.create(target);
		CompilationUnit cu = target.newCompilationUnit();
		PackageDeclaration pd = target.newPackageDeclaration();
		cu.setPackage(pd);

		ImportDeclaration im = target.newImportDeclaration();
		cu.imports().add(im);

		TypeDeclaration td = target.newTypeDeclaration();
		cu.types().add(td);
		Javadoc javadoc = target.newJavadoc();
		td.setJavadoc(javadoc);
		TagElement tg = target.newTagElement();
		javadoc.tags().add(tg);
		tg.fragments().add(target.newTextElement());
		tg.fragments().add(target.newMemberRef());
		MethodRef mr = target.newMethodRef();
		tg.fragments().add(mr);
		mr.parameters().add(target.newMethodRefParameter());

		VariableDeclarationFragment variableDeclarationFragment = target.newVariableDeclarationFragment();
		FieldDeclaration fd = target.newFieldDeclaration(variableDeclarationFragment);
		td.bodyDeclarations().add(fd);

		Initializer in = target.newInitializer();
		td.bodyDeclarations().add(in);

		MethodDeclaration md = target.newMethodDeclaration();
		SingleVariableDeclaration singleVariableDeclaration = target.newSingleVariableDeclaration();
		md.parameters().add(singleVariableDeclaration);
		td.bodyDeclarations().add(md);

		MethodInvocation inv = target.newMethodInvocation();
		ExpressionStatement expressionStatement2 = target.newExpressionStatement(inv);

		// visitor.visit(inv);
	}

}
