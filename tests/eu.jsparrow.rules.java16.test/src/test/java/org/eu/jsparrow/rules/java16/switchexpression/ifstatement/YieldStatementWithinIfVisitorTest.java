package org.eu.jsparrow.rules.java16.switchexpression.ifstatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.YieldStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.ReplaceMultiBranchIfBySwitchASTVisitor;
import eu.jsparrow.rules.java16.switchexpression.ifstatement.YieldStatementWithinIfVisitor;

class YieldStatementWithinIfVisitorTest extends UsesJDTUnitFixture {

	static List<YieldStatement> collectYieldStatements(Statement statement) {
		List<YieldStatement> contineStatements = new ArrayList<>();
		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public boolean visit(YieldStatement node) {
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
	void visit_YieldValueForSwitchExpressionEnclosingIfStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "		int yieldWithinIfStatement(int value, int value2) {\n"
				+ "			return switch (value) {\n"
				+ "			case 0 -> 0;\n"
				+ "			case 1 -> 1;\n"
				+ "			case 2 -> 4;\n"
				+ "			// break out of switch expression not allowed\n"
				+ "			// break;\n"
				+ "			default -> {\n"
				+ "				if (value2 == 0) {\n"
				+ "					yield 0;\n"
				+ "				} else if (value2 == 1) {\n"
				+ "					yield 1;\n"
				+ "				} else if (value2 == 4) {\n"
				+ "					yield 2;\n"
				+ "				} else {\n"
				+ "					yield -1;\n"
				+ "				}\n"
				+ "			}\n"
				+ "			};\n"
				+ "		}";

		assertNoChange(original);
	}

	@Test
	void visit_YieldValueForSwitchExpressionEnclosingIfStatement_shouldContainUnsupportedYieldStatement()
			throws Exception {
		String original = ""
				+ "		int yieldWithinIfStatement(int value, int value2) {\n"
				+ "			return switch (value) {\n"
				+ "			case 0 -> 0;\n"
				+ "			case 1 -> 1;\n"
				+ "			case 2 -> 4;\n"
				+ "			// break out of switch expression not allowed\n"
				+ "			// break;\n"
				+ "			default -> {\n"
				+ "				if (value2 == 0) {\n"
				+ "					yield 0;\n"
				+ "				} else if (value2 == 1) {\n"
				+ "					yield 1;\n"
				+ "				} else if (value2 == 4) {\n"
				+ "					yield 2;\n"
				+ "				} else {\n"
				+ "					yield -1;\n"
				+ "				}\n"
				+ "			}\n"
				+ "			};\n"
				+ "		}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();

		List<IfStatement> ifStatements = IfStatementsCollector.collectIfStatements(typeDeclaration);
		assertFalse(ifStatements.isEmpty());
		Statement firstThenStatement = ifStatements.get(0)
			.getThenStatement();
		assertEquals(1, collectYieldStatements(firstThenStatement).size());
		YieldStatementWithinIfVisitor continueStatementVisitor = new YieldStatementWithinIfVisitor();
		firstThenStatement.accept(continueStatementVisitor);
		assertTrue(continueStatementVisitor.isContainingUnsupportedYieldStatement());
	}

	@Test
	void visit_YieldStatementEscapedBySwitchExpression_shouldTransform() throws Exception {
		String original = ""
				+ "		void yieldWithinIfStatement(int value, int value2) {\n"
				+ "			if (value == 0) {\n"
				+ "				int result = switch (value2) {\n"
				+ "				case 0 -> 0;\n"
				+ "				default -> {\n"
				+ "					if (value2 == 0) {\n"
				+ "						yield 0;\n"
				+ "					}\n"
				+ "					yield 1;\n"
				+ "				}\n"
				+ "				};\n"
				+ "			} else if (value == 1) {\n"
				+ "\n"
				+ "			} else {\n"
				+ "\n"
				+ "			}\n"
				+ "		}";

		String expected = ""
				+ "		void yieldWithinIfStatement(int value, int value2) {\n"
				+ "			switch (value) {\n"
				+ "			case 0 -> {\n"
				+ "				int result = switch (value2) {\n"
				+ "				case 0 -> 0;\n"
				+ "				default -> {\n"
				+ "					if (value2 == 0) {\n"
				+ "						yield 0;\n"
				+ "					}\n"
				+ "					yield 1;\n"
				+ "				}\n"
				+ "				};\n"
				+ "				break;\n"
				+ "			}\n"
				+ "			case 1 -> {break;}\n"
				+ "			default -> {break;}\n"
				+ "			}\n"
				+ "		}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "				int result = switch (value2) {\n"
					+ "				case 0 -> 0;\n"
					+ "				default -> {\n"
					+ "					if (value2 == 0) {\n"
					+ "						yield 0;\n"
					+ "					}\n"
					+ "					yield 1;\n"
					+ "				}\n"
					+ "				};",
			""
					+ "				Runnable r = () -> {\n"
					+ "					int result = switch (value2) {\n"
					+ "					case 0 -> 0;\n"
					+ "					default -> {\n"
					+ "						if (value2 == 0) {\n"
					+ "							yield 0;\n"
					+ "						}\n"
					+ "						yield 1;\n"
					+ "					}\n"
					+ "					};\n"
					+ "				};",
			""
					+ "				Runnable r = new Runnable() {\n"
					+ "					@Override\n"
					+ "					public void run() {\n"
					+ "						int result = switch (value2) {\n"
					+ "						case 0 -> 0;\n"
					+ "						default -> {\n"
					+ "							if (value2 == 0) {\n"
					+ "								yield 0;\n"
					+ "							}\n"
					+ "							yield 1;\n"
					+ "						}\n"
					+ "						};\n"
					+ "					}\n"
					+ "				};",
			""
					+ "				class LocalClass {\n"
					+ "					public void run() {\n"
					+ "						int result = switch (value2) {\n"
					+ "						case 0 -> 0;\n"
					+ "						default -> {\n"
					+ "							if (value2 == 0) {\n"
					+ "								yield 0;\n"
					+ "							}\n"
					+ "							yield 1;\n"
					+ "						}\n"
					+ "						};\n"
					+ "					}\n"
					+ "				}",

	})
	void visit_EscapedYieldStatements_shouldNotContainUnsupportedYieldStatement(String codeEscapingYieldStatement)
			throws Exception {
		String original = ""
				+ "	void supportedYieldStatement(int value) {\n"
				+ "		if (value == 1) {\n"
				+ codeEscapingYieldStatement + "\n"
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
		assertEquals(3, collectYieldStatements(firstThenStatement).size());
		YieldStatementWithinIfVisitor continueStatementVisitor = new YieldStatementWithinIfVisitor();
		firstThenStatement.accept(continueStatementVisitor);
		assertFalse(continueStatementVisitor.isContainingUnsupportedYieldStatement());
	}
}
