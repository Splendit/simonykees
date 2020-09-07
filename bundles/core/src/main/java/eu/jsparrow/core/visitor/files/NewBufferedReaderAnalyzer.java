package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class NewBufferedReaderAnalyzer {

	private Expression initializer;
	private List<Expression> pathExpressions = new ArrayList<>();

	public boolean isInitializedWithNewReader(VariableDeclarationFragment fragment) {

		Expression initializer = fragment.getInitializer();
		if (initializer == null || initializer.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return false;
		}

		ClassInstanceCreation instanceCreation = (ClassInstanceCreation) initializer;
		Type type = instanceCreation.getType();
		ITypeBinding binding = type.resolveBinding();
		if (!ClassRelationUtil.isContentOfType(binding, java.io.BufferedReader.class.getName())) {
			return false;
		}

		ClassInstanceCreation newReader = findNewReaderInstanceCreation(instanceCreation).orElse(null);
		if (newReader == null) {
			return false;
		}
		
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(newReader.arguments(), Expression.class);
		if(arguments.size() != 1) {
			return false;
		}
		
		Expression readerArgument = arguments.get(0);
		if(ClassRelationUtil.isContentOfType(readerArgument.resolveTypeBinding(), java.lang.String.class.getName())) {
			pathExpressions.add(readerArgument);
			return true;
		} else if(readerArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation argInstanceCreation = (ClassInstanceCreation)readerArgument;
			Type argType = argInstanceCreation.getType();
			boolean isFile = ClassRelationUtil.isContentOfType(argType.resolveBinding(), java.io.File.class.getName());
			if(isFile ) {
				List<Expression> fileArgs = ASTNodeUtil.convertToTypedList(argInstanceCreation.arguments(), Expression.class);
				pathExpressions.addAll(fileArgs);
				return fileArgs
						.stream()
						.map(Expression::resolveTypeBinding)
						.allMatch(t -> ClassRelationUtil.isContentOfType(t, java.lang.String.class.getName()));
			}
		}

		return false;
	}

	private Optional<ClassInstanceCreation> findNewReaderInstanceCreation(ClassInstanceCreation instanceCreation) {
		List<Expression> bufferedReaderArguments = ASTNodeUtil.convertToTypedList(instanceCreation.arguments(),
				Expression.class);
		if (bufferedReaderArguments.size() != 1) {
			return Optional.empty();
		}
		Expression firstArg = bufferedReaderArguments.get(0);
		ITypeBinding argumentType = firstArg.resolveTypeBinding();
		boolean isReader = ClassRelationUtil.isContentOfType(argumentType, java.io.InputStreamReader.class.getName())
				|| ClassRelationUtil.isInheritingContentOfTypes(argumentType,
						Collections.singletonList(java.io.InputStreamReader.class.getName()));
		if (!isReader || firstArg.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return Optional.empty();
		}
		
		ClassInstanceCreation reader = (ClassInstanceCreation) firstArg;
		return Optional.of(reader);
	}

	//TODO: Return an optional of an expression. Remove this.initializer state.
	public boolean isInitializedWith(VariableDeclarationExpression newBufferedReader, SimpleName fileReaderName) {
		Type type = newBufferedReader.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, java.io.BufferedReader.class.getName())) {
			return false;
		}

		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(newBufferedReader.fragments(),
				VariableDeclarationFragment.class);
		if (fragments.size() != 1) {
			return false;
		}
		VariableDeclarationFragment fragment = fragments.get(0);
		Expression bufferedReaderInitializer = fragment.getInitializer();
		if (bufferedReaderInitializer == null) {
			return false;
		}
		this.initializer = bufferedReaderInitializer;

		if (bufferedReaderInitializer.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION) {
			return false;
		}
		ClassInstanceCreation instanceCreation = (ClassInstanceCreation) bufferedReaderInitializer;
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(instanceCreation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return false;
		}

		Expression argument = arguments.get(0);
		if (argument.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}

		SimpleName argumentName = (SimpleName) argument;
		ASTMatcher matcher = new ASTMatcher();
		return matcher.match(fileReaderName, argumentName);
	}

	public Expression getInitializer() {
		return initializer;
	}
	
	public List<Expression> getPathExpressions() {
		return this.pathExpressions;
	}
	
	public Optional<Expression> getCharset() {
		//TODO:take it (if any) from the FileReader instance creation
		return Optional.empty();
	}

}
