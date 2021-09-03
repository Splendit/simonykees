package eu.jsparrow.jdtunit.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.rules.common.util.JdtCoreVersionBindingUtil;

/**
 * This utility class provides methods to convert strings into the following
 * subclasses of {@link ASTNode}:
 * 
 * <ul>
 * <li>{@link Block}</li>
 * <li>{@link CompilationUnit}</li>
 * <li>{@link TypeDeclaration}</li>
 * <li>{@link Expression}</li>
 * </ul>
 * 
 * @author Hans-Jörg Schnedlitz
 * @since 2.5.0
 */
@SuppressWarnings("nls")
public class ASTNodeBuilder {

	private ASTNodeBuilder() {

	}

	public static Block createBlockFromString(String string) throws JdtUnitException {
		Block block = prepareAstNode(string, Block.class);
		if (block.statements()
			.isEmpty()) {
			throw new JdtUnitException("Cannot create an empty block. There might be syntax errors");
		}
		return block;
	}

	public static TypeDeclaration createTypeDeclarationFromString(String typeDeclarationName, String source,
			List<String> modifiers)
			throws JdtUnitException {
		TypeDeclaration typeDeclaration = prepareAstNode(source, TypeDeclaration.class);
		AST ast = typeDeclaration.getAST();
		@SuppressWarnings("unchecked")
		List<Modifier> typeModifiers = typeDeclaration.modifiers();
		modifiers.stream()
			.map(ModifierKeyword::toKeyword)
			.map(ast::newModifier)
			.forEach(typeModifiers::add);
		typeDeclaration.setName(typeDeclaration.getAST()
			.newSimpleName(typeDeclarationName));
		if (typeDeclaration.bodyDeclarations()
			.isEmpty()) {
			throw new JdtUnitException("Cannot create an empty type declaration. There might be syntax errors");
		}
		return typeDeclaration;
	}

	public static TypeDeclaration createTypeDeclarationFromString(String typeDeclarationName, String string)
			throws JdtUnitException {
		return createTypeDeclarationFromString(typeDeclarationName, string, Collections.emptyList());
	}

	public static CompilationUnit createCompilationUnitFromString(String string) throws JdtUnitException {
		CompilationUnit compilationUnit = prepareAstNode(string, CompilationUnit.class);
		if (compilationUnit.types()
			.isEmpty()) {
			throw new JdtUnitException("Cannot create an empty compilation unit. There might be syntax errors");
		}
		return compilationUnit;
	}

	public static Expression createExpressionFromString(String string) throws JdtUnitException {
		return prepareAstNode(string, Expression.class);
	}

	private static <T extends ASTNode> T prepareAstNode(String string, Class<T> type) throws JdtUnitException {
		Optional<Integer> kindOptional = getParserKindFromType(type);
		int kind = kindOptional
			.orElseThrow(() -> new JdtUnitException("There is no ASTParser kind for the given type " + type.getName()));
		int astLevel = JdtCoreVersionBindingUtil.findJLSLevel(JdtCoreVersionBindingUtil.findCurrentJDTCoreVersion());
		ASTParser astParser = ASTParser.newParser(astLevel);
		astParser.setSource(string.toCharArray());
		astParser.setKind(kind);
		ASTNode result = astParser.createAST(null);
		if ((result.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED) {
			throw new JdtUnitException(String.format("Failed to parse '%s'.", string));
		}
		return type.cast(result);
	}

	private static <T extends ASTNode> Optional<Integer> getParserKindFromType(Class<T> type) {
		Optional<Integer> kind = Optional.empty();

		if (CompilationUnit.class.equals(type)) {
			kind = Optional.of(ASTParser.K_COMPILATION_UNIT);
		} else if (TypeDeclaration.class.equals(type)) {
			kind = Optional.of(ASTParser.K_CLASS_BODY_DECLARATIONS);
		} else if (Block.class.equals(type)) {
			kind = Optional.of(ASTParser.K_STATEMENTS);
		} else if (Expression.class.equals(type)) {
			kind = Optional.of(ASTParser.K_EXPRESSION);
		}

		return kind;
	}
}
