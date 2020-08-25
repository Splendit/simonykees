package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.IPackageFragment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.JdtUnitFixtureClass;

public class UseOffsetBasedStringMethodsExtendedASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {

		// Does not work
		IPackageFragment additionalPackage = fixtureProject.addPackageFragment("additionalPackage");
		JdtUnitFixtureClass additionalCompilationUnit = fixtureProject.addCompilationUnit(additionalPackage,
				"AdditionalClass");
		additionalCompilationUnit.addTypeDeclarationFromString("AdditionalClass",
				"public class AdditionalClass {\n"
						+ "	public static void max() {}\n"
						+ "	}");

		setDefaultVisitor(new UseOffsetBasedStringMethodsASTVisitor());

		// Something like the following maybe works
		// but the problem is that the version "3.20.0-SNAPSHOT" is not constant
		// and must be changed at each new release.
		// addDependency("at.splendit", "eu.jsparrow.sample",
		// "3.20.0-SNAPSHOT");
		//
		// maybe we could create a MAVEN project with sample classes, having a
		// constant value for version, and could add it as a dependency like the
		// following
		// addDependency("at.splendit", "eu.jsparrow.project4test", "1.0.0")

	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_StaticImportOnDemandClashing_shouldTransform() throws Exception {

		defaultFixture.addImport("additionalPackage.AdditionalClass", true, true);
		String original = "" +
				"	void test() {" +
				"		String str = \"Hello World!\";\n" +
				"		int index = str.substring(6).indexOf(\"d\");" +
				"	}";
		String expected = "" +
				"	void test() {" +
				"		String str = \"Hello World!\";\n" +
				"		int index=Math.max(str.indexOf(\"d\",6) - 6,-1);" +
				"	}";

		assertChange(original, expected);
	}

}
