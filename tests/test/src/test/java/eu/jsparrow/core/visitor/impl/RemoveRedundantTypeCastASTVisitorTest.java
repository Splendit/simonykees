package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.jdtunit.util.MethodDeclarationBuilder;

@SuppressWarnings("nls")
public class RemoveRedundantTypeCastASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private RemoveRedundantTypeCastASTVisitor visitor;

	@BeforeEach
	public void setUp() {
		visitor = new RemoveRedundantTypeCastASTVisitor();
	}

	@Test
	public void visit_CastStringLiteralToString_shouldTransform() throws Exception {
		String before = "((String)\"HelloWorld\").charAt(0);";
		String afterExpected = "\"HelloWorld\".charAt(0);";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_CastStringVariableToString_shouldTransform() throws Exception {
		String before = "String helloWorld = \"HelloWorld\";\n" +
				"((String)helloWorld).charAt(0);";
		String afterExpected = "String helloWorld = \"HelloWorld\";\n" +
				"helloWorld.charAt(0);";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_CastSubstringInvocationToString_shouldTransform() throws Exception {
		String before = "((String)\"HelloWorld\".substring(0)).charAt(0);";
		String afterExpected = "\"HelloWorld\".substring(0).charAt(0);";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_CastIntLiteralToInt_shouldTransform() throws Exception {
		String before = "int i = 2;\n" +
				"int j = ((int) 2) * i;";
		String afterExpected = "int i = 2;\n" +
				"int j = 2 * i;";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_CastIntPreIncrementToInt_shouldTransform() throws Exception {
		String before = "int i = 1;\n" +
				"int j = ((int)++i);";
		String afterExpected = "int i = 1;\n" +
				"int j = ++i;";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_CastIntLeftSummandToInt_shouldTransform() throws Exception {
		String before = "int i = 1;\n" +
				"int j = ((int)i + 2);";
		String afterExpected = "int i = 1;\n" +
				"int j = (i + 2);";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_CastLongLiteralToLong_shouldTransform() throws Exception {
		String before = "long x = (long)1L;";
		String afterExpected = "long x = 1L;";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_CastParenthesizedAdditionWithLongResult_shouldTransform() throws Exception {
		String before = "long x = (long)(1 + 100L);";
		String afterExpected = "long x = 1 + 100L;";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_RecursiveExampleWithLong_shouldTransform() throws Exception {
		String before = "long x = ((((long)((long)((100 + 200L)) + 300))));";
		String afterExpected = "long x = ((100 + 200L)) + 300;";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_CastParenthesizedLongAdditionAssignment_shouldTransform() throws Exception {
		String before = "long x = 100L;\n" +
				"x = ((long)(x += 200L));";
		String afterExpected = "long x = 100L;\n" +
				"x = x += 200L;";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(afterExpected), methodBlock);
	}

	@Test
	public void visit_CastStringLiteralToCharSequence_shouldNotTransform() throws Exception {
		String before = "((CharSequence)\"xyz\").length();";
		
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_CastCharSequenceVariableToString_shouldNotTransform() throws Exception {
		String before = "CharSequence sequence = \"HelloWorld!\";\n" +
				"((String)sequence).contains(\"World\");";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_CastIntLiteralToLong_shouldNotTransform() throws Exception {
		String before = "long x = (long)1;";

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_CastListOfStringToListOfString_shouldTransform() throws Exception {
		String before = "List<String> l = new ArrayList<>();\n" +
				"((List<String>)l).add(\"value1\");";
		String after = "List<String> l = new ArrayList<>();\n" +
				"l.add(\"value1\");";
		
		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(after), methodBlock);
	}

	@Test
	public void visit_CastListOfJokerTypeToListOfJokerType_shouldNotTransform() throws Exception {
		String before = "List<?> l = new ArrayList<>();" +
				"((List<?>)l).size();";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_Cast2DListOfStringTo2DListOfString_shouldTransform() throws Exception {
		String before = "List<List<String>> l = new ArrayList<>();" +
				"((List<List<String>>)l).add(new ArrayList<String>());";
		String after = "List<List<String>> l = new ArrayList<>();" +
				"l.add(new ArrayList<String>());";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(after), methodBlock);
	}

	@Test
	public void visit_CastRawtypeListToListOfString_shouldNotTransform() throws Exception {
		String before = "List l = new ArrayList<>();\n" +
				"((List<String>)l).add(\"value1\");";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_CastStringArrayVarToStringArray_shouldTransform() throws Exception {
		String before = "String[] stringArr = new String [] {\"value 1\", \"value 2\"};\n" +
				"((String[])stringArr)[0].contains(\"1\");";
		String after = "String[] stringArr = new String [] {\"value 1\", \"value 2\"};\n" +
				"stringArr[0].contains(\"1\");";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(after), methodBlock);
	}

	@Test
	public void visit_CastObjectArrayVarToStringArray_shouldNotTransform() throws Exception {
		String before = "Object[] objArr = new String[] { \"value 1\", \"value 2\" };\n" +
				"((String[]) objArr)[0].contains(\"1\");";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_CastStringArrayVarToObjectArray_shouldNotTransform() throws Exception {
		String before = "String[] stringArr = new String [] {\"value 1\", \"value 2\"};\n" +
				"((Object[])stringArr)[0].toString();";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_CastObjectArrayVarToObject_shouldNotTransform() throws Exception {
		String before = "Object [] objArr = new Object[] {};\n" +
				"((Object)objArr).toString();";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);

	}

	@Test
	public void visit_CastLambdaExpressionArgument_shouldTransform() throws Exception {
		String before = "usingSupplier((Supplier<String>)() -> \"\");";
		String after = "usingSupplier(() -> \"\");";

		MethodDeclarationBuilder.factory(fixture, "usingSupplier")
			.withParameterizedTypeParameter(Supplier.class.getSimpleName(), String.class.getSimpleName());

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addImport(Supplier.class.getName());

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();

		assertMatch(ASTNodeBuilder.createBlockFromString(after), methodBlock);
	}

	@Test
	public void visit_CastLambdaExpressionInitialization_shouldTransform() throws Exception {
		String before = "Supplier<String> supplier = (Supplier<String>) () -> \"\";";
		String after = "Supplier<String> supplier = () -> \"\";";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addImport(Supplier.class.getName());

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();

		assertMatch(ASTNodeBuilder.createBlockFromString(after), methodBlock);
	}

	@Test
	public void visit_CastLambdaExpressionInAssignment_shouldTransform() throws Exception {
		String before = "Supplier<String> supplier;\n"
				+ "supplier = (Supplier<String>) () -> \"\";";
		String after = "Supplier<String> supplier;\n"
				+ "supplier = () -> \"\";";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addImport(Supplier.class.getName());

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();

		assertMatch(ASTNodeBuilder.createBlockFromString(after), methodBlock);
	}

	@Test
	public void visit_CastLambdaExpressionArgument_shouldNotTransform() throws Exception {
		String before = "usingObject((Supplier<String>)() -> \"\");";

		MethodDeclarationBuilder.factory(fixture, "usingObject")
			.withSimpleTypeParameter(Object.class.getSimpleName());

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addImport(Supplier.class.getName());

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();

		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

	@Test
	public void visit_CastLambdaExpressionInitialization_shouldNotTransform() throws Exception {
		String before = "Object object = (Supplier<String>) () -> \"\";";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addImport(Supplier.class.getName());

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();

		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);

	}

	@Test
	public void visit_CastLambdaExpressionInAssignment_shouldNotTransform() throws Exception {
		String before = "Object object;\n"
				+ "object = (Supplier<String>) () -> \"\";";

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addImport(Supplier.class.getName());

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();

		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);

	}

	@Test
	public void visit_CastLambdaExpressionOneVarArgs_shouldTransform() throws Exception {
		String before = "usingSupplier((Supplier<String>)() -> \"\");";
		String after = "usingSupplier(() -> \"\");";

		MethodDeclarationBuilder.factory(fixture, "usingSupplier")
			.withParameterizedTypeParameter(Supplier.class.getSimpleName(), String.class.getSimpleName())
			.withVarArgs();

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addImport(Supplier.class.getName());

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();

		assertMatch(ASTNodeBuilder.createBlockFromString(after), methodBlock);
	}

	@Test
	public void visit_CastLambdaExpressionTwoVarArgs_shouldTransform() throws Exception {
		String before = "usingSupplier((Supplier<String>)() -> \"\", (Supplier<String>)() -> \"\");";
		String after = "usingSupplier(() -> \"\", () -> \"\");";

		MethodDeclarationBuilder.factory(fixture, "usingSupplier")
			.withParameterizedTypeParameter(Supplier.class.getSimpleName(), String.class.getSimpleName())
			.withVarArgs();

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addImport(Supplier.class.getName());

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();

		assertMatch(ASTNodeBuilder.createBlockFromString(after), methodBlock);
	}

	@Test
	public void visit_CastPrimitiveIntVarArgs_shouldTransform() throws Exception {

		String before = "usingIntVarargs((int)1, (int)2, (int)3);";
		String after = "usingIntVarargs(1, 2, 3);";

		MethodDeclarationBuilder.factory(fixture, "usingIntVarargs")
			.withSimpleTypeParameter(int.class.getSimpleName())
			.withVarArgs();

		fixture.addImport(List.class.getName());
		fixture.addImport(ArrayList.class.getName());
		fixture.addImport(Supplier.class.getName());

		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();

		assertMatch(ASTNodeBuilder.createBlockFromString(after), methodBlock);
	}
	
	@Test
	public void visit_lambdaAsMethodInvocationExpression_shouldNotTransform() throws Exception {
		String before = ""+ 
				"		Runnable r = () -> {};\n" + 
				"		((Runnable)() -> {}).run();";
		fixture.addMethodBlock(before);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		Block methodBlock = fixture.getMethodBlock();
		assertMatch(ASTNodeBuilder.createBlockFromString(before), methodBlock);
	}

}
