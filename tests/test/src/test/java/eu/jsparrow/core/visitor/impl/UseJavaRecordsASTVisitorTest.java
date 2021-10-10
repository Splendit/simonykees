package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.java16.javarecords.UseJavaRecordsASTVisitor;

@SuppressWarnings("nls")
public class UseJavaRecordsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setJavaVersion(JavaCore.VERSION_16);
		setDefaultVisitor(new UseJavaRecordsASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/**
	 * SIM-2017: This test is expected to fail as soon as
	 * {@link UseJavaRecordsASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)}
	 * is implemented.
	 */
	@Test
	public void visit_LocalClassToRecord_research() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n" +
				"		class Point {\n" +
				"			private final int x;\n" +
				"			private final int y;\n" +
				"\n" +
				"			Point(int x, int y) {\n" +
				"				this.x = x;\n" +
				"				this.y = y;\n" +
				"			}\n" +
				"\n" +
				"			public int x() {\n" +
				"				return x;\n" +
				"			}\n" +
				"\n" +
				"			public int y() {\n" +
				"				return y;\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}
}
