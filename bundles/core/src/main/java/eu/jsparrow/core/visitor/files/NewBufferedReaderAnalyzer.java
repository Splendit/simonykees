package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class NewBufferedReaderAnalyzer {

	private Expression initializer;
	private List<Expression> pathExpressions = new ArrayList<>();

	public boolean isInitializedWithNewReader(ClassInstanceCreation newReader) {

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(newReader.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return false;
		}

		Expression readerArgument = arguments.get(0);
		if (ClassRelationUtil.isContentOfType(readerArgument.resolveTypeBinding(), java.lang.String.class.getName())) {
			pathExpressions.add(readerArgument);
			return true;
		} else if (readerArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation argInstanceCreation = (ClassInstanceCreation) readerArgument;
			Type argType = argInstanceCreation.getType();
			boolean isFile = ClassRelationUtil.isContentOfType(argType.resolveBinding(), java.io.File.class.getName());
			if (isFile) {
				List<Expression> fileArgs = ASTNodeUtil.convertToTypedList(argInstanceCreation.arguments(),
						Expression.class);
				pathExpressions.addAll(fileArgs);
				return fileArgs
					.stream()
					.map(Expression::resolveTypeBinding)
					.allMatch(t -> ClassRelationUtil.isContentOfType(t, java.lang.String.class.getName()));
			}
		}

		return false;
	}

	public Expression getInitializer() {
		return initializer;
	}

	public List<Expression> getPathExpressions() {
		return this.pathExpressions;
	}

	public Optional<Expression> getCharset() {
		// TODO:take it (if any) from the FileReader instance creation
		return Optional.empty();
	}

}
