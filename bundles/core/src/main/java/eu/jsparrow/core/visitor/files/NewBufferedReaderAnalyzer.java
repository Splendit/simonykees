package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class NewBufferedReaderAnalyzer {

	private List<Expression> pathExpressions = new ArrayList<>();
	private Expression charsetExpression;

	public boolean isInitializedWithNewReader(ClassInstanceCreation newReader) {

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(newReader.arguments(), Expression.class);
		if (arguments.isEmpty()) {
			return false;
		}
		
		if(arguments.size() == 2) {
			Expression ndArgument = arguments.get(1);
			ITypeBinding ndArgType = ndArgument.resolveTypeBinding();
			if(!ClassRelationUtil.isContentOfType(ndArgType, java.nio.charset.Charset.class.getName())) {
				return false;
			}
			this.charsetExpression = ndArgument;
		}

		Expression readerArgument = arguments.get(0);
		if (ClassRelationUtil.isContentOfType(readerArgument.resolveTypeBinding(), java.lang.String.class.getName())) {
			pathExpressions.add(readerArgument);
			return true;
		} else if (readerArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation fileInstanceCreation = (ClassInstanceCreation) readerArgument;
			Type argType = fileInstanceCreation.getType();
			boolean isFile = ClassRelationUtil.isContentOfType(argType.resolveBinding(), java.io.File.class.getName());
			if (isFile) {
				List<Expression> fileArgs = ASTNodeUtil.convertToTypedList(fileInstanceCreation.arguments(),
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

	public List<Expression> getPathExpressions() {
		return this.pathExpressions;
	}

	public Optional<Expression> getCharset() {
		return Optional.ofNullable(charsetExpression);
	}

}
