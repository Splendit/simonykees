package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

class ReplaceStringFormatByFormattedASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setJavaVersion(JavaCore.VERSION_15);
		setVisitor(new ReplaceStringFormatByFormattedASTVisitor());
	}

	@Test
	void visit_StringFormatInvocation_shouldTransform() throws Exception {
		String original = "" +
				"		String name = \"<name>\";\n"
				+ "		String phone = \"<phone>\";\n"
				+ "		String address = \"<address>\";\n"
				+ "		float salary = 10000.0f;\n"
				+ "\n"
				+ "		String output = String.format(\n"
				+ "				\"Name: %s, Phone: %s, Address: %s, Salary: $%.2f\",\n"
				+ "				name, phone, address, salary);";

		String expected = "" +
				"		String name = \"<name>\";\n"
				+ "		String phone = \"<phone>\";\n"
				+ "		String address = \"<address>\";\n"
				+ "		float salary = 10000.0f;\n"
				+ "\n"
				+ "		String output = \"Name: %s, Phone: %s, Address: %s, Salary: $%.2f\".formatted(name, phone, address, salary);\n"
				+ "";

		assertChange(original, expected);
	}

	@Test
	void visit_NoObjectArguments_shouldTransform() throws Exception {
		String original = "String output = String.format(\"line-1 %nline-2 %nline-3\");";
		String expected = "String output = \"line-1 %nline-2 %nline-3\".formatted();";

		assertChange(original, expected);
	}

	@Test
	void visit_MethodBindingNotResolved_shouldNotTransform() throws Exception {
		String original = "String output = format(\"line-1 %nline-2 %nline-3\");";

		assertNoChange(original);
	}

	@Test
	void visit_MethodNameFormatted_shouldNotTransform() throws Exception {
		String original = ""
				+ "		String output = \"line-1 %nline-2 %nline-3\";\n"
				+ "		output = output.formatted();";

		assertNoChange(original);
	}

	@Test
	void visit_LocaleAsFirstArgument_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.Locale.class.getName());
		String original = "" +
				"		String name = \"<name>\";\n"
				+ "		String phone = \"<phone>\";\n"
				+ "		String address = \"<address>\";\n"
				+ "		float salary = 10000.0f;\n"
				+ "\n"
				+ "		String output = String.format(\n"
				+ "				Locale.ENGLISH,\n"
				+ "				\"Name: %s, Phone: %s, Address: %s, Salary: $%.2f\",\n"
				+ "				name, phone, address, salary);";

		assertNoChange(original);
	}

	@Test
	void visit_FormatIsNoStringMethod_shouldNotTransform() throws Exception {
		fixture.addMethodDeclarationFromString(""
				+ "	String format(String format, Object... args) {\n"
				+ "		return \"\";\n"
				+ "	}");
		String original = "" +
				"		String name = \"<name>\";\n"
				+ "		String phone = \"<phone>\";\n"
				+ "		String address = \"<address>\";\n"
				+ "		float salary = 10000.0f;\n"
				+ "\n"
				+ "		String output = format(\"Name: %s, Phone: %s, Address: %s, Salary: $%.2f\", name, phone,\n"
				+ "				address, salary);";

		assertNoChange(original);
	}

	@Test
	void visit_ConcatenatedFormatString_shouldNotTransform() throws Exception {
		String original = "" +
				"		String name = \"<name>\";\n"
				+ "		String phone = \"<phone>\";\n"
				+ "		String address = \"<address>\";\n"
				+ "		float salary = 10000.0f;\n"
				+ "\n"
				+ "		String output = String.format(\"Name: %s,\" + \" Phone: %s,\" + \" Address: %s,\" + \" Salary: $%.2f\", name, phone,\n"
				+ "				address, salary);";

		assertNoChange(original);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"String.format(null, null);", "String.format(null);", "String.format();"})
	void visit_nullArguments_shouldNotTransform(String original) throws Exception {
		assertNoChange(original);
	}
}