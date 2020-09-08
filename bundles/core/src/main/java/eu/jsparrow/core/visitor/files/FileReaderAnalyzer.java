package eu.jsparrow.core.visitor.files;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class FileReaderAnalyzer {

	private VariableDeclarationExpression variableDeclaration;
	private Expression charsetExpression;
	private List<Expression> pathExpressions = new ArrayList<>();

	public FileReaderAnalyzer(VariableDeclarationExpression variableDeclaration) {
		this.variableDeclaration = variableDeclaration;
	}

	public boolean isFileReaderDeclaration() {
		Type type = variableDeclaration.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		if (!isContentOfType(typeBinding, java.io.FileReader.class.getName()) ||
				!ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Collections.singletonList(java.io.Reader.class.getName()))) {
			return false;
		}
		List<VariableDeclarationFragment> fragments = convertToTypedList(variableDeclaration.fragments(),
				VariableDeclarationFragment.class);
		if (fragments.size() != 1) {
			return false;
		}
		VariableDeclarationFragment fragment = fragments.get(0);

		Expression initialzier = fragment.getInitializer();
		if (initialzier == null || initialzier.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return false;
		}

		ClassInstanceCreation fileReaderCreation = (ClassInstanceCreation) initialzier;
		List<Expression> arguments = convertToTypedList(fileReaderCreation.arguments(), Expression.class);
		int argumentSize = arguments.size();
		if (argumentSize == 0 || argumentSize > 2) {
			return false;
		}
		Expression file = arguments.get(0);
		if (!isFileInstanceCreation(file)) {
			return false;
		}

		if (argumentSize == 2) {
			Expression charset = arguments.get(1);
			ITypeBinding charsetBinding = charset.resolveTypeBinding();
			if (isContentOfType(charsetBinding, java.nio.charset.Charset.class.getName())) {
				this.charsetExpression = charset;
			} else {
				return false;
			}
		}

		return true;
	}

	private boolean isFileInstanceCreation(Expression expression) {
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		if (isContentOfType(typeBinding, java.io.FileReader.class.getName())) {
			return false;
		}
		if (expression.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return false;
		}

		ClassInstanceCreation fileInstanceCreation = (ClassInstanceCreation) expression;
		this.pathExpressions = new ArrayList<>();
		List<Expression> arguments = convertToTypedList(fileInstanceCreation.arguments(), Expression.class);
		this.pathExpressions.addAll(arguments);
		return arguments
				.stream()
				.allMatch(argument -> isContentOfType(argument.resolveTypeBinding(), java.lang.String.class.getName()));
	}

	public Optional<Expression> getCharset() {
		return Optional.ofNullable(charsetExpression);
	}

	public List<Expression> getPathExpressions() {
		return pathExpressions;
	}

}
