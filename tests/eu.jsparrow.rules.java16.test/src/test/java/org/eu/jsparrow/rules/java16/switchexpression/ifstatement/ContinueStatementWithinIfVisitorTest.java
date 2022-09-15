package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ContinueStatementWithinIfVisitor;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;

class ContinueStatementWithinIfVisitorTest extends UsesJDTUnitFixture {

	static List<ContinueStatement> collectContinueStatements(Statement statement) {
		List<ContinueStatement> contineStatements = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(ContinueStatement node) {
				contineStatements.add(node);
				return true;
			}

		};
		statement.accept(visitor);
		return contineStatements;
	}

	@BeforeEach
	void setUp() {
		setDefaultVisitor(new ReplaceMultiBranchIfBySwitchASTVisitor());
		setJavaVersion(JavaCore.VERSION_14);
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_continueForStatementEnclosingIfStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void continueForStatementEnclosingIfStatement(int value) {\n"
				+ "		for (;;) {\n"
				+ "			if (value == 1) {\n"
				+ "				continue;\n"
				+ "			} else if (value == 2) {\n"
				+ "			} else {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	void visit_continueForStatementEnclosingIfStatement_shouldContainUnsupportedContinueStatement() throws Exception {
		String original = ""
				+ "	void continueForStatementEnclosingIfStatement(int value) {\n"
				+ "		for (;;) {\n"
				+ "			if (value == 1) {\n"
				+ "				continue;\n"
				+ "			} else if (value == 2) {\n"
				+ "			} else {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();

		List<IfStatement> ifStatements = IfStatementsCollector.collectIfStatements(typeDeclaration);
		assertFalse(ifStatements.isEmpty());
		Statement firstThenStatement = ifStatements.get(0)
			.getThenStatement();
		assertEquals(1, collectContinueStatements(firstThenStatement).size());
		ContinueStatementWithinIfVisitor continueStatementVisitor = new ContinueStatementWithinIfVisitor();
		firstThenStatement.accept(continueStatementVisitor);
		assertTrue(continueStatementVisitor.isContainingUnsupportedContinueStatement());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "			while (true) {\n"
					+ "				continue;\n"
					+ "			}",
			""
					+ "			for (;;) {\n"
					+ "				continue;\n"
					+ "			}",
			""
					+ "			do {\n"
					+ "				continue;\n"
					+ "			} while (true);",
			""
					+ "			for (int i : new int[100]) {\n"
					+ "				continue;\n"
					+ "			}",
			""
					+ "			Runnable r = () -> {\n"
					+ "				while (true) {\n"
					+ "					continue;\n"
					+ "				}\n"
					+ "			};",
			""
					+ "			Runnable r = new Runnable() {\n"
					+ "				@Override\n"
					+ "				public void run() {\n"
					+ "					while (true) {\n"
					+ "						continue;\n"
					+ "					}\n"
					+ "				}\n"
					+ "			};",
			""
					+ "			class LocalCass {\n"
					+ "				public void run() {\n"
					+ "					while (true) {\n"
					+ "						continue;\n"
					+ "					}\n"
					+ "				}\n"
					+ "			}",
	})
	void visit_shouldNotContainUnsupportedContinueStatement(String codeEscapingContinueStatement) throws Exception {
		String original = ""
				+ "	void supportedBreakStatement(int value) {\n"
				+ "		if (value == 1) {\n"
				+ codeEscapingContinueStatement + "\n"
				+ "		} else if (value == 2) {\n"
				+ "		} else {\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();

		List<IfStatement> ifStatements = IfStatementsCollector.collectIfStatements(typeDeclaration);
		assertFalse(ifStatements.isEmpty());
		Statement firstThenStatement = ifStatements.get(0)
			.getThenStatement();
		assertEquals(1, collectContinueStatements(firstThenStatement).size());
		ContinueStatementWithinIfVisitor continueStatementVisitor = new ContinueStatementWithinIfVisitor();
		firstThenStatement.accept(continueStatementVisitor);
		assertFalse(continueStatementVisitor.isContainingUnsupportedContinueStatement());
	}
}
