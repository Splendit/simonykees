package eu.jsparrow.core.visitor.impl.entryset;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

/**
 * TODO: ??? move all functionalities to {@link ASTNodeBuilder} ???
 * 
 *
 */
public class TestHelper {

	static <T extends ASTNode> T extractFirstNodeOfType(ASTNode node, Class<T> nodeType) {
		List<ASTNode> list = new ArrayList<>();

		ASTVisitor visitor = new ASTVisitor() {

			@Override
			public void preVisit(ASTNode node) {
				list.add(node);
			}

		};
		node.accept(visitor);

		return list.stream()
			.filter(nodeType::isInstance)
			.findFirst()
			.map(nodeType::cast)
			.get();
	}

	static <T extends Expression> T createExpressionFromString(String code, Class<T> expressionSubclass)
			throws Exception {
		return expressionSubclass.cast(ASTNodeBuilder.createExpressionFromString(code));
	}

	static <T extends BodyDeclaration> T createBodyDeclarationFromString(String className,
			String bodyDeclaration,
			Class<T> bodyDeclarationSubclass) throws Exception {

		TypeDeclaration classDeclaration = ASTNodeBuilder.createTypeDeclarationFromString(className, bodyDeclaration);
		return bodyDeclarationSubclass.cast(classDeclaration.bodyDeclarations()
			.get(0));
	}

	static <T extends Statement> T createStatementFromString(String code, Class<T> statementSubClass)
			throws Exception {
		return statementSubClass.cast(ASTNodeBuilder.createBlockFromString(code)
			.statements()
			.get(0));

	}

	static EnumDeclaration createExampleEnumDeclaration() throws Exception {

		String enumDeclarationSource = ""
				+ "		enum ExampleEnum {\n"
				+ "			ENTRY;\n"
				+ "		}";
		EnumDeclaration enumDeclaration = createBodyDeclarationFromString("X", enumDeclarationSource,
				EnumDeclaration.class);
		return enumDeclaration;
	}
}
